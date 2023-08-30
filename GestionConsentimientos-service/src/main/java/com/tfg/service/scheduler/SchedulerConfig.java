package com.tfg.service.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
@EnableScheduling
public class SchedulerConfig {
	
	@Bean
	public JobDetailFactoryBean jobDetail() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(ScheduledJob.class); // Clase que ejecuta la tarea
        factory.setDurability(true);
        return factory;
    }
	
	@Bean
    public CronTriggerFactoryBean cronTrigger() {
        CronTriggerFactoryBean factory = new CronTriggerFactoryBean();
        factory.setJobDetail(jobDetail().getObject());
        factory.setCronExpression("0 0 0 * * ?"); // Ejecutar todos los días a las 12:00 AM
        return factory;
    }

}
