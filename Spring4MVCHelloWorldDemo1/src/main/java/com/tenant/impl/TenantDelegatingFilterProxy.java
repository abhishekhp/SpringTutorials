package com.tenant.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.GenericFilterBean;

import com.tenant.interfaces.ITenantContextProvider;
import com.tenant.impl.TenantDispatcherServlet;


/**
 * This is a custom filter implementation responsible for looking up the spring
 * security context. The lookup for the spring security context is delegated to
 * the appropriate tenant child context based on the
 * {@link TenantContextProvider}. This implementation should reside in the root
 * context.
 */
public class TenantDelegatingFilterProxy extends GenericFilterBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(TenantDelegatingFilterProxy.class);

	private final TenantDispatcherServlet tenantDispatchServlet;
	private final ITenantContextProvider tenantContextProvider;

	public TenantDelegatingFilterProxy(TenantDispatcherServlet tenantDispatchServlet,
			ITenantContextProvider tenantContextProvider) {

		this.tenantDispatchServlet = tenantDispatchServlet;
		this.tenantContextProvider = tenantContextProvider;
	}

	protected Filter getTenantDelegateFilter(String tenantCode) {
		WebApplicationContext tenantContext = tenantDispatchServlet.getTenantContext(tenantCode);
		if (tenantContext != null) {
			// Lookup the tenant spring security filter
			//Filter filter = tenantContext.getBean("springSecurityFilterChain", Filter.class);
			return tenantContext.getBean("springSecurityFilterChain", Filter.class);
		}
		return null;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest req = (HttpServletRequest) request;

		String tenantCode = tenantContextProvider.getTenantCode();
		if (tenantCode != null) {
			Filter tenantDelegateFilter = getTenantDelegateFilter(tenantCode);
			if (tenantDelegateFilter != null) {
				tenantDelegateFilter.doFilter(request, response, chain);
				return;
			}
		}

		LOGGER.warn("Could not locate delegate filter for request {}.", req.getRequestURL());
		chain.doFilter(request, response);
	}
}
