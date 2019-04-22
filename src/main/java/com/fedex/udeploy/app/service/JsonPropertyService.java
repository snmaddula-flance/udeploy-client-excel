package com.fedex.udeploy.app.service;

import static org.springframework.http.HttpMethod.PUT;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fedex.udeploy.app.config.UDeployManifest;
import com.fedex.udeploy.app.dto.UDPropertyReq;

import lombok.RequiredArgsConstructor;

@Service
@Profile("sandbox")
@RequiredArgsConstructor
public class JsonPropertyService implements PropertyService {
	
	private final RestTemplate rt;
	private final UDeployManifest manifest;

	@Override
	public void addApplicationProperty(String application, String name, String value) {
		HttpEntity<UDPropertyReq> entity = new HttpEntity<>(UDPropertyReq.forApp(application, name, value), manifest.getBasicAuthHeaders());
		ResponseEntity<String> response = rt.exchange(manifest.addApplicationPropertyUri().toUri(), PUT, entity, String.class);

		int statusCode = response.getStatusCodeValue();
		if (statusCode == 200) {
			System.out.println("PROPERTY: [" + name + "=" + value + " ] ADDED TO APPLICATION: [ " + application + " ]");
		}
		if (StringUtils.hasText(response.getBody()))
			System.err.println(response.getBody());
		
	}
	
	@Override
	public void addComponentProperty(String component, String name, String value) {
		HttpEntity<UDPropertyReq> entity = new HttpEntity<>(UDPropertyReq.forComponent(component, name, value), manifest.getBasicAuthHeaders());
		  ResponseEntity<String> response = rt.exchange(manifest.addComponentPropertyUri().toUri(), PUT, entity, String.class);

		  int statusCode = response.getStatusCodeValue();
		  if(statusCode == 200) {
		    System.out.println("PROPERTY: [" + name + "=" + value + " ] ADDED TO COMPONENT: [ " + component + " ]");
		  }
		    if(StringUtils.hasText(response.getBody())) System.err.println(response.getBody());
		
	}
	
}
