package com.tenant.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * Description : ClassLoader that searches for classes in tenant's /Libs folder.
 * This implementation is based on {@link URLClassLoader} but there is a bug in Java SE 1.6 
 * - the class loader holds a lock on open JAR files thus not allowing us to replace the files.
 ******************************************************************************/

public class TenantClassLoader extends URLClassLoader {
	
	private final String tenantLibsFolder;
	private final String name;
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantClassLoader.class);
	public TenantClassLoader(String tenantLibsFolder, ClassLoader parent) {
		super(scanFolder(tenantLibsFolder), parent);

		this.tenantLibsFolder = tenantLibsFolder;
		this.name = String.format("TenantClassLoader (%s); parent loader: %s",
				tenantLibsFolder, parent.toString());
	}

	/**
	 * Closes all open jar files
	 * http://stackoverflow.com/questions/3216780/problem
	 * -reloading-a-jar-using-urlclassloader
	 * http://management-platform.blogspot.
	 * com/2009/01/classloaders-keeping-jar-files-open.html
	 */
	public void close() {
		return;
		/*
		try {
			Class clazz = java.net.URLClassLoader.class;
			java.lang.reflect.Field ucp = clazz.getDeclaredField("ucp");
			ucp.setAccessible(true);
			Object sun_misc_URLClassPath = ucp.get(this);
			java.lang.reflect.Field loaders = sun_misc_URLClassPath.getClass()
					.getDeclaredField("loaders");
			loaders.setAccessible(true);
			Object java_util_Collection = loaders.get(sun_misc_URLClassPath);
			for (Object sun_misc_URLClassPath_JarLoader : ((java.util.Collection) java_util_Collection)
					.toArray()) {
				try {
					java.lang.reflect.Field loader = sun_misc_URLClassPath_JarLoader
							.getClass().getDeclaredField("jar");
					loader.setAccessible(true);
					Object java_util_jar_JarFile = loader
							.get(sun_misc_URLClassPath_JarLoader);
					((java.util.jar.JarFile) java_util_jar_JarFile).close();
				} catch (Throwable t) {
					// if we got this far, this is probably not a JAR loader so
					// skip it
				}
			}
		} catch (Throwable t) {
			// probably not a SUN VM
		}
		*/
	}

	private static URL[] scanFolder(String tenantLibsFolder) {

		List<URL> urls = new ArrayList<URL>();
		File libs = new File(tenantLibsFolder);
		for (File lib : libs.listFiles()) {
			try {
				if (lib.getName().endsWith(".jar")) {
					URL libURL = new URL("file:" + lib.getAbsolutePath());
					urls.add(libURL);
				}
			} catch (MalformedURLException e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		return urls.toArray(new URL[urls.size()]);
	}

	public String getTenantLibsFolder() {
		return tenantLibsFolder;
	}

	@Override
	public String toString() {
		return name;
	}
}
