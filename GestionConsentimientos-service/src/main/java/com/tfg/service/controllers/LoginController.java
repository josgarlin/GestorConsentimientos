package com.tfg.service.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.tfg.service.models.entity.User;

@Controller
public class LoginController {
	
	@GetMapping("/")
	public String start() {
		return "redirect:/login";
	}
	
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("user", new User());
		
		return "login";
	}

}
