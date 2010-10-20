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
		String version = getServerVersion(getFullServerVersion(new File(location,type.getSystemJarPath())));
		ServerBean server = new ServerBean(location.getPath(),getName(location),type,version);
		return server;
	}
	
	public JBossServerType getServerType(File location) {
		if(JBossServerType.AS.isServerRoot(location)) {
			return JBossServerType.AS;
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
		String version = null;
		if(systemJarFile.canRead()) {
			try {
				ZipFile jar = new ZipFile(systemJarFile);
				ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");//$NON-NLS-1$
				Properties props = new Properties();
				props.load(jar.getInputStream(manifest));
				version = (String)props.get("Specification-Version");//$NON-NLS-1$
			} catch (IOException e) {
				// version = ""
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
