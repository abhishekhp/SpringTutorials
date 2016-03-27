package com.tenant.impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 * Description : ClassLoader that searches for classes in tenant's /Libs folder.
 * 
 ******************************************************************************/

public class TenantClassLoader2 extends ClassLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TenantClassLoader2.class);

	private final String tenantLibsFolder;
	private final String name;

	private List<ZipFile> jarFiles;

	public TenantClassLoader2(String tenantLibsFolder, ClassLoader parent) {
		super(parent);
		this.jarFiles = new ArrayList<ZipFile>();
		this.tenantLibsFolder = tenantLibsFolder;
		this.name = String.format("TenantClassLoader (%s); parent loader: %s", tenantLibsFolder, parent.toString());
		scanFolder();
	}

	public Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] b = loadClassData(name);
		//Class<?> clazz = defineClass(name, b, 0, b.length);
		return defineClass(name, b, 0, b.length);		
	}

	/**
	 * Tries to find the requested class (*.class) file inside the known JAR files present in the
	 * tenant Libs folder.
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	private byte[] loadClassData(String name) throws ClassNotFoundException {
		String entryName = name.replace('.', '/').concat(".class");
		for (ZipFile jar : jarFiles) {
			ZipEntry entry = jar.getEntry(entryName);
			if (entry != null && !entry.isDirectory()) {
				try {
					return loadClassData(jar, entry);
				} catch (IOException e) {
					String msg = String.format("Exception while reading class '%s' bytecode in JAR file '%s'.",
							entryName, jar.getName());
					LOGGER.error(msg, e);
					break;
				}
			}
		}
		throw new ClassNotFoundException();
	}

	private byte[] loadClassData(ZipFile jar, ZipEntry entry) throws IOException {
		InputStream in = null;
		try {
			in = jar.getInputStream(entry);
			DataInputStream dataIs = new DataInputStream(in);
			byte[] result = new byte[(int) entry.getSize()];
			dataIs.readFully(result);
			result.clone();
			return result;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage(),e);
				
			}
		}
	}

	/**
	 * Closes all open jar files. Note that after that operation this
	 * ClassLoader will not be able to resolve any further classes, thus
	 * resulting in {@link ClassNotFoundException}.
	 */
	public void close() {
		List<ZipFile> filesToClose = new ArrayList<ZipFile>(jarFiles);
		jarFiles = new ArrayList<ZipFile>();
		for (ZipFile zipFile : filesToClose) {
			try {
				zipFile.close();
			} catch (IOException e) {
				String msg = String.format("Exception while closing the JAR file '%s'.", zipFile.getName());
				LOGGER.error(msg, e);
			}
		}
	}

	/**
	 * Lists all the jar files present in the tenant Libs folder and adds them to this class loader.
	 */
	private void scanFolder() {

		File libs = new File(tenantLibsFolder);
		for (File lib : libs.listFiles()) {
			if (lib.isFile() && lib.getName().endsWith(".jar")) {
				try {
					jarFiles.add(new ZipFile(lib));
				} catch (Exception e) {
					String msg = String.format("Exception while trying to open JAR file '%s'.", lib.toString());
					LOGGER.error(msg, e);
				}
			}
		}
	}

	/**
	 * Get the tenant libs folder that this ClassLoader works on.
	 * 
	 * @return
	 */
	public String getTenantLibsFolder() {
		return tenantLibsFolder;
	}

	@Override
	public String toString() {
		return name;
	}
}