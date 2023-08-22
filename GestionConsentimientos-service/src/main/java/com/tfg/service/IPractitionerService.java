package com.tfg.service;

import javax.servlet.http.HttpServletRequest;

import com.tfg.service.models.entity.User;

public interface IPractitionerService {
	
	public String requestConsent(User user);
	public void completedTask(HttpServletRequest request);

}
