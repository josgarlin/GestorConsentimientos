package com.tfg.service.models.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "dni"))
public class User implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "dni", nullable = false)
	private String dni;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "surname", nullable = false)
	private String surname;
	@Column(name = "email", nullable = false)
	private String email;
	@Column(name = "password", nullable = false)
	private String password;
	@Column(name = "practitioner", nullable = false)
	private Boolean practitioner;
	
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}

	public User(String dni, String name, String surname, String email, String password, Boolean practitioner) {
		this.dni = dni;
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
		this.practitioner = practitioner;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getPractitioner() {
		return practitioner;
	}

	public void setPractitioner(Boolean practitioner) {
		this.practitioner = practitioner;
	}

}
