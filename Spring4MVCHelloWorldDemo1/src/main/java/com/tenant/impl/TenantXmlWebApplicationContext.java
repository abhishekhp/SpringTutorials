
package com.tenant.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.tenant.interfaces.ITenantPlaceholderResolver;

/*******************************************************************************
 * Description : TenantXmlWebApplicationContext file.
 * 
 * ******************************************************************************/

public class TenantXmlWebApplicationContext extends XmlWebApplicationContext {

	private static final String LIBS_FOLDER = "Libs";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantXmlWebApplicationContext.class);

	private final ClassLoader initialClassLoadeder;
	private TenantClassLoader2 tenantClassLoader;

	private TenantsConfiguration tenantsConfiguration;
	private ITenantPlaceholderResolver tenantPlaceholderResolver;

	public TenantXmlWebApplicationContext() {
		initialClassLoadeder = getClassLoader();
		setAllowBeanDefinitionOverriding(true);
	}

	@Override
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		setupClassLoader();
		super.initBeanDefinitionReader(beanDefinitionReader);
	}

	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.postProcessBeanFactory(beanFactory);
		tenantsConfiguration = beanFactory.getBean(TenantsConfiguration.class);
		tenantPlaceholderResolver = beanFactory.getBean(ITenantPlaceholderResolver.class);
	}

	@Override
	public Resource getResource(String location) {
		String resourceLocation = location;
		if (tenantPlaceholderResolver != null) {
			resourceLocation = tenantPlaceholderResolver.resolvePlaceholder(resourceLocation);
		}
		//Resource resource = super.getResource(location);
		return super.getResource(resourceLocation);
	}
	
	public void setupClassLoader() {
		if (tenantClassLoader == null) {
			tenantClassLoader = new TenantClassLoader2(getTenantLibsFolder(), initialClassLoadeder);
			setClassLoader(tenantClassLoader);
		}
	}

	public void releaseClassLoader() {
		if (tenantClassLoader != null) {
			tenantClassLoader.close();
			tenantClassLoader = null;
			setClassLoader(initialClassLoadeder);
		}
	}

	@Override
	protected void prepareRefresh() {
		releaseClassLoader();
		super.prepareRefresh();
	}

	@Override
	public void close() {
		releaseClassLoader();
		super.close();
	}

	public String getRootFolder() {
		return tenantsConfiguration.getRootLocation();
	}

	private String tenantRootFolder = null;

	public String getTenantRootFolder() {
		if (tenantRootFolder == null) {
			String[] locations = getConfigLocations();
			if (locations.length > 0) {
				// We will only have one tenant specific context xml file.
				tenantRootFolder = extractPath(locations[0]);
			}
		}
		return tenantRootFolder;
	}

	private String tenantLibsFolder = null;

	public String getTenantLibsFolder() {
		if (tenantLibsFolder == null) {
			File tenantLibsFile = new File(getTenantRootFolder(), LIBS_FOLDER);
			tenantLibsFolder = tenantLibsFile.getPath();
		}
		return tenantLibsFolder;
	}

	private String extractPath(String location) {
		// Location sample value will be
		// 'file:d:\DataCenter\TenantA\context.xml'
		String resourceLocation = location;
		File file = new File(resourceLocation);
		resourceLocation = file.getParent();

		// Trim the 'file:' element
		if (resourceLocation.startsWith("file:")) {
			resourceLocation = resourceLocation.substring(5);
		}

		return resourceLocation;
	}
}