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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.server.jbossmodules.LayeredModulePathFactory;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class ServerBeanType {
	
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
		String value = getManifestPropFromFolderJars(location, mainFolder, property);
		if( value != null && value.trim().startsWith(propPrefix))
			return true;
		return false;
	}

	protected static String getManifestPropFromFolderJars(File location, String mainFolder, String property) {
		File f = new File(location, mainFolder);
		if( f.exists() ) {
			File[] children = f.listFiles();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getName().endsWith(IWTPConstants.EXT_JAR)) {
					return getJarProperty(children[i], property);
				}
			}
		}
		return null;
	}

	

	protected static boolean scanManifestPropFromJBossModulesFolder(File[] moduleRoots, String moduleId, String slot, String property, String propPrefix) {
		String value = getManifestPropFromJBossModulesFolder(moduleRoots, moduleId, slot, property);
		if( value != null && value.trim().startsWith(propPrefix))
			return true;
		return false;
	}
	
	protected static String getManifestPropFromJBossModulesFolder(File[] moduleRoots, String moduleId, String slot, String property) {
		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(moduleRoots);
		for( int i = 0; i < layeredRoots.length; i++ ) {
			IPath[] manifests = getFilesForModule(layeredRoots[i], moduleId, slot, manifestFilter());
			if( manifests.length > 0 ) {
				String value = getManifestProperty(manifests[0].toFile(), property);
				if( value != null )
					return value;
				return null;
			}
		}
		return null;
	}
	
	protected static boolean scanManifestPropFromJBossModules(File[] moduleRoots, String moduleId, String slot, String property, String propPrefix) {
		String value = getManifestPropFromJBossModules(moduleRoots, moduleId, slot, property);
		if( value != null && value.trim().startsWith(propPrefix))
			return true;
		return false;
	}
	
	protected static String getManifestPropFromJBossModules(File[] moduleRoots, String moduleId, String slot, String property) {
		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(moduleRoots);
		for( int i = 0; i < layeredRoots.length; i++ ) {
			IPath[] jars = getFilesForModule(layeredRoots[i], moduleId, slot, jarFilter());
			if( jars.length > 0 ) {
				String value = getJarProperty(jars[0].toFile(), property);
				return value;
			}
		}
		return null;
	}

	
	private static FileFilter jarFilter() {
		return new FileFilter() {
			public boolean accept(File pathname) {
				if( pathname.isFile() && pathname.getName().endsWith(".jar")) {
					return true;
				}
				return false;
			} 
		};
	}
	private static FileFilter manifestFilter() {
		return new FileFilter() {
			public boolean accept(File pathname) {
				if( pathname.isFile() && pathname.getName().toLowerCase().equals("manifest.mf")) {
					return true;
				}
				return false;
			} 
		};
	}
	
	private static IPath[] getFilesForModule(File modulesFolder, String moduleName, String slot, FileFilter filter) {
		String slashed = moduleName.replaceAll("\\.", "/");
		slot = (slot == null ? "main" : slot);
		return getFiles(modulesFolder, new Path(slashed).append(slot), filter);
	}
	private static IPath[] getFiles(File modulesFolder, IPath moduleRelativePath, FileFilter filter) {
		File[] layeredPaths = LayeredModulePathFactory.resolveLayeredModulePath(modulesFolder);
		for( int i = 0; i < layeredPaths.length; i++ ) {
			IPath lay = new Path(layeredPaths[i].getAbsolutePath());
			File layeredPath = new File(lay.append(moduleRelativePath).toOSString());
			if( layeredPath.exists()) {
				return getFilesFrom(layeredPath, filter);
			}
		}
		return new IPath[0];
	}

	
	private static IPath[] getFilesFrom(File layeredPath, FileFilter filter) {
		ArrayList<IPath> list = new ArrayList<IPath>();
		File[] children = layeredPath.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( filter.accept(children[i])) {
				list.add(new Path(children[i].getAbsolutePath()));
			}
		}
		return (IPath[]) list.toArray(new IPath[list.size()]);
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
	

	private static String getManifestProperty(File manifestFile, String propertyName) {
		try {
			String contents = FileUtil.getContents(manifestFile);
			if( contents != null ) {
				Manifest mf = new Manifest(new ByteArrayInputStream(contents.getBytes()));
				Attributes a = mf.getMainAttributes();
				String val = a.getValue(propertyName);
				return val;
			}
		} catch(IOException ioe) {
			// 
		}
		return null;
	}
	
	/**
	 * Get a name for this server bean. The default implementation 
	 * returns only the name of the folder, though
	 * subclasses may override this in cases where the default
	 * value does not seem to make sense.
	 * @param root
	 * @Since 3.0
	 * @return
	 */
	public String getServerBeanName(File root) {
		return root.getName();
	}
}
