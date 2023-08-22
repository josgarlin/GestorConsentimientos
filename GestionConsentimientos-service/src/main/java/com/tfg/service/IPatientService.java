package com.tfg.service;

import javax.servlet.http.HttpServletRequest;

import com.tfg.service.models.entity.User;

public interface IPatientService {
	
	public String generateFile(String dni);
	public String seeRequestedConsent(User user, Long idInstanceProcess);
	public void completedTask(HttpServletRequest request);

}
