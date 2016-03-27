package com.tenant.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.tenant.interfaces.ITenantPlaceholderResolver;

/*******************************************************************************
 * Description : Configures tenant property placeholder (currently ${tenantRoot} and ${root}).
 * - The ${tenantRoot} is the full path to the tenant directory.
 * - The ${root} is the full path to the DataCenter directory.
 * 
 * ******************************************************************************/

public class TenantPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer 
				implements ApplicationContextAware, ITenantPlaceholderResolver {

	protected static final String ROOT_PLACEHOLDER_NAME = "root";
	protected static final String TENANT_ROOT_PLACEHOLDER_NAME = "tenantRoot";

	protected static final String ROOT_PLACEHOLDER = DEFAULT_PLACEHOLDER_PREFIX + ROOT_PLACEHOLDER_NAME + DEFAULT_PLACEHOLDER_SUFFIX;
	protected static final String TENANT_ROOT_PLACEHOLDER = DEFAULT_PLACEHOLDER_PREFIX + TENANT_ROOT_PLACEHOLDER_NAME + DEFAULT_PLACEHOLDER_SUFFIX;
	
	protected Map<String, String> placeholderToValue;
	protected Map<String, String> placeholderNameToValue;
	
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		if (ctx instanceof TenantXmlWebApplicationContext) {
			TenantXmlWebApplicationContext conf = (TenantXmlWebApplicationContext) ctx;

			String root = conf.getRootFolder();			
			String tenantRoot = conf.getTenantRootFolder();

			this.placeholderToValue = new HashMap<String, String>();
			this.placeholderToValue.put(ROOT_PLACEHOLDER, root);
			this.placeholderToValue.put(TENANT_ROOT_PLACEHOLDER, tenantRoot);

			this.placeholderNameToValue = new HashMap<String, String>();
			this.placeholderNameToValue.put(ROOT_PLACEHOLDER_NAME, root);
			this.placeholderNameToValue.put(TENANT_ROOT_PLACEHOLDER_NAME, tenantRoot);
		}
	}

	/*
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// TODO Auto-generated method stub
		super.postProcessBeanFactory(beanFactory);
		
		String[] names = beanFactory.getBeanDefinitionNames();
		for(String name : names) {
			BeanDefinition bd = beanFactory.getBeanDefinition(name);
			if(bd.getPropertyValues().contains("flowLocations")) {
				PropertyValue value = bd.getPropertyValues().getPropertyValue("flowLocations");
				if(value.getValue() instanceof List) {
					List list = (List) value.getValue();
					//for(FlowLocation location : list) {
					//}
					System.out.print(value.getValue().toString());
				}
			}
		}
	}
	*/
	
	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
		String value = null;

		if(placeholderNameToValue.containsKey(placeholder)) {
			value = placeholderNameToValue.get(placeholder);
		}
		
		// default to the base implementation
		if (value == null) {
			value = super.resolvePlaceholder(placeholder, props);
		}

		return value;
	}

	/* (non-Javadoc)
	 * @see com.metlife.gsp.tenancy.ITenantPlaceholderResolver#resolvePlaceholder(java.lang.String)
	 */
	@Override
	public String resolvePlaceholder(String location) {
		String updatedLocation = location;
		if (updatedLocation.contains(DEFAULT_PLACEHOLDER_PREFIX)) {
			StringBuilder sb = new StringBuilder(updatedLocation);
			for (Entry<String, String> e : placeholderToValue.entrySet()) {
				fillPlaceholder(sb, e.getKey(), e.getValue());
			}
			updatedLocation = sb.toString();
		}
		return updatedLocation;
	}
	
	public static String fillRootPlaceholder(String str, String value) {
		StringBuilder sb = new StringBuilder(str);
		fillPlaceholder(sb, ROOT_PLACEHOLDER, value);
		return sb.toString();
	}
	
	private static void fillPlaceholder(StringBuilder sb, String placeholder, String value) {
		int i = sb.indexOf(placeholder);
		if (i != -1) {
			sb.replace(i, i + placeholder.length(), value);
		}
	}
}
