package com.tenant.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*******************************************************************************
 * Description : Loads the tenant properties file into a object structure.
 * 
 * ******************************************************************************/

public class TenantsConfiguration {

	private static final String TENANTS_KEY = "tenants";
	
	private final File configFile;
	private final TenantInfo[] tenants;
	
	private String rootLocation;

	public TenantsConfiguration(Properties tenantProperties, String tenantPropertiesPath) {
		this.configFile = new File(tenantPropertiesPath);
		
		String[] tenantNames = tenantProperties.getProperty(TENANTS_KEY, "").split(",");
		if (tenantNames.length == 0) {
			throw new RuntimeException("No tenants configured.");
		}

		List<TenantInfo> tenantList = new ArrayList<TenantInfo>();

		for (String tenantName : tenantNames) {
			
			if (tenantName.length() == 0) {
				throw new RuntimeException("Tenant name is empty.");
			}
			
			System.out.println("Seetting tenant info for tenant: " + tenantName);

			String contextFile = tenantProperties.getProperty(tenantName + ".contextFile", "");
			String[] hosts = tenantProperties.getProperty(tenantName + ".domains", "").split(",");

			// Replace the ${root} placeholder with a value
			contextFile = TenantPropertyPlaceholderConfigurer.fillRootPlaceholder(contextFile, getRootLocation());

			TenantInfo tenantInfo = new TenantInfo(tenantName, contextFile, hosts);
			tenantList.add(tenantInfo);
		}

		tenants = tenantList.toArray(new TenantInfo[tenantList.size()]);
	}
	
	public String getConfigLocation() {
		return configFile.getPath();
	}
	
	/**
	 * Returns the DataCenter root folder path.
	 * Example: "d:/DataCenter_POLAND"
	 * Note there is no file: prior the file path.
	 * @return
	 */
	public String getRootLocation() {
		if(rootLocation == null) {
			rootLocation = configFile.getParent();
			if(rootLocation.startsWith("file:")) {
				rootLocation = rootLocation.substring(5);
			}		
		}
		return rootLocation;
	}
	
	public TenantInfo[] getTenants() {
		return tenants.clone();
	}

	/*******************************************************************************
	 * iGATE Corporation.
	 * 
	 * Project : GSP (Global Sales Platform) 
	 * Program : TenantsConfiguration.java
	 * Author  : iGate 
	 * Date    : Dec-2013
	 * Description : TenantsConfiguration.java file.
	 * 
	 * Revision Log (mm/dd/yy initials description)
	 * -------------------------------------------------------- 
	 * Created Date : 12/12/13 
	 * ******************************************************************************/
	public class TenantInfo {
		private final String name;
		private final String contextFile;
		private final String[] hostNames;

		public TenantInfo(String name, String contextFile, String[] hostNames) {
			this.name = name;
			this.contextFile = contextFile;
			this.hostNames = hostNames.clone();
		}

		public String getName() {
			return name;
		}

		public String getContextFile() {
			return contextFile;
		}

		public String[] getHostNames() {
			return hostNames.clone();
		}
	}

}
