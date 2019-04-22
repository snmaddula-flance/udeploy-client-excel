package com.fedex.udeploy.app.service;

import static org.springframework.http.HttpMethod.PUT;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fedex.udeploy.app.config.UDeployManifest;
import com.fedex.udeploy.app.dto.UDResourceReq;

import lombok.AllArgsConstructor;



@Service
@AllArgsConstructor
public class TeamService {

	private static final String ENVIRONMENT = "_Environment";
	private RestTemplate rt;
	private UDeployManifest manifest;
	
	public void addTeam(String agent, String team) {
		HttpEntity<UDResourceReq> entity = new HttpEntity<>(manifest.getBasicAuthHeaders());
		ResponseEntity<String> response = rt.exchange(manifest.createTeamUri(agent, team).toUri(), PUT, entity, String.class);
		int statusCode = response.getStatusCodeValue();
		if(statusCode == 200) {
			System.out.println("AGENT: [" + agent + " ] ADDED TO TEAM: [ " + team + " ]");
		}else {
			if(response.getBody() != null) System.out.println(response.getBody());
		}
	}

	public void addComponentToTeam(String component, String team) {
		HttpEntity<UDResourceReq> entity = new HttpEntity<>(manifest.getBasicAuthHeaders());
		ResponseEntity<String> response = rt.exchange(manifest.addTeamUri(component, team).toUri(), PUT, entity, String.class);
		int statusCode = response.getStatusCodeValue();
		if(statusCode == 200) {
			System.out.println("COMPONENT: [" + component + " ] ADDED TO TEAM: [ " + team + " ]");
		}else {
			if(response.getBody() != null) System.out.println(response.getBody());
		}
	}

	public void addEnvironmentToTeam(String appName, String level, String team) {
		HttpEntity<UDResourceReq> entity = new HttpEntity<>(manifest.getBasicAuthHeaders());
		String type = level + ENVIRONMENT;
		ResponseEntity<String> response = rt.exchange(manifest.addEnvironmentToTeamUri(appName, level, team, type).toUri(), PUT, entity, String.class);
		int statusCode = response.getStatusCodeValue();
		if(statusCode == 200) {
			System.out.println("ENVIRONMENT: [" + type + " ] ADDED TO TEAM: [ " + team + " ]");
		}else {
			if(response.getBody() != null) System.out.println(response.getBody());
		}
	}

}
