package com.tfg.service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.json.JSONObject;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.stereotype.Service;

import com.tfg.service.config.KieUtil;
import com.tfg.service.mapper.MapToQuestionnaireResponse;
import com.tfg.service.mapper.QuestionnaireToFormPractitioner;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Service
public class PractitionerService {
	
	private FhirContext ctx = FhirContext.forR5();
	private IParser parser = ctx.newJsonParser().setPrettyPrint(true);
	
	private final QuestionnaireToFormPractitioner questionnaireToFormPractitioner = new QuestionnaireToFormPractitioner();
	private MapToQuestionnaireResponse mapToQuestionnaireResponse;
	
	private static final Logger logger = LogManager.getLogger();

	// Crea una nueva instancia del proceso
	public Long newInstanceProcess() {
		Long idInstanceProcess = null;
		
		KieUtil util = new KieUtil();
		KieServicesClient kieServicesClient = util.getKieServicesClient();
		ProcessServicesClient processServicesClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		
		logger.info("PROCEDEMOS A CREAR UNA NUEVA INSTANCIA DEL PROCESO");
		idInstanceProcess = processServicesClient.startProcess("GestionConsentimientos-kjar", "GestionConsentimientos-kjar.practitionerProcess");
		logger.info("INSTANCIA CREADA CORRECTAMENTE CON ID = "+idInstanceProcess);
		
		return idInstanceProcess;
	}
	
	// Inicia y finaliza la tarea
	public String executeTask(String idInstanceProcess) {
		String response = null;
		
		KieUtil util = new KieUtil();
		KieServicesClient kieServicesClient = util.getKieServicesClient();
		UserTaskServicesClient userClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);
		QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
		
		// Obtenemos el id de la tarea con la que queremos trabajar, la del proceso instanciado
		Long idTask = getIdTask(userClient, Long.valueOf(idInstanceProcess));
		
		// Comprobamos que el id de la tarea no sea null
		if (idTask != null) {
			logger.info("INICIAMOS LA TAREA HUMANA CON ID = "+idTask);
			// Empezamos la tarea, cambia su estado de "Reserved" a "InProgress"
			userClient.startTask("GestionConsentimientos-kjar", idTask, "wbadmin");
			// Completamos la tarea
			completeTask(userClient, idTask);
			
			// Una vez terminada la tarea y ejecutado el proceso completo
			// Procedemos a obtener las variables que nos interesan
			
			// Obtenemos todas las variables del proceso
			List<VariableInstance> variableInstancesList = queryClient.findVariablesCurrentState(Long.valueOf(idInstanceProcess));
			
			// Para facilitarlo
			// Recorremos la lista, guardando en un Map en nombre de la variable y su indice correspondiente en la lista
			Map<String, Integer> index = new HashMap<String, Integer>();
			for (int i = 0; i < variableInstancesList.size(); i++) {
				String name = variableInstancesList.get(i).getVariableName();
				index.put(name, i);
			}
			
			// OBTENEMOS EL VALOR DE LA VARIABLE id_QuestionnaireResponse
			VariableInstance variableInstance = variableInstancesList.get(index.get("id_QuestionnaireResponse"));
			String idQuestionnaireResponse = getIdQuestionnaireResponse(variableInstance.getValue());
			
			// Generamos la respuesta
			response = "{"
					+"\"idQuestionnaireResponse\": \"" + idQuestionnaireResponse + "\""
					+"}";
		}
		
		return response;
	}
	
	// Metodo que termina de completar la tarea humana
	private void completeTask(UserTaskServicesClient userClient, Long idTask) {
		logger.info("PROCEDEMOS A COMPLETAR LA TAREA CON ID = "+idTask);
		
		// Obtenemos el parametro de entrada de la tarea, el meta-cuestionario
		Map<String, Object> inputContentTask = userClient.getTaskInputContentByTaskId("GestionConsentimientos-kjar", idTask);
		String questionnaire = inputContentTask.get("questionnaire").toString();
		
		// Llamada al metodo principal de la clase, el cual realiza la tarea para obtener el recurso QuestionnaireResponse
		String questionnaireResponse = task(questionnaire);
		// Llamada al metodo patients() para obtener la lista de pacientes a los que se dirige el profesional sanitario
		List<String> patients = patients();
		
		// Incluimos los parametros de salida de la tarea humana en un Map, en donde sus claves se corresponden con el nombre de la variable en la tarea
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("questionnaireResponse", questionnaireResponse);
		params.put("patients", patients);
		
		userClient.completeTask("GestionConsentimientos-kjar", idTask, "wbadmin", params);
		logger.info("TAREA CON ID = "+idTask+", COMPLETADA CON ÉXITO");
	}
	
	// Metodo principal que realiza la tarea humana
	private String task(String questionnaire) {
		String questionnaireResponse = null;
		
		// Convertimos el parametro de entrada en recurso fhir Questionnaire
		Questionnaire quest = parser.parseResource(Questionnaire.class, questionnaire);
		
		// Mapeamos el recurso fhir a formulario javax.swing y nos devuelve la respuesta en un Map
		Map<String, Object> responseForm = questionnaireToFormPractitioner.map(quest);
		logger.info("\tMapeo de recurso fhir Questionnaire a Formulario realizado correctamente");
		
		// Mapeamos la respuesta del formulario a un recurso fhir QuestionnaireResponse
		mapToQuestionnaireResponse = new MapToQuestionnaireResponse(quest);
		QuestionnaireResponse questResponse = mapToQuestionnaireResponse.map(responseForm);
		logger.info("\tMapeo de respuesta de Formualrio a recurso fhir QuestionnaireResponse realizado correctamente");
		
		// Convertimos el recurso fhir QuestionnaireResponse a String
		questionnaireResponse = parser.encodeResourceToString(questResponse);
		
		return questionnaireResponse;
	}
	
	// Pacientes a los que va dirigido la consulta de consentimiento
	private List<String> patients(){
		List<String> patients = new ArrayList<String>();
		
		patients.add("Jose");
		
		return patients;
	}
	
	// Metodo que nos devuelve el id de la tarea
	private Long getIdTask(UserTaskServicesClient userClient, Long idInstanceProcess) {
		Long idTask = null;
		
		// Obtenemos todas las tareas humanas pertenecientes a la instancia del proceso, independientemente de su estado
		List<TaskSummary> taskList = userClient.findTasksByStatusByProcessInstanceId(idInstanceProcess, null, 0, 0);
		// Nos quedamos con la primera, la más reciente
		TaskSummary task = taskList.get(0);
		
		// Comprobamos que su estado sea el adecuado para iniciarla
		if (task.getStatus().equals("Reserved")) {
			idTask = task.getId();
		}
		
		return idTask;
	}
	
	// Metodo que nos devuelve el id del recurso fhir QuestionnaireResponse persistido en la tarea REST
	private String getIdQuestionnaireResponse(String value) {
		String newValue = value.substring(0, value.lastIndexOf(",")).concat("\n}");
		JSONObject json = new JSONObject(newValue);
		String idQuestionnaireResponse = json.getString("id");
		
		return idQuestionnaireResponse;
	}
	
	// Recoge los campos despues de enviar el formulario
	public void sendFormHtml(String body) {
		this.questionnaireToFormPractitioner.onClickButton(body);
	}
}
