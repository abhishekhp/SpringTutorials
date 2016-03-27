package com.tenant.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.tenant.interfaces.ITenantContextProvider;
import com.tenant.impl.TenantsConfiguration.TenantInfo;

/*******************************************************************************
 * Description : TenantDispatcherServlet file.
 * 
 * ******************************************************************************/

public class TenantDispatcherServlet implements Servlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TenantDispatcherServlet.class);

	private final TenantsConfiguration tenantsConfiguration;
	private final ITenantContextProvider tenantContextProvider;
	private final Map<String, DispatcherServlet> servlets;

	private ServletConfig servletConfig;

	public TenantDispatcherServlet(TenantsConfiguration tenantsConfiguration,
			ITenantContextProvider tenantContextProvider) {

		this.tenantsConfiguration = tenantsConfiguration;
		this.tenantContextProvider = tenantContextProvider;
		// TM: once dynamic adding and removal of tenants during runtime in implemented this field needs to be synchronized!
		this.servlets = new HashMap<String, DispatcherServlet>();
	}

	public WebApplicationContext getTenantContext(String tenantCode) {
		if (servlets.containsKey(tenantCode)) {
			return servlets.get(tenantCode).getWebApplicationContext();
		}
		return null;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		servletConfig = config;
		initDispatcherServlets();
	}

	@Override
	public void destroy() {
		destroyDispatcherServlets();
		servletConfig = null;
	}

	@Override
	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	@Override
	public String getServletInfo() {
		return getClass().getSimpleName();
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String tenantCode = tenantContextProvider.getTenantCode();
		if (tenantCode != null && servlets.containsKey(tenantCode)) {

			DispatcherServlet tenantServlet = servlets.get(tenantCode);

			//LOGGER.debug("Dispatching request '{}' to tenant ({}).", req.getRequestURI(),  tenantCode);
			System.out.println("Request: " + req.getRequestURI() + " tenant: " + tenantCode);
			try{
				tenantServlet.service(request, response);
			}catch(Exception e){
				//LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
			return;
		}
		System.out.println("Could not locate tenant dispatcher for request: " + req.getRequestURL());
		LOGGER.warn("Could not locate tenant dispatcher servlet for request {}.", req.getRequestURL());
		res.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	private void initDispatcherServlets() {

		System.out.println("Tenant configuration loaded from: " + tenantsConfiguration.getConfigLocation() );
		LOGGER.info("Tenant configuration loaded from {}.", tenantsConfiguration.getConfigLocation());
	  	for (TenantInfo tenant : tenantsConfiguration.getTenants()) {
			try {
				initDispatchServlet(tenant);
			} catch (Exception ex) {
				ex.printStackTrace();
				LOGGER.error("Could not add tenant {}, details {}.", tenant.getName(), ex.toString());
			}
		}
	}

	protected void initDispatchServlet(TenantInfo tenant) throws ServletException {

		ServletContext servletContext = servletConfig.getServletContext();
		String servletName = tenant.getName() + "TenantDispatcher";

		ServletConfigWrapper tenantServletConfig = new ServletConfigWrapper(servletContext, servletName);
		tenantServletConfig.setInitParameter("contextConfigLocation", tenant.getContextFile());
		tenantServletConfig.setInitParameter("contextClass", TenantXmlWebApplicationContext.class.getName());
		System.out.println("Add tenant " + tenant.getName() + " servletName: " + servletName);
		try {
		DispatcherServlet servlet = new DispatcherServlet();
		servlet.init(tenantServletConfig);

		servlets.put(tenant.getName(), servlet);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//LOGGER.info("Added tenant {} dispatch servlet with name {}.", tenant.getName(), servletName);
		System.out.println("Added tenant " + tenant.getName() + " servletName: " + servletName);
	}

	protected void destroyDispatcherServlets() {
		for (DispatcherServlet servlet : servlets.values()) {
			servlet.destroy();
		}
		servlets.clear();
	}

	protected class ServletConfigWrapper implements ServletConfig {

		private final ServletContext servletContext;
		private final String servletName;
		private final Map<String, String> initParams;

		public ServletConfigWrapper(ServletContext servletContext, String servletName) {

			this.servletContext = servletContext;
			this.servletName = servletName;
			this.initParams = new HashMap<String, String>();
		}

		public void setInitParameter(String name, String value) {
			initParams.put(name, value);
		}

		@Override
		public String getInitParameter(String name) {
			return initParams.get(name);
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			return Collections.enumeration(initParams.keySet());
		}

		@Override
		public ServletContext getServletContext() {
			return servletContext;
		}

		@Override
		public String getServletName() {
			return servletName;
		}
	}

}
