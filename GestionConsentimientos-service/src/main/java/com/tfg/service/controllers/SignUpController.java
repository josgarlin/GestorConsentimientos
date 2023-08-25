package com.tfg.service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.tfg.service.models.entity.User;
import com.tfg.service.models.service.IUserService;

@Controller
public class SignUpController {
	
	@Autowired
	private IUserService userService;
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		
		return "signup";
	}
	
	@PostMapping("/signup")
	public String register(@ModelAttribute User user, BindingResult result, Model model) {
		String redirect = null;
		
		if (result.hasErrors()) {
			redirect = "redirect:/signup";
		} else {
			if (userService.checkDNIFormatError(user.getDni())) {
				redirect = "redirect:/signup?error";
			} else {
				user.setDni(user.getDni().toUpperCase());
				if (userService.checkDNIExist(user.getDni())) {
					redirect = "redirect:/signup?exist";
				} else {
					model.addAttribute("user", userService.register(user));
					redirect = "redirect:/login?success";
				}
			}
		}
		
		return redirect;
	} 

}
