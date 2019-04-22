package com.fedex.udeploy.app.service;

public interface PropertyService {

	void addApplicationProperty(String application, String name, String value);

	void addComponentProperty(String component, String name, String value);
}
