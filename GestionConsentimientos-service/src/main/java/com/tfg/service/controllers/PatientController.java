package com.tfg.service.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tfg.service.IPatientService;
import com.tfg.service.models.entity.User;

@Controller
@RequestMapping("/private/patient")
public class PatientController {
	
	@Autowired
	private IPatientService patientService;
	
	@GetMapping("/seeRequestedConsent")
	@ResponseBody
	public String seeRequestedConsent(HttpSession session, @RequestParam("param") Long idInstanceProcess) {
		User user = (User) session.getAttribute("user");
		
		String contentHtml = patientService.seeRequestedConsent(user, idInstanceProcess);
		
		return contentHtml;
	}

}
