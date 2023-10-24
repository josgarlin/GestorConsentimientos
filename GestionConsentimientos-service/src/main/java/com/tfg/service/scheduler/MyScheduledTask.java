package com.tfg.service.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tfg.service.models.dao.IPractitionerInstancesDAO;
import com.tfg.service.models.entity.PractitionerInstance;

@Component
public class MyScheduledTask {
	
	@Autowired
	private IPractitionerInstancesDAO practitionerInstancesDAO;
	
	// Actualizar los procesos del practitioner para que se borren de la bbdd cuando pase la fecha de validez
	public void executeTask() {
		// Obtenemos la fecha actual
		LocalDate currentDate = LocalDate.now();
		
		// Obtenemos toda la bbdd con los procesos instanciados por los practitioners
		List<PractitionerInstance> practitionerInstancesList = practitionerInstancesDAO.findAll();
		
		for (PractitionerInstance practitionerInstances : practitionerInstancesList) {
			LocalDate endDate = LocalDate.parse(practitionerInstances.getEndDate());
			
			if (currentDate.isAfter(endDate)) {
				// Borro en la bbdd
				practitionerInstancesDAO.delete(practitionerInstances);
			}
		}
	}

}
