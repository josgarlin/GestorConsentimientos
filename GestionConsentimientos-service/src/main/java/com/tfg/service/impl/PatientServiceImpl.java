package com.tfg.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tfg.service.IPatientService;
import com.tfg.service.KieUtil;
import com.tfg.service.mapper.CreatePatientMenu;
import com.tfg.service.mapper.MapToQuestionnaireResponse;
import com.tfg.service.mapper.QuestionnaireResponseToQuestionnaire;
import com.tfg.service.mapper.QuestionnaireToFormPatient;
import com.tfg.service.models.dao.IPatientInstancesDAO;
import com.tfg.service.models.dao.IUserDAO;
import com.tfg.service.models.entity.PatientInstances;
import com.tfg.service.models.entity.User;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Service
public class PatientServiceImpl implements IPatientService {

	@Value("${kieserver.location}")
	private String URL;

	private KieUtil util;

	@Value("${kieserver.containerId}")
	private String containerId;
	@Value("${kieserver.processId.patient}")
	private String processId;

	private static User user;

	private static final Logger logger = LogManager.getLogger();

	private FhirContext ctx = FhirContext.forR5();
	private IParser parser = ctx.newJsonParser().setPrettyPrint(true);

	@Autowired
	private IPatientInstancesDAO patientInstancesDAO;
	
	@Autowired 
	private IUserDAO userDAO;

	private CreatePatientMenu createPatientMenu;
	private final QuestionnaireResponseToQuestionnaire questionnaireResponseToQuestionnaire = new QuestionnaireResponseToQuestionnaire();
	private final QuestionnaireToFormPatient questionnaireToFormPatient = new QuestionnaireToFormPatient();
	private MapToQuestionnaireResponse mapToQuestionnaireResponse;

	private static Questionnaire questionnaire;
	private static Long idInstanceProcess;
	private static Long idTask;

	@Override
	public String generateFile(String dni) {
		String contentHtml = null;
		
		// Instancias de procesos del paciente autenticado
		List<PatientInstances> patientInstancesList = patientInstancesDAO.findByPatient(dni);
		Map<Long, String> instancesAndTitle = new HashMap<Long, String>();
		for (PatientInstances patientInstances : patientInstancesList) {
			instancesAndTitle.put(patientInstances.getInstance(), patientInstances.getTitle());
		}
		
		// Comprobamos si ademas de Patient es Practitioner
		Boolean isPractitioner = userDAO.isPractitionerByDni(dni);
		createPatientMenu = new CreatePatientMenu(isPractitioner);
		contentHtml = createPatientMenu.map(instancesAndTitle);

		return contentHtml;
	}

	@Override
	public String seeRequestedConsent(User user, Long idInstanceProcess) {
		String contentHtml = null;

		this.user = user;
		util = new KieUtil(URL, user.getDni(), userDAO.findByDni(user.getDni()).getPassword());

		this.idInstanceProcess = idInstanceProcess;
		contentHtml = startTask(idInstanceProcess);

		return contentHtml;
	}

	// Inicia la tarea
	private String startTask(Long idInstanceProcess) {
		String contentHtml = null;

		UserTaskServicesClient userClient = util.getUserTaskServicesClient();

		// Obtenemos el id de la tarea con la que queremos trabajar
		Long idTask = getIdTask(userClient, idInstanceProcess);
		this.idTask = idTask;

		if (idTask != null) {
			logger.info("INICIAMOS LA TAREA HUMANA CON ID = " + idTask);
			// Empezamos la tarea, cambia su estado de "Ready" a "InProgress"
			userClient.startTask(containerId, idTask, user.getDni());

			// Obtenemos los parametros de entrada de la tarea
			Map<String, Object> inputContentTask = userClient.getTaskInputContentByTaskId(containerId, idTask);
			String questionnaireResponse = inputContentTask.get("questionnaireResponse").toString();

			// Convertimos los parametros de entrada en recursos fhir
			QuestionnaireResponse questResponse = parser.parseResource(QuestionnaireResponse.class, questionnaireResponse);

			// Mapeamos el recurso fhir QuestionnaireResponse a Questionnaire
			Questionnaire questionnaire = questionnaireResponseToQuestionnaire.map(questResponse);
			this.questionnaire = questionnaire;
			logger.info("\tMapeo de recurso fhir QuestionnaireResponse a Questionnaire realizado correctamente");

			// Mapeamos el recurso fhir a formulario html y obtenemos el nombre del fichero
			contentHtml = questionnaireToFormPatient.map(questionnaire);
			logger.info("\tMapeo de recurso fhir Questionnaire a Formulario realizado correctamente");
		}

		return contentHtml;
	}

	// Metodo que nos devuelve el id de la tarea
	private Long getIdTask(UserTaskServicesClient userClient, Long idInstanceProcess) {
		Long idTask = null;

		// Obtenemos todas las tareas humanas pertenecientes a la instancia del proceso,
		// independientemente de su estado
		List<TaskSummary> taskList = userClient.findTasksByStatusByProcessInstanceId(idInstanceProcess, null, 0, 0);
		// Nos quedamos con la primera, la más reciente
		TaskSummary task = taskList.get(0);

		// Comprobamos que su estado sea el adecuado para iniciarla
		if (task.getStatus().equals("Ready")) {
			idTask = task.getId();
		}

		return idTask;
	}

	@Override
	public void completedTask(HttpServletRequest request) {
		// Obtenemos los campos rellenados del Meta-Cuestionario en un Map
		Map<String, String[]> responseForm = request.getParameterMap();

		// Mapeamos la respuesta del formulario a un recurso fhir QuestionnaireResponse
		mapToQuestionnaireResponse = new MapToQuestionnaireResponse(questionnaire);
		QuestionnaireResponse questResponse = mapToQuestionnaireResponse.map(responseForm);
		logger.info("\tMapeo de respuesta de Formulario a recurso fhir QuestionnaireResponse realizado correctamente");

		// Convertimos el recurso fhir QuestionnaireResponse a String
		String questionnaireResponse = parser.encodeResourceToString(questResponse);

		// Incluimos los parametros de salida de la tarea humana en un Map, en donde sus
		// claves se corresponden con el nombre de la variable en la tarea
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("questionnaireResponse1", questionnaireResponse);

		UserTaskServicesClient userClient = util.getUserTaskServicesClient();
		userClient.completeTask(containerId, idTask, user.getDni(), params);
		logger.info("TAREA CON ID = "+idTask+" COMPLETADA CON ÉXITO");
		
		// Borramos esa instancia de la tabla "patient_instance" de la bbdd
		patientInstancesDAO.deleteByPatientAndInstance(user.getDni(), idInstanceProcess);
	}

}
