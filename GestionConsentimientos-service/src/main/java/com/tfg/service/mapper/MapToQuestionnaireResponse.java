package com.tfg.service.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r5.model.BooleanType;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.hl7.fhir.r5.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
import org.hl7.fhir.r5.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.r5.model.StringType;

public class MapToQuestionnaireResponse implements IMapper<Map<String, Object>, QuestionnaireResponse> {

	private Questionnaire questionnaire;
	
	public MapToQuestionnaireResponse(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}

	@Override
	public QuestionnaireResponse map(Map<String, Object> in) {
		QuestionnaireResponse response =  new QuestionnaireResponse();
		
		response.setStatus(QuestionnaireResponseStatus.COMPLETED);
		response.setQuestionnaire(questionnaire.getId());
		
		for (Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem()) {
			if (!(in.get(item.getLinkId()) == null)) {
				switch (item.getType()) {
				case BOOLEAN:
					response.addItem()
						.setLinkId(item.getLinkId())
						.setText(item.getText())
						.addAnswer()
							.setValue(new BooleanType((String) in.get(item.getLinkId())));
					break;
				case STRING:
					response.addItem()
						.setLinkId(item.getLinkId())
						.setText(item.getText())
						.addAnswer()
							.setValue(new StringType((String) in.get(item.getLinkId())));
					break;
				case CHOICE:
					response.addItem()
						.setLinkId(item.getLinkId())
						.setText(item.getText())
						.addAnswer()
							.setValue(new StringType((String) in.get(item.getLinkId())));
					break;
				case DATE:
					response.addItem()
						.setLinkId(item.getLinkId())
						.setText(item.getText())
						.addAnswer()
							.setValue(new StringType((String) in.get(item.getLinkId())));
					break;
				case GROUP:
					responseGroup(response, item, item.getItem(), (Map<String, Object>) in.get(item.getLinkId()));
					break;
				default:
					throw new UnsupportedOperationException("Tipo de componente no soportado: " + item.getType().getDisplay());
				}
			}
		}
		
		return response;
	}
	
	private void responseGroup(QuestionnaireResponse response, QuestionnaireItemComponent item, List<QuestionnaireItemComponent> items, Map<String, Object> resultados) {
		List<QuestionnaireResponseItemComponent> example = new ArrayList<QuestionnaireResponse.QuestionnaireResponseItemComponent>();
		
		for (Questionnaire.QuestionnaireItemComponent it : items) {
			QuestionnaireResponseItemComponent t = new QuestionnaireResponseItemComponent();
			if (!(resultados.get(it.getLinkId()) == null)) {
				switch (it.getType()) {
				case BOOLEAN:
					t.setLinkId(it.getLinkId())
						.setText(it.getText())
						.addAnswer()
							.setValue(new BooleanType((String) resultados.get(it.getLinkId())));
					example.add(t);
					break;
				case STRING:
					t.setLinkId(it.getLinkId())
						.setText(it.getText())
						.addAnswer()
							.setValue(new StringType((String) resultados.get(it.getLinkId())));
					example.add(t);
					break;
				case CHOICE:
					t.setLinkId(it.getLinkId())
						.setText(it.getText())
						.addAnswer()
							.setValue(new StringType((String) resultados.get(it.getLinkId())));
					example.add(t);
					break;
				case DATE:
					t.setLinkId(it.getLinkId())
						.setText(it.getText())
						.addAnswer()
							.setValue(new StringType((String) resultados.get(it.getLinkId())));
					example.add(t);
					break;
				case GROUP:
					responseGroup(response, it, it.getItem(), (Map<String, Object>) resultados.get(it.getLinkId()));
					break;
				default:
					throw new UnsupportedOperationException("Tipo de componente no soportado: " + it.getType().getDisplay());
				}
			}
		}
		
		response.addItem()
			.setLinkId(item.getLinkId())
			.setText(item.getText())
			.setItem(example);
	}
}
