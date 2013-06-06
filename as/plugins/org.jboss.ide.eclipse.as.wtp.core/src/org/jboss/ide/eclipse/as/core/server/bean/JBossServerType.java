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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class JBossServerType implements IJBossToolingConstants {
	
	public static final String JBOSS_AS_PATH = "jboss-as"; //$NON-NLS-1$
	public static final String BIN_PATH = "bin"; //$NON-NLS-1$
	public static final String RUN_JAR_NAME = "run.jar"; //$NON-NLS-1$
	public static final String IMPLEMENTATION_TITLE = "Implementation-Title"; //$NON-NLS-1$
	private static final String JBEAP_RELEASE_VERSION = "JBossEAP-Release-Version"; //$NON-NLS-1$
	
	protected static final String UNKNOWN_STR = "UNKNOWN"; //$NON-NLS-1$
	

	// NEW_SERVER_ADAPTER
	public static final JBossServerType AS = new ServerBeanTypeAS();
	public static final JBossServerType AS7 = new ServerBeanTypeAS7();
	public static final JBossServerType EAP_STD = new ServerBeanTypeEAPStandalone();
	public static final JBossServerType EAP = new ServerBeanTypeEAP();
	public static final JBossServerType EAP6 = new ServerBeanTypeEAP6();
	public static final JBossServerType UNKNOWN_AS72_PRODUCT = new ServerBeanTypeUnknownAS72Product();
	public static final JBossServerType AS72 = new ServerBeanTypeAS72();
	public static final JBossServerType EAP61 = new ServerBeanTypeEAP61();
	public static final JBossServerType WILDFLY80 = new ServerBeanTypeWildfly80();
	public static final JBossServerType JPP6 = new ServerBeanTypeJPP6();
	public static final JBossServerType UNKNOWN_AS71_PRODUCT = new ServerBeanTypeUnknownAS71Product();
	public static final JBossServerType SOAP = new ServerBeanTypeSOAP(); 
	public static final JBossServerType SOAP_STD = new ServerBeanTypeSOAPStandalone();
	public static final JBossServerType EWP = new ServerBeanTypeEWP();
	public static final JBossServerType EPP = new ServerBeanTypeEPP();
	public static final JBossServerType AS7GateIn = new ServerBeanTypeAS7GateIn();
	/* Any reason the unknown type needs so many versions? */
	public static final JBossServerType UNKNOWN = new ServerBeanTypeUnknown();

	
	/**
	 * This public variable duplicates the hidden one. 
	 * We shouldn't have to update this in multiple places.
	 */
	public static final JBossServerType[] KNOWN_TYPES = ServerBeanLoader.typesInOrder;

	
	protected String name;
	protected String jbossSystemJarPath;
	protected String[] versions = new String[0];
	protected JBossServerType.Condition condition = null;
	protected String id=UNKNOWN_STR;
	
	protected JBossServerType(String id, String name, String jbossSystemJarPath, String[] versions, Condition condition) {
		this.id = id;
		this.name = name;
		this.jbossSystemJarPath = jbossSystemJarPath;
		this.versions = versions;
		this.condition = condition;
	}
	

	public String getId() {
		return id;
	}
	public String toString() {
		return id;
	}
	
	public String[] getVersions() {
		return versions;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSystemJarPath() {
		return jbossSystemJarPath;
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
	
	
	static interface Condition {
		/**
		 * Is this location the root of an installation?
		 * @param location
		 * @return
		 */
		public boolean isServerRoot(File location);
		
		/**
		 * Get the full version of this server. Provide the system jar / reference file 
		 * as a hint. 
		 * 
		 * @param serverRoot
		 * @param systemFile
		 * @return
		 */
		public String getFullVersion(File serverRoot, File systemFile);
		
		/**
		 * Get the ServerType id associated with this installation
		 * 
		 * @param serverRoot
		 * @param systemFile
		 * @return
		 */
		public String getServerTypeId(String version);

	}
	
	public static abstract class AbstractCondition implements Condition {
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

	
	@Deprecated
	public static class EAPStandaloneServerTypeCondition extends ServerBeanTypeEAPStandalone.EAPStandaloneServerTypeCondition {}
	@Deprecated
	public static class ASServerTypeCondition extends ServerBeanTypeAS.ASServerTypeCondition {}
	@Deprecated
	public static class AS7ServerTypeCondition extends ServerBeanTypeAS7.AS7ServerTypeCondition {}
	@Deprecated
	public static class AS72ServerTypeCondition extends ServerBeanTypeAS72.AS72ServerTypeCondition {}
	@Deprecated
	public static class SOAPServerTypeCondition extends ServerBeanTypeSOAP.SOAPServerTypeCondition{}
	@Deprecated
	public static class SOAPStandaloneServerTypeCondition extends ServerBeanTypeSOAPStandalone.SOAPStandaloneServerTypeCondition {}
	@Deprecated
	public static class EWPTypeCondition extends ServerBeanTypeEWP.EWPTypeCondition {}
	@Deprecated
	public static class EPPTypeCondition extends ServerBeanTypeEPP.EPPTypeCondition {}
	@Deprecated
	public static class EAPServerTypeCondition extends ServerBeanTypeEAP.EAPServerTypeCondition {}
	@Deprecated
	public static class EAP6ServerTypeCondition extends ServerBeanTypeEAP6.EAP6ServerTypeCondition{}
	@Deprecated
	public static class EAP61ServerTypeCondition extends ServerBeanTypeEAP61.EAP61ServerTypeCondition {};
	@Deprecated
	public static class JPP6ServerTypeCondition extends ServerBeanTypeJPP6.JPP6ServerTypeCondition{};
	@Deprecated
	public static class AS7GateInServerTypeCondition extends ServerBeanTypeAS7GateIn.AS7GateInServerTypeCondition {}
	
	
	/* Deprecated methods which are poorly defined  */
	@Deprecated
	public static boolean isEAP(File systemJarFile) {
		String title = getJarProperty(systemJarFile, IMPLEMENTATION_TITLE);
		return title != null && title.contains("EAP"); //$NON-NLS-1$
	}
	
	@Deprecated
	public static boolean isEAP6(File systemJarFile) {
		String value = getJarProperty(systemJarFile, JBEAP_RELEASE_VERSION);
		if( value != null && value.trim().startsWith("6.")) //$NON-NLS-1$
				return true;
		return false;
	}

	@Deprecated
	protected static boolean checkAS7StyleVersion(File location, String mainFolder, String property, String propPrefix) {
		return scanFolderJarsForManifestProp(location, mainFolder, property, propPrefix);
	}
		
	/**
	 * This method is almost impossible to return accurately.
	 * AS7 and AS-5 both have the same JBossServerType.name value, "AS", 
	 * so if a user wishes to get the JBossServerType which corresponds
	 * to AS7, it is impossible for him to do so. 
	 * 
	 * This method really should be deprecated or fixed. 
	 * 
	 * @param name
	 * @return
	 */
	@Deprecated
	public static JBossServerType getType(String name) {
		if(AS.name.equals(name)) {
			return AS;
		} else if(EAP.name.equals(name)) {
			return EAP;
		} else if(SOAP.name.equals(name)) {
			return SOAP;
		} else if(SOAP_STD.name.equals(name)) {
			return SOAP_STD;
		} else if(EWP.name.equals(name)) {
			return EWP;
		} else if(EPP.name.equals(name)) {
			return EPP;
		}
		// TODO externalize
		throw new IllegalArgumentException("Name '" + name + "' cannot be converted to ServerType"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}