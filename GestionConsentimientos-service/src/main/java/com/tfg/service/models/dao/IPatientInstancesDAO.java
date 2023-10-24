package com.tfg.service.models.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfg.service.models.entity.PatientInstance;

@Repository
public interface IPatientInstancesDAO extends JpaRepository<PatientInstance, Long> {
	
	List<PatientInstance> findByPatient(String patient);
	@Transactional
	void deleteByPatientAndInstance(String patient, Long instance);
	Boolean existsByInstance(Long instance);

}
