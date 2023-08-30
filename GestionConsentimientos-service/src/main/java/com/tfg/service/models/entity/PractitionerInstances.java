package com.tfg.service.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "practitioner_instances")
public class PractitionerInstances {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Long id;
	@Column(name = "practitioner", nullable = false)
	private String practitioner;
	@Column(name = "instance", nullable = false)
	private Long instance;
	@Column(name = "title", nullable = false)
	private String title;
	@Column(name = "endDate", nullable = false)
	private String endDate;
	@Column(name = "questionnaireResponse", nullable = false)
	private String questionnaireResponse;
	@Column(name = "patients", nullable = false)
	private String patients;

	public PractitionerInstances() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PractitionerInstances(String practitioner, Long instance, String title, String endDate,
			String questionnaireResponse, String patients) {
		this.practitioner = practitioner;
		this.instance = instance;
		this.title = title;
		this.endDate = endDate;
		this.questionnaireResponse = questionnaireResponse;
		this.patients = patients;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPractitioner() {
		return practitioner;
	}

	public void setPractitioner(String practitioner) {
		this.practitioner = practitioner;
	}

	public Long getInstance() {
		return instance;
	}

	public void setInstance(Long instance) {
		this.instance = instance;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getQuestionnaireResponse() {
		return questionnaireResponse;
	}

	public void setQuestionnaireResponse(String questionnaireResponse) {
		this.questionnaireResponse = questionnaireResponse;
	}

	public String getPatients() {
		return patients;
	}

	public void setPatients(String patients) {
		this.patients = patients;
	}

}
