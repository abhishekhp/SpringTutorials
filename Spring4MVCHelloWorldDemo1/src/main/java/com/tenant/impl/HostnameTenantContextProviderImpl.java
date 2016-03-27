package com.tenant.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.tenant.interfaces.ITenantContextProvider;
import com.tenant.impl.TenantsConfiguration;
import com.tenant.impl.TenantsConfiguration.TenantInfo;

/**
 * Resolves the tenant context based on the request domain name.
 */
public class HostnameTenantContextProviderImpl implements ITenantContextProvider {

	@Autowired
	private HttpServletRequest request;
	private final Map<String, String> hostToTenantDispatchServletName;

	public HostnameTenantContextProviderImpl(TenantsConfiguration tenantsConfiguration) {

		hostToTenantDispatchServletName = new HashMap<String, String>();
		for (TenantInfo tenant : tenantsConfiguration.getTenants()) {
			for (String host : tenant.getHostNames()) {
				System.out.println("host:" + host + ", tenant: " + tenant.getName());
				hostToTenantDispatchServletName.put(host, tenant.getName());
			}
		}
	}

	@Override
	public String getTenantCode() {
		String host = request.getHeader("host");
		System.out.println("host: "  + host);
		if (hostToTenantDispatchServletName.containsKey(host)) {
			return hostToTenantDispatchServletName.get(host);
		}
		return null;
	}
}
