package com.tfg.service.models.service.impl;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tfg.service.models.dao.IUserDAO;
import com.tfg.service.models.entity.User;
import com.tfg.service.models.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {
	
	@Autowired
	private IUserDAO userDAO;

	@Override
	public User findByDni(String dni) {
		return userDAO.findByDni(dni);
	}

	@Override
	public User register(User u) {
		return userDAO.save(u);
	}

	@Override
	public Boolean checkDNIExist(String dni) {
		Boolean result = null;
		User user = userDAO.findByDni(dni);
		
		if (user != null) {
			result = true;
		} else {
			result = false;
		}
		
		return result;
	}

	@Override
	public Boolean checkDNIFormat(String dni) {
		Boolean result = null;
		String pattern = "\\d{8}[A-Za-z]";
		
		if (dni.length() != 9) {
			result = true;
		} else {
			if (Pattern.matches(pattern, dni)) {
				result = false;
			} else {
				result = true;
			}
		}
		
		return result;
	}

}
