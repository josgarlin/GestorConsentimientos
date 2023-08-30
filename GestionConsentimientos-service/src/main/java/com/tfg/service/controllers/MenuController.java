package com.tfg.service.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tfg.service.IPatientService;
import com.tfg.service.IPractitionerService;
import com.tfg.service.models.entity.User;
import com.tfg.service.models.service.IUserService;

@Controller
@RequestMapping("/private")
public class MenuController {
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private IPractitionerService practitionerService;
	
	@Autowired
	private IPatientService patientService;
	
	@GetMapping("/")
	public String menu(Authentication auth, HttpSession session) {
		String out = "menu";
		String username = auth.getName();
		
		if (session.getAttribute("user") == null) {
			User user = userService.findByDni(username);
			user.setPassword(null);
			session.setAttribute("user", user);
			
			if (user.getPractitioner()) {
				out = "menu";
			} else {
				out = "redirect:/private/patient/";
			}
		}
		
		return out;
	}
	
	@GetMapping("/practitioner")
	public String menuPractitioner() {
		return "menuPractitioner";
	}
	
	@PostMapping("/practitioner")
	public String responseQuestionnairePractitioner(HttpServletRequest request) {
		String redirect = null;
		
		practitionerService.completedTask(request);
		
		redirect = "redirect:/private/practitioner?success";
		
		return redirect;
	}
	
	@GetMapping("/patient")
	@ResponseBody
	private String menuPatient(Authentication auth) {
		String contentHtml = patientService.generateFile(auth.getName().toUpperCase());
		
		return contentHtml;
	}
	
	@PostMapping("/patient")
	public String responseQuestionnairePatient(HttpServletRequest request) {
		String redirect = null;
		
		patientService.completedTask(request);
		
		redirect = "redirect:/private/patient?success";
		
		return redirect;
	}

}
