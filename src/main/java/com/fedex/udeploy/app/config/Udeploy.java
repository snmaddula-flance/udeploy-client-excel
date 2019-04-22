package com.fedex.udeploy.app.config;

import static org.springframework.beans.BeanUtils.copyProperties;

import java.io.File;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fedex.udeploy.app.domain.DataCenter;
import com.fedex.udeploy.app.domain.UDComponent;

import lombok.Data;

@Data
@Component
public class Udeploy {

	private String team;
	private String appName;
	private String appEAI;
	private String resourceGroup;
	private List<UDComponent> components;
	private List<DataCenter> dataCenters;

	@Value("${input-file}")
	private @JsonIgnore String inputFile;

	@PostConstruct
	public void loadConfig() throws Exception {
		copyProperties(new ObjectMapper(new YAMLFactory()).readValue(new File(inputFile), getClass()), this);
	}
}