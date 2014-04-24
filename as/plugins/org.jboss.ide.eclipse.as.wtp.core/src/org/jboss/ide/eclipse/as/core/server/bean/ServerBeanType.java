/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class ServerBeanType implements IJBossToolingConstants {
	
	protected static final String UNKNOWN_STR = "UNKNOWN"; //$NON-NLS-1$
	public static final ServerBeanType UNKNOWN = new ServerBeanTypeUnknown();

	
	protected String name;
	protected String systemJarPath;
	protected ICondition condition = null;
	protected String id=UNKNOWN_STR;
	
	protected ServerBeanType(String id, String name, String systemJarPath, ICondition condition) {
		this.id = id;
		this.name = name;
		this.systemJarPath = systemJarPath;
		this.condition = condition;
	}
	
	public String getId() {
		return id;
	}
	public String toString() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSystemJarPath() {
		return systemJarPath;
	}
	
	public boolean isServerRoot(File location) {
		return this.condition.isServerRoot(location);
	}
	
	public String getFullVersion(File root) {
		if( this.condition == null )
			return null;
		return this.condition.getFullVersion(root, new File(root, getSystemJarPath()));
	}
	
	/**
	 * This method is for conditions where the underlying server may be 
	 * of a different id than the JBossServerType. For example, any 
	 * JBossServerType which represents an entire class of similar but
	 * not identical servers, the server type may have an id such as 
	 * AS-Product, and this method may return something like "JPP"
	 * 
	 * Note that differs from the method of the same name in 
	 * the AbstractCondition, which will return null if there is no 
	 * underlying type. This method will default to returning the
	 * value of 'id' in the case where there is no different underlying type.  
	 * 
	 * @param location
	 * @param systemFile
	 * @return an underlying type id, or the id of this JBossServerType
	 * 		   if the condition does not provide an underlying type. 
	 * @since 3.0 (actually 2.4.101)
	 */

	public String getUnderlyingTypeId(File root) {
		if( this.condition == null )
			return null;
		String ret = null;
		if( this.condition instanceof AbstractCondition ) {
			ret = ((AbstractCondition)condition).getUnderlyingTypeId(root, new File(root, getSystemJarPath()));
		}
		return ret == null ? id : ret;
	}

	
	/**
	 * This will return a version, if it can be discovered.
	 * If this is an UNKNOWN server bean, the return 
	 * value will be null
	 * 
	 * @param version
	 * @return
	 */
	public String getServerAdapterTypeId(String version) {
		return condition == null ? null :
			this.condition.getServerTypeId(version);
	}
	
	
	/**
	 * Get the relative path from what is the server bean's root
	 * to what would be it's server adapter's root, or null if equal. 
	 * 
	 * @param version
	 * @return
	 */
	public String getRootToAdapterRelativePath(String version) {
		return null;
	}
	
	
	protected static Properties loadProperties(File f) {
		Properties p = new Properties();
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(f); 
			p.load(stream);
			return p;
		} catch(IOException ioe) {
			return p;
		} finally {
			if( stream != null ) {
				try {
					stream.close();
				} catch(IOException ioe) {
					// Do nothing
				}
			}
		}
	}

	protected static String asPath(String... vals) {
		StringBuffer sb = new StringBuffer();
		for ( String v : vals ) {
			sb.append(v);
			sb.append(File.separatorChar);
		}
		String s = sb.toString();
		s = s.substring(0, s.length() - 1);
		return s;
	}
	
	/**
	 * Scans the jars in the folder until a jar with a 
	 * manifest and a matching property key is found.  
	 * 
	 * If the given prefix is a prefix of the property value, 
	 * there is a match, and a 'true' is returned. 
	 * 
	 * Search 
	 * @param location  a root folder
	 * @param mainFolder a path leading to a subfolder of the location
	 * @param property a property to search for in manifest.mf
	 * @param propPrefix a prefix to check against for a match. 
	 * @return true if there is a match, false otherwise. 
	 */
	protected static boolean scanFolderJarsForManifestProp(File location, String mainFolder, String property, String propPrefix) {
	
		File f = new File(location, mainFolder);
		if( f.exists() ) {
			File[] children = f.listFiles();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getName().endsWith(IWTPConstants.EXT_JAR)) {
					String value = getJarProperty(children[i], property);
					if( value != null && value.trim().startsWith(propPrefix))
							return true;
				}
			}
		}
		return false;
	}


	/**
	 * This method will check a jar file for a manifest, and, if it has it, 
	 * find the value for the given property. 
	 * 
	 * If either the jar, manifest or the property are not found, 
	 * return null.
	 * 
	 * @param systemJarFile
	 * @param propertyName
	 * @return
	 */
	public static String getJarProperty(File systemJarFile, String propertyName) {
		if (systemJarFile.canRead()) {
			ZipFile jar = null;
			try {
				jar = new ZipFile(systemJarFile);
				ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");//$NON-NLS-1$
				Properties props = new Properties();
				props.load(jar.getInputStream(manifest));
				String value = (String) props.get(propertyName);
				return value;
			} catch (IOException e) {
				// Intentionally empty
				return null; 
			} finally {
				if (jar != null) {
					try {
						jar.close();
					} catch (IOException e) {
						// Intentionally empty
						return null;
					}
				}
			}
		} 
		return null;
	}
}
