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

	public PatientInstances() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PatientInstances(String patient, Long instance) {
		this.patient = patient;
		this.instance = instance;
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

}
