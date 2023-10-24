package com.tfg.service.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.service.models.entity.PractitionerInstance;

@Repository
public interface IPractitionerInstancesDAO extends JpaRepository<PractitionerInstance, Long> {
	
	List<PractitionerInstance> findByPractitioner(String practitioner);
	Boolean existsByInstance(Long instance);
	PractitionerInstance  findByInstance(Long instance);

}
