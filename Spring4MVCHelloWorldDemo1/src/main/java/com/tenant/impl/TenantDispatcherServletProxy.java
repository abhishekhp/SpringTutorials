package com.tenant.impl;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/*******************************************************************************
 * Description : Servlet that is a proxy to ({@link TenantDispatcherServlet}).
 * 
 * ******************************************************************************/

public class TenantDispatcherServletProxy implements Servlet {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantDispatcherServletProxy.class);

	private ServletConfig servletConfig;
	private TenantDispatcherServlet tenantDispatcherServlet;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;

		ServletContext servletContext = servletConfig.getServletContext();
		WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

		tenantDispatcherServlet = appContext.getBean(TenantDispatcherServlet.class);
		if (tenantDispatcherServlet == null) {
			throw new ServletException("Could not find TenantDispatchServlet bean defined in root context.");
		}
		tenantDispatcherServlet.init(servletConfig);
	}

	@Override
	public void destroy() {
		if (tenantDispatcherServlet != null) {
			tenantDispatcherServlet.destroy();
			tenantDispatcherServlet = null;
		}
		servletConfig = null;
	}

	@Override
	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	@Override
	public String getServletInfo() {
		return tenantDispatcherServlet.getServletInfo();
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

		if (tenantDispatcherServlet == null) {
			
			HttpServletResponse res = (HttpServletResponse) response;
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			
		} else {
			tenantDispatcherServlet.service(request, response);
		}
	}
}
