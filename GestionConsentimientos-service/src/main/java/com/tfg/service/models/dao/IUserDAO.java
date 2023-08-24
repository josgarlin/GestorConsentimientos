package com.tfg.service.models.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tfg.service.models.entity.User;

@Repository
public interface IUserDAO extends JpaRepository<User, String> {
	
	User findByDni(String dni);
	@Query("SELECT u.practitioner FROM User u WHERE u.dni = :dni")
	Boolean isPractitionerByDni(String dni);

}
