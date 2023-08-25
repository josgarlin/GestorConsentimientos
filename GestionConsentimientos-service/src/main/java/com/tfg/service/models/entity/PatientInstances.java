package com.tfg.service.models.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "patient_instances")
public class PatientInstances implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Long id;
	@Column(name = "patient", nullable = false)
	private String patient;
	@Column(name = "instance", nullable = false)
	private Long instance;
	@Column(name = "title", nullable = false)
	private String title;

	public PatientInstances() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PatientInstances(String patient, Long instance, String title) {
		this.patient = patient;
		this.instance = instance;
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
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

}
