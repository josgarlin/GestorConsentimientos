package com.tfg.service.mapper;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemType;

public class QuestionnaireToFormPatient implements IMapper<Questionnaire, Map<String, Object>> {

private Map<String, Object> result;
	
	private Questionnaire questionnaire;
	
	@Override
	public Map<String, Object> map(Questionnaire in) {
		Map<String, Object> fillResult = null;
		questionnaire = in;
		
		String contentHtml = generateHtml(in);
		
		String fileName = createFileHtml(contentHtml);
		showFormHtml(fileName);
		
		while (fillResult == null) {
			fillResult = getResult();
		}
		
		// Borro el fichero
		deleteFile(fileName);
		
		return fillResult;
	}
	
	public Map<String, Object> getResult() {
		return result;
	}
	
	public void onClickButton(String body) {
		result = new HashMap<String, Object>();
		
		body = body.replace("%3B", ";");
		String[] queryParameters = body.split("&");

		for (Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem()) {
			if (item.getType() == QuestionnaireItemType.GROUP) {
				Map<String, Object> groupResult = new HashMap<String, Object>();
				for (QuestionnaireItemComponent it : item.getItem()) {
					String value = getValueParameter(queryParameters, it.getLinkId());
					groupResult.put(it.getLinkId(), value);
				}
				result.put(item.getLinkId(), groupResult);
			} else { 
				String value = getValueParameter(queryParameters, item.getLinkId());
				result.put(item.getLinkId(), value);
			}
		}
	}
	
	private String getValueParameter(String[] queryParameters, String id) {
		String result = null;
		
		// Mapa para almacenar temporalmente los resultados
        Map<String, StringBuilder> resultMap = new HashMap<>();
		
		for (String parameter : queryParameters) {
			 String[] parts = parameter.split("=");
			 
			 if (parts.length == 2) {
				 String key = parts[0];
	             String value = parts[1];
	             
	             // Verificar si la clave corresponde a la que se busca
	             if (key.equals(id)) {
	            	 if (resultMap.containsKey(key)) {
	            		 StringBuilder sb = resultMap.get(key);
	                     sb.append(";").append(value);
	            	 } else {
	            		 StringBuilder sb = new StringBuilder(value);
	                     resultMap.put(key, sb);
	            	 }
	            	 
	            	 // Obtener el resultado como un solo string separado por ";"
		             result = resultMap.get(id).toString();
	             }
			 }
		}
		
		return result;
	}

	private void showFormHtml(String fileName) {
		String filePath = "file:///" + getFilePath(fileName);
			
	    try {
	       	String rutaFormateada = filePath.replace(" ", "%20");
			URI uri = new URI(rutaFormateada);
				
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(uri);
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getFilePath(String fileName) {
		String folderPath = "src/main/resources/public/";
		String absolutePath = null;
		String filePath = folderPath + fileName;
		
		File file = new File(filePath);
		
		if (file.exists()) {
			absolutePath = file.getAbsolutePath().replace("\\", "/");
		}
		
		return absolutePath;
	}
	
	private String createFileHtml(String contentHtml) {
		String folderPath = "src/main/resources/public/";
		String fileName = "Questionnaire.html";
		String filePath = folderPath + fileName;
		
		deleteFile(filePath);
		
		try {
			FileWriter fileWriter = new FileWriter(filePath);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			
			printWriter.println(contentHtml);
			
			printWriter.close();
			System.out.println("Archivo " + fileName + " generado correctamente.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fileName;
	}
	
	private void deleteFile(String fileName) {
		String folderPath = "src/main/resources/public/";
		String filePath = folderPath + fileName;
		
		File file = new File(filePath);
		
		if (file.exists()) {
			if (file.delete()) {
				System.out.println("Archivo borrado con éxito");
			} else {
				System.out.println("No se a podido borrar el archivo");
			}
		} else {
			System.out.println("El archivo no existe");
		}
	}
	
	private String generateHtml(Questionnaire questionnaire) {
		String html = "<!DOCTYPE html>\r\n"
				+ "<html>\r\n";
		
		String head = generatehead();
		html = html + head;
		
		String body = generateBody(questionnaire);
		html = html + body;
		
		html = html + "</html>";

		return html;
	}
	
	private String generateBody(Questionnaire questionnaire) {
		String body = null;
		
		body = "<body>\r\n"
				+ "  <h1>Questionnaire</h1>\r\n"
				+ "  <form action=\"http://localhost:8090/patientProcess/formHTML\" method=\"post\">\r\n";
		
		for (Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem()) {
			String question = generateQuestion(item);
			if (question != null) {
				body = body + question;
			}
		}	
		
		body = body
				+ "    <input type=\"submit\" value=\"Enviar\">\r\n"
				+ "  </form>\r\n"
				+ "</body>\r\n";
		
		return body;
	}
	
	private String generateQuestion(Questionnaire.QuestionnaireItemComponent item) {
		String question = null;
		
		switch (item.getType()) {
		case BOOLEAN:
			question = createBooleanComponent(item);
			break;
		case STRING:
			question = createStringComponent(item);
			break;
		case DATE:
			question = createDateComponent(item);
			break;
		case CHOICE:
			question = createChoiceComponent(item);
			break;
		case GROUP:
			question = createGroupComponent(item);
			break;
		default:
			throw new UnsupportedOperationException("Tipo de componente no soportado: " + item.getType().getDisplay());
		}
		
		return question;
	}
	
	private String createGroupComponent(Questionnaire.QuestionnaireItemComponent item) {
		String nameGroup = item.getText() + ":";
		String component = "    <fieldset>\r\n"
				+ "      <legend>+" + nameGroup + "+</legend>\r\n";
		
		for (Questionnaire.QuestionnaireItemComponent it : item.getItem()) {
			switch (it.getType()) {
			case BOOLEAN:
				component = component + createBooleanComponent(it);
				break;
			case STRING:
				component = component + createStringComponent(it);
				break;
			case DATE:
				component = component + createDateComponent(it);
				break;
			case CHOICE:
				component = component + createChoiceComponent(it);
				break;
			case GROUP:
				component = component + createGroupComponent(it);
				break;
			default:
				throw new UnsupportedOperationException("Tipo de componente no soportado: " + it.getType().getDisplay());
			}
		}
		
		component = component + "    </fieldset>\r\n";
		
		return component;
	}
	
	private String createBooleanComponent(Questionnaire.QuestionnaireItemComponent item) {
		String question = item.getText();
		String id = item.getLinkId();
		
		String component = null;
		
		if (item.getRequired()) {
			component = "<div class=\"consent-question\">\r\n"
					+ "<label for=\"" + id + "\">" + question + "</label>\r\n"
					+ "    <div class=\"radio-group\">\r\n"
					+ "      <label>\r\n"
					+ "        <input type=\"radio\" id=\"" + id + "-yes\" name=\"" + id + "\" value=\"true\" required>\r\n"
					+ "        <span>Sí, estoy de acuerdo y autorizo</span>\r\n"
					+ "      </label>\r\n"
					+ "      <label>\r\n"
					+ "        <input type=\"radio\" id=\"" + id + "-no\" name=\"" + id + "\" value=\"false\" required>\r\n"
					+ "        <span>No, no estoy de acuerdo</span>\r\n"
					+ "      </label>\r\n"
					+ "    </div>\r\n"
					+ "</div>\r\n";
		} else {
			String value = item.getAnswerOption().get(0).getValue().toString();
			
			if (value.equals("true")) {
				component = "<label for=\"" + id + "\">" + question + "</label>\r\n"
						+ "    <div class=\"radio-group\">\r\n"
						+ "      <label>\r\n"
						+ "        <input type=\"radio\" id=\"" + id + "-yes\" name=\"" + id + "\" value=\"true\" checked> Sí\r\n"
						+ "      </label>\r\n"
						+ "      <label>\r\n"
						+ "        <input type=\"radio\" id=\"" + id + "-no\" name=\"" + id + "\" value=\"false\" disabled> No\r\n"
						+ "      </label>\r\n"
						+ "    </div>\r\n";
			} else {
				component = "<label for=\"" + id + "\">" + question + "</label>\r\n"
						+ "    <div class=\"radio-group\">\r\n"
						+ "      <label>\r\n"
						+ "        <input type=\"radio\" id=\"" + id + "-yes\" name=\"" + id + "\" value=\"true\" disabled> Sí\r\n"
						+ "      </label>\r\n"
						+ "      <label>\r\n"
						+ "        <input type=\"radio\" id=\"" + id + "-no\" name=\"" + id + "\" value=\"false\" checked> No\r\n"
						+ "      </label>\r\n"
						+ "    </div>\r\n";
			}
		}
		
		return component;
	}
	
	private String createStringComponent(Questionnaire.QuestionnaireItemComponent item) {
		String question = item.getText();
		String id = item.getLinkId();
		String value = item.getAnswerOption().get(0).getValue().toString();
		
		String component = "	<label for=\"" + id + "\">" + question + "</label>\r\n"
					+ "		<input type=\"text\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + value + "\" readonly>\r\n";
		
		return component;
	}
	
	private String createDateComponent(Questionnaire.QuestionnaireItemComponent item) {
		String question = item.getText();
		String id = item.getLinkId();
		String value = item.getAnswerOption().get(0).getValue().toString();
		String[] dates = value.split(";");
		
		String component = "	<label for=\"" + id + "\">" + question + "</label>\r\n"
				+ "     <div>\r\n"
				+ "     <span>Start:</span>\r\n"
				+ "		<input type=\"date\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + dates[0] + "\" readonly>\r\n"
				+ "     </div>\r\n"
				+ "     <div>\r\n"
				+ "     <span>Start:</span>\r\n"
				+ "		<input type=\"date\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + dates[1] + "\" readonly>\r\n"
				+ "     </div>\r\n";
		
		return component;
	}
	
	private String createChoiceComponent(Questionnaire.QuestionnaireItemComponent item) {
		String question = item.getText();
		String id = item.getLinkId();
		String values = item.getAnswerOption().get(item.getAnswerOption().size()-1).getValue().toString();
		List<QuestionnaireItemAnswerOptionComponent> options = item.getAnswerOption().subList(0, item.getAnswerOption().size()-1);
		
		String component = "<label>" + question + "</label>\r\n"
				+ "      <div class=\"checkbox-group\">\r\n";
		
		for (QuestionnaireItemAnswerOptionComponent option : options) {
			String code = option.getValueCoding().getCode();
			String display = option.getValueCoding().getDisplay();
			if (checkMark(values, code)) {
				component = component + "<label><input type=\"checkbox\" name=\"" + id + "\" value=\"" + code + "\" checked><span>" + display + "</span></label>\r\n";
			} else {
				component = component + "<label><input type=\"checkbox\" name=\"" + id + "\" value=\"" + code + "\" disabled><span>" + display + "</span></label>\r\n";
			}
		}
		
		component = component + "      </div>\r\n";
		
		return component;
	}
	
	private boolean checkMark(String values, String code) {
		Boolean check = false;
		
		for (String value : values.split(";")) {
			if (value.equals(code)) {
				check = true;
			}
		}
		
		return check;
	}
	
	private String generatehead() {
		String head = null;
		String style = generateStyle();
		
		head = "<head>\r\n"
				+ "  <title>Questionnaire</title>\r\n"
				+ style
				+ "</head>\r\n";
		
		return head;
	}
	
	private String generateStyle() {
		String style = "<style>\r\n"
				+ "    body {\r\n"
				+ "      font-family: Arial, sans-serif;\r\n"
				+ "      background-color: #f2f2f2;\r\n"
				+ "      padding: 20px;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    h1 {\r\n"
				+ "      color: #333333;\r\n"
				+ "      text-align: center;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    form {\r\n"
				+ "      max-width: 500px;\r\n"
				+ "      margin: 0 auto;\r\n"
				+ "      background-color: #ffffff;\r\n"
				+ "      padding: 20px;\r\n"
				+ "      border-radius: 5px;\r\n"
				+ "      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    label {\r\n"
				+ "      display: block;\r\n"
				+ "      margin-bottom: 10px;\r\n"
				+ "      font-weight: bold;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    input[type=\"text\"],\r\n"
				+ "    input[type=\"date\"] {\r\n"
				+ "      width: 100%;\r\n"
				+ "      height: 20px;\r\n"
				+ "      padding: 10px;\r\n"
				+ "      border: 1px solid #cccccc;\r\n"
				+ "      border-radius: 4px;\r\n"
				+ "      box-sizing: border-box;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .checkbox-group {\r\n"
				+ "      display: grid;\r\n"
				+ "      grid-template-columns: repeat(3, 1fr);\r\n"
				+ "      grid-gap: 10px;\r\n"
				+ "      margin-bottom: 10px;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .checkbox-group label {\r\n"
				+ "      display: flex;\r\n"
				+ "      align-items: center;\r\n"
				+ "      font-size: 14px;\r\n"
				+ "      font-weight: normal;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .checkbox-group label input[type=\"checkbox\"] {\r\n"
				+ "      margin-right: 5px;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    input[type=\"submit\"] {\r\n"
				+ "      display: block;\r\n"
				+ "      margin: 20px auto;\r\n"
				+ "      background-color: #4CAF50;\r\n"
				+ "      color: #ffffff;\r\n"
				+ "      padding: 10px 20px;\r\n"
				+ "      border: none;\r\n"
				+ "      border-radius: 4px;\r\n"
				+ "      cursor: pointer;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    input[type=\"submit\"]:hover {\r\n"
				+ "      background-color: #45a049;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .consent-question {\r\n"
				+ "      margin-top: 20px;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .consent-question label {\r\n"
				+ "      font-weight: lighter;\r\n"
				+ "      font-style: italic;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .consent-question input[type=\"radio\"] {\r\n"
				+ "      margin-right: 5px;\r\n"
				+ "      transform: scale(1.2);\r\n"
				+ "      -webkit-appearance: none;\r\n"
				+ "      -moz-appearance: none;\r\n"
				+ "      appearance: none;\r\n"
				+ "      border: 2px solid #333333;\r\n"
				+ "      border-radius: 50%;\r\n"
				+ "      outline: none;\r\n"
				+ "      width: 16px;\r\n"
				+ "      height: 16px;\r\n"
				+ "      cursor: pointer;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .consent-question input[type=\"radio\"]:checked {\r\n"
				+ "      background-color: #333333;\r\n"
				+ "    }\r\n"
				+ "\r\n"
				+ "    .consent-question input[type=\"radio\"]:focus {\r\n"
				+ "      box-shadow: 0 0 3px #333333;\r\n"
				+ "    }\r\n"
				+ "  </style>\r\n";
		
		return style;
	}
}
