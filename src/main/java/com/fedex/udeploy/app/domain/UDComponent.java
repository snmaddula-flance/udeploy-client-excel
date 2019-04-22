package com.fedex.udeploy.app.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UDComponent {

	private String componentName;
	private String componentDesc;
	private String componentPath;
	private String componentAppName;
	private String componentDeployDir;
	private String componentDiskSpace;
	private String componentSudoUser;

}