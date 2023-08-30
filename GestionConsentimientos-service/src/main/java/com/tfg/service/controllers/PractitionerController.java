package com.tfg.service.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tfg.service.IPractitionerService;
import com.tfg.service.models.entity.User;

@Controller
@RequestMapping("/private/practitioner")
public class PractitionerController {
	
	@Autowired
	private IPractitionerService practitionerService;
	
	@GetMapping("/requestConsent")
	@ResponseBody
	public String requestConsent(HttpSession session) {
		User user = (User) session.getAttribute("user");
		
		String contentHtml = practitionerService.requestConsent(user);
		
		return contentHtml;
	}
	
	@GetMapping("/consentsRequested")
	@ResponseBody
	public String consentsRequested(HttpSession session) {
		User user = (User) session.getAttribute("user");
		
		String contentHtml = practitionerService.consentsRequested(user.getDni());
		
		return contentHtml;
	}
	
	@GetMapping("/consentsRequested/consent")
	@ResponseBody
	public String seeRequestedConsent(@RequestParam("param") Long idInstanceProcess) {
		String contentHtml = practitionerService.seeConsent(idInstanceProcess);
		
		return contentHtml;
	}

}
