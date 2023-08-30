package com.tfg.service.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.service.models.entity.PractitionerInstances;

@Repository
public interface IPractitionerInstancesDAO extends JpaRepository<PractitionerInstances, Long> {
	
	List<PractitionerInstances> findByPractitioner(String practitioner);
	Boolean existsByInstance(Long instance);
	PractitionerInstances  findByInstance(Long instance);

}
