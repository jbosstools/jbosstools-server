/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.jboss.ide.eclipse.as.core.server.bean;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author eskimo
 *
 */
public class ServerBeanLoader {
	
	public static final String SOAP_JBPM_JPDL_PATH = "jbpm-jpdl";//$NON-NLS-1$

	public ServerBean loadFromLocation(File location) {
		JBossServerType type = getServerType(location);
		String version = null;
		if (!JBossServerType.UNKNOWN.equals(type)) {
			String fullVersion = getFullServerVersion(new File(location,type.getSystemJarPath()));
			if (fullVersion != null && fullVersion.startsWith("5.1.1") && JBossServerType.SOAP.equals(type)) { //$NON-NLS-1$
				// SOA-P 5.2
				String runJar = JBossServerType.JBOSS_AS_PATH + File.separatorChar + 
						JBossServerType.BIN_PATH+ File.separatorChar + JBossServerType.RUN_JAR_NAME;
				fullVersion = getFullServerVersion(new File(location, runJar));
			}
			version = getServerVersion(fullVersion);
		} 
		ServerBean server = new ServerBean(location.getPath(),getName(location),type,version);
		return server;
	}
	
	public JBossServerType getServerType(File location) {
		if(JBossServerType.AS.isServerRoot(location)) {
			return JBossServerType.AS;
		} else if(JBossServerType.AS7.isServerRoot(location)) {
			return JBossServerType.AS7; 
		} else if(JBossServerType.EAP_STD.isServerRoot(location)) {
				return JBossServerType.EAP_STD;
		} else if(JBossServerType.EAP.isServerRoot(location) && JBossServerType.SOAP.isServerRoot(location)) {
			return JBossServerType.SOAP;
		} else if(JBossServerType.SOAP_STD.isServerRoot(location)) {
			return JBossServerType.SOAP_STD;
		} else if(JBossServerType.EAP.isServerRoot(location) && JBossServerType.EPP.isServerRoot(location)) {
			return JBossServerType.EPP;
		} else if(JBossServerType.EAP.isServerRoot(location)) {
			return JBossServerType.EAP;
		} else if(JBossServerType.EWP.isServerRoot(location)) {
			return JBossServerType.EWP;
		}
		return JBossServerType.UNKNOWN;
	}
	
	public String getName(File location) {
		return location.getName();
	}
	
	public String getFullServerVersion(File systemJarFile) {
		if (systemJarFile.isDirectory()) {
			File[] files = systemJarFile.listFiles(new FilenameFilter() {
				
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar"); //$NON-NLS-1$
				}
			});
			if (files != null && files.length == 1) {
				systemJarFile = files[0];
			}
		}
		
		String version = null;
		ZipFile jar = null;
		if(systemJarFile.canRead()) {
			try {
				jar = new ZipFile(systemJarFile);
				ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");//$NON-NLS-1$
				Properties props = new Properties();
				props.load(jar.getInputStream(manifest));
				version = (String)props.get("Specification-Version");//$NON-NLS-1$
			} catch (IOException e) {
				// version = ""
			} finally {
				if (jar != null) {
					try {
						jar.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
		return version;
	}
	
	public String getServerVersion(String version) {
		if(version==null) return "";//$NON-NLS-1$
		String[] versions = JBossServerType.UNKNOWN.getVersions();
		String adapterVersion = "";//$NON-NLS-1$
		//  trying to match adapter version by X.X version
		for (String currentVersion : versions) {
			String pattern = currentVersion.replace(".", "\\.") + ".*";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(version.matches(pattern)) {
				adapterVersion = currentVersion;
				break;
			}
		}
		
		if("".equals(adapterVersion)) {//$NON-NLS-1$
			// trying to match by major version
			for (String currentVersion : versions) {
				String pattern = currentVersion.substring(0, 2).replace(".", "\\.") + ".*";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				if(version.matches(pattern)) {
					adapterVersion = currentVersion;
					break;
				}
			}
		}
		return adapterVersion;
	}
	
	public String getAdapterVersion(String version) {
		String[] versions = JBossServerType.UNKNOWN.getVersions();
		String adapterVersion = "";//$NON-NLS-1$
		//  trying to match adapter version by X.X version
		for (String currentVersion : versions) {
			String pattern = currentVersion.replace(".", "\\.") + ".*";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(version.matches(pattern)) {
				adapterVersion = currentVersion;
				break;
			}
		}
		
		if("".equals(adapterVersion)) { //$NON-NLS-1$
			// trying to match by major version
			for (String currentVersion : versions) {
				String pattern = currentVersion.substring(0, 2).replace(".", "\\.") + ".*";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if(version.matches(pattern)) {
					adapterVersion = currentVersion;
					break;
				}
			}
		}
		return adapterVersion;
	}
}
