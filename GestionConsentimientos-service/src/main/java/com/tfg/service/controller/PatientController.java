package com.tfg.service.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.service.service.PatientService;

@RestController
@RequestMapping("/patientProcess")
public class PatientController {

	private final PatientService service = new PatientService();

	@PostMapping("/instances/questionnaireResponse/{id}")
	public Long newInstanceProcess(@PathVariable("id") String idQuestionnaireResponse) {
		return this.service.newInstanceProcess(idQuestionnaireResponse);
	}
	
	@PutMapping("/instances/{id}/task/execute")
	public String executeTask(@PathVariable("id") String idInstanceProcess) {
		return this.service.executeTask(idInstanceProcess);
	}
	
	@PostMapping("/formHTML")
//	@Operation(hidden = true)	// Ocultar en swagger
	private void sendFormHtml(@RequestBody String body) {
		this.service.sendFormHtml(body);
	}
}
