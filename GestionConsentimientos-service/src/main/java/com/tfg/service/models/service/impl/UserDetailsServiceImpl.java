package com.tfg.service.models.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tfg.service.models.dao.IUserDAO;
import com.tfg.service.models.entity.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private IUserDAO userDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		username = username.toUpperCase(); // En la bbdd se guarda en mayusculas
		
		User user = userDAO.findByDni(username);
		UserBuilder builder = null;
		
		if (user != null) {
			builder = org.springframework.security.core.userdetails.User.withUsername(username);
			builder.disabled(false);
			builder.password(user.getPassword());
			builder.authorities(role(user));
		} else {
			throw new UsernameNotFoundException("User not found");
		}
		
		return builder.build();
	}
	
	private Collection<GrantedAuthority> role(User user) {
		Collection<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		
		// Añadimos el rol por defecto
		roles.add(new SimpleGrantedAuthority("Patient"));
		
		// Si es practitioner, le añadimos ese rol
		if (user.getPractitioner()) {
			roles.add(new SimpleGrantedAuthority("Practitioner"));
		}
		
		return roles;
	}

}
