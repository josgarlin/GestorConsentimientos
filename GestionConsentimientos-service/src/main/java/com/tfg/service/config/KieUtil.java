package com.tfg.service.config;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;

public class KieUtil {

	private String URL = "http://localhost:8090/rest/server";
	private String USERNAME = "wbadmin";
	private String PASSWORD = "wbadmin";
	
	private KieServicesConfiguration config;

	public KieServicesClient getKieServicesClient() {
		config = KieServicesFactory.newRestConfiguration(URL, USERNAME, PASSWORD);
		config.setMarshallingFormat(MarshallingFormat.JSON);
//		Set<Class<?>> set = new HashSet<Class<?>>();
//		set.add(Sanitario.class);
//		config.addJaxbClasses(set);
		return KieServicesFactory.newKieServicesClient(config);
	}

}
