package com.fedex.udeploy.app.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fedex.udeploy.app.config.Udeploy;
import com.fedex.udeploy.app.domain.UDComponent;

import lombok.AllArgsConstructor;



@Service
@AllArgsConstructor
public class AppService {

	private Udeploy udeploy;
	private ObjectWriter writer;
	private ApplicationContext applicationContext;
	
	private TagService tagService;
	private TeamService teamService;
	private AgentService agentService;
	private ResourceService resourceService;
	private ComponentService componentService;
	private ValidationService validationService;
	private ApplicationService applicationService;
	private EnvironmentService environmentService;
	private PropertyService propertyService;
	
	
	public void createResource() {
		try {
			final String parent = udeploy.getResourceGroup().trim();
			final String appName = udeploy.getAppName();
			final String appEAI = udeploy.getAppEAI();
			final String team = udeploy.getTeam();
			final List<UDComponent> components = udeploy.getComponents();
			final boolean componentsExist = !components.isEmpty();
			applicationService.createApplication(appName, parent);
			propertyService.addApplicationProperty(appName, "appName", appName);
			propertyService.addApplicationProperty(appName, "appEAI", appEAI);
			
			components.forEach(component -> {
				final String componentName = component.getComponentName();
				final String componentDesc = component.getComponentDesc();
				final String componentPath = component.getComponentPath();
				final String componentAppName = component.getComponentAppName();
				final String componentDeployDir = component.getComponentDeployDir();
				final String componentDiskSpace = component.getComponentDiskSpace();
				final String componentSudoUser = component.getComponentSudoUser();
				
				componentService.createComponent(componentName, componentDesc, componentPath);
				
				propertyService.addComponentProperty(componentName, "appName", componentAppName);
				propertyService.addComponentProperty(componentName, "deployDir", componentDeployDir);
				propertyService.addComponentProperty(componentName, "diskSpace", componentDiskSpace);
				propertyService.addComponentProperty(componentName, "sudoUser", componentSudoUser);

				componentService.addComponentToApp(appName, component.getComponentName());
			});
			
			resourceService.createRoot(parent);
			resourceService.createApp(parent, appName);
			
	//		validationService.validateTeam(team);
	//		validationService.validateParent(parent);
			
			udeploy.getDataCenters().forEach(dc -> {
				final String dcName = dc.getName();
				dc.getResourceMap().forEach((level, agents) -> {
					resourceService.createGroup(parent, appName, level);
					environmentService.createEnvironment(appName, level);
					teamService.addEnvironmentToTeam(appName, level, team);
					agents.forEach(agent -> {
						environmentService.createEnvironment(appName, level);
						agentService.addAgent(parent, appName, level, agent);
						agentService.addAgentToEnv(parent, appName, level, agent);
						teamService.addTeam(agent, team);
						tagService.addTag(parent, appName, level, agent, dcName);
						if(componentsExist)
						componentService.addComponent(parent, appName, level, agent, dc.getComponentName());
					});
					if(componentsExist)
					teamService.addComponentToTeam(dc.getComponentName(), team);
				});
			});
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			shutdown();
		}
	}


	@PostConstruct
	public void logConfig() throws JsonProcessingException {
		System.out.println("\n==================================== UDEPLOY CONFIG =====================================");
		System.out.println("\n" + writer.writeValueAsString(udeploy));
		System.out.println("\n=========================================================================================\n");
	}
	
	private void shutdown() {
		try {
			((ConfigurableApplicationContext) applicationContext).close();
		}catch(Throwable t) {
			t.printStackTrace();
		}
	}

}