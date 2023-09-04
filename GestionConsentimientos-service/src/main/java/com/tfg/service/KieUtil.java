package com.tfg.service;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;

public class KieUtil {
	
	private String URL;
	private String USERNAME;
	private String PASSWORD;
	
	private KieServicesConfiguration config;

	public KieUtil(String uRL, String uSERNAME, String pASSWORD) {
		URL = uRL;
		USERNAME = uSERNAME;
		PASSWORD = pASSWORD;
	}

	public ProcessServicesClient getProcessServicesClient() {
		KieServicesClient kieServicesClient = getKieServicesClient();
		ProcessServicesClient processServicesClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
		
		return processServicesClient;
	}
	
	public UserTaskServicesClient getUserTaskServicesClient() {
		KieServicesClient kieServicesClient = getKieServicesClient();
		UserTaskServicesClient userClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);
		
		return userClient;
	}
	
	public QueryServicesClient getQueryServicesClient() {
		KieServicesClient kieServicesClient = getKieServicesClient();
		QueryServicesClient queryClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
		
		return queryClient;
	}

	private KieServicesClient getKieServicesClient() {
		config = KieServicesFactory.newRestConfiguration(URL, USERNAME, PASSWORD);
		config.setMarshallingFormat(MarshallingFormat.JSON);
		
		return KieServicesFactory.newKieServicesClient(config);
	}

}
