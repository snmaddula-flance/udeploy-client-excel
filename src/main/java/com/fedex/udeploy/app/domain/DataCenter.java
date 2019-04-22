package com.fedex.udeploy.app.domain;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DataCenter {
	
	private String name;
	private String componentName;
	private Map<String, List<String>> resourceMap;
	
}
