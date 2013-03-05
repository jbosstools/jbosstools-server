/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
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
	
	// NEW_SERVER_ADAPTER
	public static JBossServerType[] typesInOrder = {
		JBossServerType.AS, 
		JBossServerType.EAP61,
		JBossServerType.UNKNOWN_AS72_PRODUCT,
		JBossServerType.AS72, 
		JBossServerType.JPP6, JBossServerType.EAP6, 
		JBossServerType.UNKNOWN_AS71_PRODUCT, JBossServerType.AS7, 
		JBossServerType.EAP_STD, 
		JBossServerType.SOAP, JBossServerType.SOAP_STD, JBossServerType.EPP, JBossServerType.EAP, 
		JBossServerType.EWP
	};

	private ServerBean bean = null;
	private File rootLocation = null;

	public ServerBeanLoader(File location) {
		rootLocation = location;
	}
	
	public ServerBean getServerBean() {
		if( bean == null )
			loadBeanInternal();
		return bean;
	}

	public JBossServerType getServerType() {
		if( bean == null )
			loadBeanInternal();
		return bean == null ? JBossServerType.UNKNOWN : bean.getType();
	}
	
	private void loadBeanInternal() {
		JBossServerType type = loadTypeInternal(rootLocation);
		String version = null;
		if (!JBossServerType.UNKNOWN.equals(type)) {
			String fullVersion = type.getFullVersion(rootLocation);
			version = getServerVersion(fullVersion);
		} 
		ServerBean server = new ServerBean(rootLocation.getPath(),getName(rootLocation),type,version);
		this.bean = server;
	}
	
	private JBossServerType loadTypeInternal(File location) {
		for( int i = 0; i < typesInOrder.length; i++ ) {
			if( typesInOrder[i].isServerRoot(location))
				return typesInOrder[i];
		}
		return JBossServerType.UNKNOWN;
		
	}
	
	public String getName(File location) {
		return location.getName();
	}
	
	public String getFullServerVersion() {
		if( bean == null )
			loadBeanInternal();
		return bean.getType().getFullVersion(rootLocation);
	}
	
	public String getServerAdapterId() {
		if( bean == null )
			loadBeanInternal();
		return bean.getType().getServerAdapterTypeId(bean.getVersion());
	}
	
	/**
	 * This method does NOT belong here. The two users are both of the 
	 * Condition interface, and so this more likely belongs in 
	 * AbstractCondition
	 * 
	 * @param systemJarFile
	 * @return
	 */
	public static String getFullServerVersionFromZip(File systemJarFile) {
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
				version = props.getProperty("JBossEAP-Release-Version"); //$NON-NLS-1$
				if (version != null) {
					return version;
				}
				version = (String)props.get("Specification-Version");//$NON-NLS-1$
				if (version == null || version.trim().length() == 0 || !(version.charAt(0) >= '0' && version.charAt(0) <= '9')) {
					version = (String)props.get("Implementation-Version");//$NON-NLS-1$
				}
			} catch (IOException e) {
				// It's already null, and would fall through to return null,
				// but hudson doesn't like empty catch blocks.
				return null;  
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
	
	/**
	 * Please use getMajorMinorVersion(version) instead. 
	 * 
	 * There are differences in implementation to be aware of. 
	 * This method will first attempt to match either major.minor, 
	 * or simply major, against all versions listed in 
	 * JBossServerType.UNKNOWN's list of versions. 
	 * 
	 * getMajorMinorVersion(version) will do no such comparisons, 
	 * will not involve JBossServerType.UNKNOWN at all,
	 * and will simply attempt to return a major.minor from the string passed in. 
	 * 
	 * 
	 * @param version
	 * @return
	 */
	@Deprecated
	public static String getServerVersion(String version) {
		if(version==null) 
			return "";//$NON-NLS-1$
		String adapterVersion = getAdapterVersion(version);
		boolean isEmpty = adapterVersion == null || "".equals(adapterVersion);
		return isEmpty ? getMajorMinorVersion(version) : adapterVersion;
	}
	
	/**
	 * Turn a version string into a major.minor version string. 
	 * Example:
	 *    getMajorMinorVersion("4.1.3.Alpha3") -> "4.1"
	 *    
	 * @param version
	 * @return
	 */
	public static String getMajorMinorVersion(String version) {
		if(version==null) 
			return "";//$NON-NLS-1$

		int firstDot = version.indexOf(".");
		int secondDot = firstDot == -1 ? -1 : version.indexOf(".", firstDot + 1);
		if( secondDot != -1) {
			String currentVersion = version.substring(0, secondDot);
			return currentVersion;
		}
		if( firstDot != -1)
			// String only has one ".", and is assumed to be already in "x.y" form
			return version;
		return "";
	}
	
	/**
	 * This method should NOT BE USED. The symantics are unclear,
	 * and it requires checking the JBossServerType.UNKNOWN list of versions, 
	 * which may not be always updated or include all possible version strings. 
	 * 
	 * Furthermore, this method will NOT return a proper server adapter
	 * version which can be used in any way. 
	 * 
	 * To properly discover a server bean's server adapter id, please use:
	 *    JBossServerType.getServerAdapterTypeId(version)
	 */
	@Deprecated
	public static String getAdapterVersion(String version) {
		String[] versions = JBossServerType.UNKNOWN.getVersions();
		//  trying to match adapter version by X.X version
		for (String currentVersion : versions) {
			String pattern = currentVersion.replace(".", "\\.") + ".*";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(version.matches(pattern)) {
				return currentVersion;
			}
		}
		
		// trying to match by major version
		for (String currentVersion : versions) {
			String pattern = currentVersion.substring(0, 2).replace(".", "\\.") + ".*";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(version.matches(pattern)) {
				return currentVersion;
			}
		}
		return "";
	}
}
