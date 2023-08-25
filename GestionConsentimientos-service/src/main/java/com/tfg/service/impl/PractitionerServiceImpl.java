package com.tfg.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tfg.service.IPractitionerService;
import com.tfg.service.KieUtil;
import com.tfg.service.mapper.MapToQuestionnaireResponse;
import com.tfg.service.mapper.QuestionnaireToFormPractitioner;
import com.tfg.service.models.dao.IPatientInstancesDAO;
import com.tfg.service.models.dao.IUserDAO;
import com.tfg.service.models.entity.PatientInstances;
import com.tfg.service.models.entity.User;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Service
public class PractitionerServiceImpl implements IPractitionerService {
	
	@Value("${kieserver.location}")
	private String URL;
	
	private KieUtil util;
	
	@Value("${kieserver.containerId}")
	private String containerId;
	@Value("${kieserver.processId.practitioner}")
	private String processId;
	
	private static User user;
	
	private static final Logger logger = LogManager.getLogger();
	
	private FhirContext ctx = FhirContext.forR5();
	private IParser parser = ctx.newJsonParser().setPrettyPrint(true);
	
	private final QuestionnaireToFormPractitioner questionnaireToFormPractitioner = new QuestionnaireToFormPractitioner();
	private MapToQuestionnaireResponse mapToQuestionnaireResponse;
	
	private static Questionnaire questionnaire;
	private static Long idTask;
	
	@Autowired
	private IUserDAO userDAO;
	
	@Autowired
	private IPatientInstancesDAO patientInstancesDAO;

	@Override
	public String requestConsent(User user) {
		String contentHtml = null;
		
		this.user = user;
		util = new KieUtil(URL, user.getDni(), userDAO.findByDni(user.getDni()).getPassword());
		
		Long idInstanceProcess = newInstanceProcess();
		contentHtml = startTask(idInstanceProcess);
		
		return contentHtml;
	}
	
	// Crea una nueva instancia del proceso
	private Long newInstanceProcess() {
		Long idInstanceProcess = null;
			
		ProcessServicesClient processServicesClient = util.getProcessServicesClient();
		
		logger.info("PROCEDEMOS A CREAR UNA NUEVA INSTANCIA DEL PROCESO");
		idInstanceProcess = processServicesClient.startProcess(containerId, processId);
		logger.info("INSTANCIA CREADA CORRECTAMENTE CON ID = "+idInstanceProcess);
			
		return idInstanceProcess;
	}
	
	// Inicia la tarea
	private String startTask(Long idInstanceProcess) {
		String contentHtml = null;
			
		UserTaskServicesClient userClient = util.getUserTaskServicesClient();
			
		// Obtenemos el id de la tarea con la que queremos trabajar
		Long idTask = getIdTask(userClient, idInstanceProcess);
		this.idTask = idTask;
			
		if (idTask != null) {
			logger.info("INICIAMOS LA TAREA HUMANA CON ID = "+idTask);
			// Empezamos la tarea, cambia su estado de "Ready" a "InProgress"
			userClient.startTask(containerId, idTask, user.getDni());
				
			// Obtenemos el parametro de entrada de la tarea, el meta-cuestionario
			Map<String, Object> inputContentTask = userClient.getTaskInputContentByTaskId(containerId, idTask);
			String questionnaire = inputContentTask.get("questionnaire").toString();
				
			// Convertimos el parametro de entrada en recurso fhir Questionnaire
			Questionnaire quest = parser.parseResource(Questionnaire.class, questionnaire);
			this.questionnaire = quest;
				
			// Mapeamos el recurso fhir a formulario html y obtenemos el nombre del fichero
			contentHtml = questionnaireToFormPractitioner.map(quest);
			logger.info("\tMapeo de recurso fhir Questionnaire a Formulario realizado correctamente");
		}
			
		return contentHtml;
	}
	
	// Metodo que nos devuelve el id de la tarea
	private Long getIdTask(UserTaskServicesClient userClient, Long idInstanceProcess) {
		Long idTask = null;
			
		// Obtenemos todas las tareas humanas pertenecientes a la instancia del proceso, independientemente de su estado
		List<TaskSummary> taskList = userClient.findTasksByStatusByProcessInstanceId(idInstanceProcess, null, 0, 0);
		// Nos quedamos con la primera, la más reciente
		TaskSummary task = taskList.get(0);
			
		// Comprobamos que su estado sea el adecuado para iniciarla
		if (task.getStatus().equals("Ready")) {
			idTask = task.getId();
		}
			
		return idTask;
	}

	// Con el cuestionario y los pacientes a los que este va dirigido, completamos la tarea humana
	@Override
	public void completedTask(HttpServletRequest request) {
		// Obtenemos los campos rellenados del Meta-Cuestionario en un Map
		Map<String, String[]> responseForm = request.getParameterMap();
		
		// Obtenemos el titulo
		String title = responseForm.get("1.1")[0];
				
		// Obtenemos la lista de pacientes a los que va dirigido el consentimiento
		List<String> patientList = getPatientList(responseForm.get("1.2")[0]);
				
		// Borramos el campo correspondiente a los pacientes
		responseForm = deleteFielsPatients(responseForm);
				
		// Mapeamos la respuesta del formulario a un recurso fhir QuestionnaireResponse
		mapToQuestionnaireResponse = new MapToQuestionnaireResponse(questionnaire);
		QuestionnaireResponse questResponse = mapToQuestionnaireResponse.map(responseForm);
		logger.info("\tMapeo de respuesta de Formulario a recurso fhir QuestionnaireResponse realizado correctamente");
				
		// Convertimos el recurso fhir QuestionnaireResponse a String
		String questionnaireResponse = parser.encodeResourceToString(questResponse);
			
		// Incluimos los parametros de salida de la tarea humana en un Map, en donde sus claves se corresponden con el nombre de la variable en la tarea
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("questionnaireResponse", questionnaireResponse);
		params.put("patientList", patientList);
		params.put("title", title);
				
		UserTaskServicesClient userClient = util.getUserTaskServicesClient();
		userClient.completeTask(containerId, idTask, user.getDni(), params);
		logger.info("TAREA CON ID = "+idTask+" COMPLETADA CON ÉXITO");
				
		// Asociamos los pacientes con la instancia de proceso correspondiente
		pendingPatientProcess(patientList);
	}
	
	private Map<String, String[]> deleteFielsPatients(Map<String, String[]> response){
		Map<String, String[]> result = new HashMap<String, String[]>();
		
		for (Map.Entry<String, String[]> entry : response.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			
			if (!(key.equals("1.2"))) {
				result.put(key, values);
			}
		}
		
		return result;
	}
	
	// Obtenemos una lista con los pacientes
	private List<String> getPatientList(String patients) {
		List<String> patientList = Arrays.asList(patients.split(";"));
			
		return patientList;
	}
	
	// Asocia el identificador del paciente con su proceso activo, su instancia
	private void pendingPatientProcess(List<String> patientList) {
		// Obtener todos los procesos instanciados del contenedor
		QueryServicesClient queryClient = util.getQueryServicesClient();
		List<Integer> status = new ArrayList<Integer>();
		// Estado "Activo"
		status.add(1);
		List<ProcessInstance> processInstancesList = queryClient.findProcessInstancesByContainerId(containerId+"-1_0-SNAPSHOT", status, 0, 10);
		System.out.println(processInstancesList);
			
		for (ProcessInstance processInstance : processInstancesList) {
			if (processInstance.getProcessName().equals("patientProcess")) {
				Long processInstanceId = processInstance.getId();
				
				// Obtenemos las variables de la instancia
				List<VariableInstance> variableList = queryClient.findVariablesCurrentState(processInstanceId);
				
				// Obtenemos la variable "title"
				String title = queryClient.findVariableHistory(processInstanceId, "title", 0, 0).get(0).getValue();
					
				// Obtenemos la variable "patient"
				for (VariableInstance variableInstance : variableList) {
					if (variableInstance.getVariableName().equals("patient")) {
						String patientVar = variableInstance.getValue();
						// Comprobamos que existe ese paciente en la bbdd					
						if (userDAO.existsById(patientVar)) {
							PatientInstances patientInstances = new PatientInstances(patientVar, processInstanceId, title);
							patientInstancesDAO.save(patientInstances);
						} else {
							// Abortamos esa instancia, ya que no existe el paciente
							ProcessServicesClient processServicesClient = util.getProcessServicesClient();
							processServicesClient.abortProcessInstance(containerId, processInstanceId);
						}
					}
	            }
			}
		}
	}

}
