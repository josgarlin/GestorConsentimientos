package com.tfg.service.models.service;

import com.tfg.service.models.entity.User;

public interface IUserService {
	
	User findByDni(String dni);
	User register(User u);
	Boolean checkDNIExist(String dni);
	Boolean checkDNIFormatError(String dni);

}
