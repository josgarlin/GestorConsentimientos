package com.tfg.service.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class ScheduledJob extends QuartzJobBean {

	@Autowired
	private MyScheduledTask myScheduledTask;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		myScheduledTask.executeTask();
	}

}
