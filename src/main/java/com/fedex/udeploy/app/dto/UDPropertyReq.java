package com.fedex.udeploy.app.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
public class UDPropertyReq {
	
	private String component;
	private String application;
	private String name;
	private String value;
	
	public static UDPropertyReq forApp(String application, String name, String value) {
		return new UDPropertyReq(null, application, name, value);
	}

	public static UDPropertyReq forComponent(String component, String name, String value) {
		return new UDPropertyReq(component, null, name, value);
	}
	

}
