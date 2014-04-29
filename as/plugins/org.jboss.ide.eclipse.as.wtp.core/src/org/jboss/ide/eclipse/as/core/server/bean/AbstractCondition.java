/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
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

public abstract class AbstractCondition implements ICondition {
	
	/**
	 * This method is for conditions where the underlying server may be 
	 * of a different id than the JBossServerType. For example, any 
	 * JBossServerType which represents an entire class of similar but
	 * not identical servers, the server type may have an id such as 
	 * AS-Product, and this method may return something like "JPP"
	 * 
	 * @param location
	 * @param systemFile
	 * @return an ID, or null if the JBossServerType does not represent a class of different types.
	 * @since 3.0 (actually 2.4.101)
	 */
	public String getUnderlyingTypeId(File location, File systemFile) {
		return null;
	}
	
	public String getFullVersion(File location, File systemFile) {
		return ServerBeanLoader.getFullServerVersionFromZip(systemFile);
	}
	
	/**
	 * This method is an older implementation on how to discover 
	 * the version of your server type. 
	 * 
	 * Only legacy code should call this. All new clients 
	 * should properly implement their own method. The method
	 * is still public for legacy and backwards compatability reasons.
	 * 
	 * @param systemJarFile
	 * @return
	 */
	public static String getFullServerVersionFromZipLegacy(File systemJarFile) {
		return getFullServerVersionFromZipLegacy(systemJarFile, new String[]{"Bundle-Version"});
	}
	
	
	public static String getFullServerVersionFromZipLegacy(File systemJarFile, String[] manifestAttributes) {

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
				
				for( int i = 0; i < manifestAttributes.length; i++ ) {
					version = props.getProperty(manifestAttributes[i]); //$NON-NLS-1$
					if (version != null && version.trim().length() > 0) {
						return version;
					}
					version = (String)props.get(manifestAttributes[i]);
					if (version != null && version.trim().length() > 0) {
						return version;
					}
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
}