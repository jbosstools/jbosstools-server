/******************************************************************************* 
 * Copyright (c) 2012-2014 Red Hat, Inc. 
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


/**
 * @author eskimo
 *
 */
public class ServerBeanLoader {
	

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

	public ServerBeanType getServerBeanType() {
		if( bean == null )
			loadBeanInternal();
		return bean == null ? ServerBeanType.UNKNOWN : bean.getBeanType();
	}

	
	private void loadBeanInternal() {
		ServerBeanType type = loadTypeInternal(rootLocation);
		String version = null;
		if (!ServerBeanType.UNKNOWN.equals(type)) {
			version = type.getFullVersion(rootLocation);
		} 
		ServerBean server = new ServerBean(rootLocation.getPath(),getName(rootLocation),type,version);
		this.bean = server;
	}
	
	private ServerBeanType loadTypeInternal(File location) {
		ServerBeanType[] all = ServerBeanExtensionManager.getDefault().getAllTypes();
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].isServerRoot(location))
				return all[i];
		}
		return ServerBeanType.UNKNOWN;
		
	}
	
	public String getName(File location) {
		return location.getName();
	}
	
	public String getFullServerVersion() {
		if( bean == null )
			loadBeanInternal();
		return bean.getFullVersion();
	}
	
	/**
	 * Get a string representation of this bean's 
	 * server type. This will usually be equivalent to 
	 * getServerType().getId(),  but may be overridden 
	 * in some cases that require additional differentiation. 
	 * 
	 * @return an org.eclipse.wst.server.core.IServerType's type id
	 * @since 3.0 (actually 2.4.101)
	 */
	public String getUnderlyingTypeId() {
		if( bean == null )
			loadBeanInternal();
		return bean.getUnderlyingTypeId();
	}
	
	
	/**
	 * Get a server type id corresponding to an org.eclipse.wst.server.core.IServerType
	 * that matches with this server bean's root location. 
	 * 
	 * @return an org.eclipse.wst.server.core.IServerType's type id
	 */
	public String getServerAdapterId() {
		if( bean == null )
			loadBeanInternal();
		return bean.getServerAdapterTypeId();
	}
	
	/**
	 * This method is deprecated and should not be used, 
	 * nor should it's replacement be used. 
	 * 
	 * The replacement method, AbstractCondition.getFullServerVersionFromZipLegacy, 
	 * should only be used by legacy code. 
	 * 
	 * @param systemJarFile
	 * @return
	 */
	public static String getFullServerVersionFromZip(File systemJarFile) {
		return AbstractCondition.getFullServerVersionFromZipLegacy(systemJarFile);
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
	
	
	/*
	 * Legacy code Lives Here... beware!
	 */
	

	// NEW_SERVER_ADAPTER
	@Deprecated
	public static JBossServerType[] typesInOrder = JBossServerType.KNOWN_TYPES;
	
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
	 * Deprecated, please use getServerBeanType();
	 * @return
	 */
	@Deprecated
	public JBossServerType getServerType() {
		getServerBeanType();
		return bean == null ? JBossServerType.UNKNOWN : bean.getType();
	}
	

}
