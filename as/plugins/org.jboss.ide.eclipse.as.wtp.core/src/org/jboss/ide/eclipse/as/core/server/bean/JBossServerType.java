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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class JBossServerType implements IJBossToolingConstants {
	
	public static final String JBOSS_AS_PATH = "jboss-as"; //$NON-NLS-1$
	public static final String BIN_PATH = "bin"; //$NON-NLS-1$
	private static final String TWIDDLE_JAR_NAME = "twiddle.jar"; //$NON-NLS-1$
	public static final String RUN_JAR_NAME = "run.jar"; //$NON-NLS-1$
	private static final String JBOSS_ESB_PATH = "jboss-esb"; //$NON-NLS-1$
	private static final String SOAP_JBPM_JPDL_PATH = "jbpm-jpdl"; //$NON-NLS-1$
	private static final String JBOSS_AS_WEB_PATH = "jboss-as-web"; //$NON-NLS-1$
	private static final String JBOSS_PORTLETBRIDGE_PATH = "portletbridge"; //$NON-NLS-1$
	private static final String JBOSS_PORTAL_SAR = "jboss-portal.sar";  //$NON-NLS-1$
	private static final String UNKNOWN_STR = "UNKNOWN"; //$NON-NLS-1$
	
	private String name;
	private String jbossSystemJarPath;
	private String[] versions = new String[0];
	
	private JBossServerType.Condition condition = null;
	private String id=UNKNOWN_STR;
	
	protected JBossServerType(String id, String name, String jbossSystemJarPath, String[] versions, Condition condition) {
		this.id = id;
		this.name = name;
		this.jbossSystemJarPath = jbossSystemJarPath;
		this.versions = versions;
		this.condition = condition;
	}

	public static final JBossServerType AS = new JBossServerType(
			"AS", //$NON-NLS-1$
			"Application Server", //$NON-NLS-1$
			BIN_PATH+File.separatorChar + TWIDDLE_JAR_NAME,
			new String[]{V6_0, V6_1, V5_1, V5_0, V4_2, V4_0, V3_2}, new ASServerTypeCondition());
	
	public static final JBossServerType AS7 = new JBossServerType(
			"AS", //$NON-NLS-1$
			"Application Server", //$NON-NLS-1$
			"modules" + File.separatorChar +  //$NON-NLS-1$
			"org" + File.separatorChar + //$NON-NLS-1$
			"jboss" + File.separatorChar + //$NON-NLS-1$
			"as" + File.separatorChar + //$NON-NLS-1$
			"server" + File.separatorChar + //$NON-NLS-1$
			"main", //$NON-NLS-1$
			new String[]{V7_0,V7_1}, new AS7ServerTypeCondition());
	
	public static final JBossServerType EAP_STD = new JBossServerType(
			"EAP_STD",//$NON-NLS-1$
			"Enterprise Application Platform",//$NON-NLS-1$
			BIN_PATH+ File.separatorChar + TWIDDLE_JAR_NAME, 
			new String[]{V4_2,V4_3,V5_0,V5_1}, new EAPStandaloneServerTypeCondition());
	
	public static final JBossServerType EAP = new JBossServerType(
			"EAP",//$NON-NLS-1$
			"Enterprise Application Platform",//$NON-NLS-1$
			JBOSS_AS_PATH + File.separatorChar + BIN_PATH+ File.separatorChar + TWIDDLE_JAR_NAME, 
			new String[]{V4_2,V4_3,V5_0,V5_1}, new EAPServerTypeCondition());
	
	public static final JBossServerType EAP6 = new JBossServerType(
			"EAP", //$NON-NLS-1$
			"Enterprise Application Platform", //$NON-NLS-1$
				"modules" + File.separatorChar +  //$NON-NLS-1$
				"org" + File.separatorChar + //$NON-NLS-1$
				"jboss" + File.separatorChar + //$NON-NLS-1$
				"as" + File.separatorChar + //$NON-NLS-1$
				"server" + File.separatorChar + //$NON-NLS-1$
				"main", //$NON-NLS-1$
			new String[]{V6_0}, new EAP6ServerTypeCondition());
	
	public static final JBossServerType SOAP = new JBossServerType(
			"SOA-P",//$NON-NLS-1$
			"SOA Platform",//$NON-NLS-1$
			JBOSS_AS_PATH + File.separatorChar + BIN_PATH+ File.separatorChar + TWIDDLE_JAR_NAME,
			new String[]{V4_3, V5_0 }, new SOAPServerTypeCondition());

	public static final JBossServerType SOAP_STD = new JBossServerType(
			"SOA-P-STD",//$NON-NLS-1$
			"SOA Platform Standalone",//$NON-NLS-1$
			JBOSS_ESB_PATH + File.separatorChar + BIN_PATH+ File.separatorChar + RUN_JAR_NAME,
			new String[]{V4_3, V5_0 }, new SOAPStandaloneServerTypeCondition());

	public static final JBossServerType EWP = new JBossServerType( 
			"EWP",//$NON-NLS-1$
			"Enterprise Web Platform",//$NON-NLS-1$
			JBOSS_AS_WEB_PATH + File.separatorChar + BIN_PATH + File.separatorChar + RUN_JAR_NAME,
			new String[]{V5_0 }, new EWPTypeCondition());
	
	public static final JBossServerType EPP = new JBossServerType( 
			"EPP",//$NON-NLS-1$
			"Enterprise Portal Platform",//$NON-NLS-1$
			JBOSS_AS_PATH + File.separatorChar + BIN_PATH + File.separatorChar + RUN_JAR_NAME,
			new String[]{V4_3, V5_0}, new EPPTypeCondition());
	
	public static final JBossServerType UNKNOWN = new JBossServerType(
			UNKNOWN_STR,
			UNKNOWN_STR,
			"",//$NON-NLS-1$
			new String[]{V7_0, V7_1, V6_0, V6_1, V5_1, V5_2, V5_0, V4_3, V4_2, V4_0, V3_2}, null);

	public String toString() {
		return id;
	}
	
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
	
	public String getServerAdapterTypeId(String version) {
		return this.condition.getServerTypeId(version);
	}
	
	private static final String IMPLEMENTATION_TITLE = "Implementation-Title"; //$NON-NLS-1$
	private static final String JBEAP_RELEASE_VERSION = "JBossEAP-Release-Version"; //$NON-NLS-1$
	private static final String JBAS7_RELEASE_VERSION = "JBossAS-Release-Version"; //$NON-NLS-1$
	
	public static boolean isEAP(File systemJarFile) {
		String title = getJarProperty(systemJarFile, IMPLEMENTATION_TITLE);
		return title != null && title.contains("EAP"); //$NON-NLS-1$
	}
	
	public static boolean isEAP6(File systemJarFile) {
		String value = getJarProperty(systemJarFile, JBEAP_RELEASE_VERSION);
		if( value != null && value.trim().startsWith("6.")) //$NON-NLS-1$
				return true;
		return false;
	}
	
	/**
	 * This method will check a jar file for a manifest, and, if it has it, 
	 * find the value for the given property. 
	 * 
	 * If either the manifest or the property are not found, return null
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
				// I would let it fall through, but hudson doesn't like empty catch blocks
				return null; 
			} finally {
				if (jar != null) {
					try {
						jar.close();
					} catch (IOException e) {
						// I would let it fall through, but hudson doesn't like empty catch blocks
						return null;
					}
				}
			}
		} 
		return null;
	}
	
	public static final JBossServerType[] KNOWN_TYPES = {AS, EAP, SOAP, SOAP_STD, EWP, EPP};

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
	}

	public static abstract class AbstractEAPTypeCondition extends AbstractCondition {
		public String getServerTypeId(String version) {
			// V4_2,V4_3,V5_0,V5_1
			if( V4_2.equals(version)) return IJBossToolingConstants.SERVER_EAP_43;
			if( V4_3.equals(version)) return IJBossToolingConstants.SERVER_EAP_43;
			if( V5_0.equals(version)) return IJBossToolingConstants.SERVER_EAP_50;
			if( V5_1.equals(version)) return IJBossToolingConstants.SERVER_EAP_50;
			if( V6_0.equals(version)) return IJBossToolingConstants.SERVER_EAP_60;
			return null;
		}
	}
	
	public static class EAPServerTypeCondition extends AbstractEAPTypeCondition {
		public boolean isServerRoot(File location) {
			File asSystemJar = new File(location, JBossServerType.EAP.getSystemJarPath());
			return asSystemJar.exists() && asSystemJar.isFile();
		}
	}
	
	public static class EAP6ServerTypeCondition extends AbstractEAPTypeCondition {
		public boolean isServerRoot(File location) {
			return getEAP6Version(location, "6.") != null; //$NON-NLS-1$
		}
		public String getFullVersion(File location, File systemJarFile) {
			return getEAP6Version(location, "6."); //$NON-NLS-1$
		}
	}
	
	/**
	 * Get the eap6-style version string, or null if not found 
	 * @param location
	 * @param versionPrefix
	 * @return
	 */
	protected static String getEAP6Version(File location,  String versionPrefix) {
		IPath rootPath = new Path(location.getAbsolutePath());
		IPath productConf = rootPath.append("bin/product.conf"); //$NON-NLS-1$
		if( productConf.toFile().exists()) {
			try {
				Properties p = new Properties();
				p.load(new FileInputStream(productConf.toFile()));
				String product = (String) p.get("slot"); //$NON-NLS-1$
				if("eap".equals(product)) { //$NON-NLS-1$
					IPath eapDir = rootPath.append("modules/org/jboss/as/product/eap/dir/META-INF"); //$NON-NLS-1$
					if( eapDir.toFile().exists()) {
						IPath manifest = eapDir.append("MANIFEST.MF"); //$NON-NLS-1$
						Properties p2 = new Properties();
						p2.load(new FileInputStream(manifest.toFile()));
						String type = p2.getProperty("JBoss-Product-Release-Name"); //$NON-NLS-1$
						String version = p2.getProperty("JBoss-Product-Release-Version"); //$NON-NLS-1$
						if( "EAP".equals(type) && version.startsWith(versionPrefix)) //$NON-NLS-1$
							return version;
					}
				}
			} catch(IOException ioe) {
				// I would let it fall through, but hudson doesn't like empty catch blocks
				return null;
			}
		}
		return null;
	}

	public static class EAPStandaloneServerTypeCondition extends AbstractEAPTypeCondition {
		public boolean isServerRoot(File location) {
			File asSystemJar = new File(location, JBossServerType.EAP_STD.getSystemJarPath());
			if (asSystemJar.exists() && asSystemJar.isFile()) {
				return isEAP(asSystemJar);
			}
			return false;
		}
	}
	
	public static class ASServerTypeCondition extends AbstractCondition {
		public boolean isServerRoot(File location) {
			File asSystemJar = new File(location, JBossServerType.AS.getSystemJarPath());
			if (asSystemJar.exists() && asSystemJar.isFile()) {
				return !isEAP(asSystemJar);
			}
			return false;
		}

		public String getServerTypeId(String version) {
			// V6_0, V6_1, V5_1, V5_0, V4_2, V4_0, V3_2
			if( version.equals(V3_2)) return IJBossToolingConstants.SERVER_AS_32;
			if( version.equals(V4_0)) return IJBossToolingConstants.SERVER_AS_40;
			if( version.equals(V4_2)) return IJBossToolingConstants.SERVER_AS_42;
			if( version.equals(V5_0)) return IJBossToolingConstants.SERVER_AS_50;
			if( version.equals(V5_1)) return IJBossToolingConstants.SERVER_AS_51;
			if( version.equals(V6_0)) return IJBossToolingConstants.SERVER_AS_60;
			if( version.equals(V6_1)) return IJBossToolingConstants.SERVER_AS_60;
			return null;
		}
	}
	
	public static class AS7ServerTypeCondition extends AbstractCondition {
		public boolean isServerRoot(File location) {
			return checkAS7Version(location, JBAS7_RELEASE_VERSION, "7."); //$NON-NLS-1$
		}

		public String getServerTypeId(String version) {
			if( version.equals(V7_0)) return IJBossToolingConstants.SERVER_AS_70;
			if( version.equals(V7_1)) return IJBossToolingConstants.SERVER_AS_71;
			return null;
		}
	}
	
	protected static boolean checkAS7Version(File location, String property, String propPrefix) {
		String mainFolder = JBossServerType.AS7.jbossSystemJarPath;
		File f = new File(location, mainFolder);
		if( f.exists() ) {
			File[] children = f.listFiles();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getName().endsWith(IWTPConstants.EXT_JAR)) {
					String value = getJarProperty(children[i], property);
					if( value != null && value.trim().startsWith(propPrefix))
							return true;
					return false;
				}
			}
		}
		return false;
	}

	public static class SOAPServerTypeCondition extends EAPServerTypeCondition{
		
		public boolean isServerRoot(File location) {
			File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
			return super.isServerRoot(location) && jbpmFolder.exists() && jbpmFolder.isDirectory();
		}
		
		public String getFullVersion(File location, File systemFile) {
			String fullVersion = ServerBeanLoader.getFullServerVersionFromZip(systemFile);
			if (fullVersion != null && fullVersion.startsWith("5.1.1")) { //$NON-NLS-1$
				// SOA-P 5.2
				String runJar = JBossServerType.JBOSS_AS_PATH + File.separatorChar + 
						JBossServerType.BIN_PATH+ File.separatorChar + JBossServerType.RUN_JAR_NAME;
				fullVersion = ServerBeanLoader.getFullServerVersionFromZip(new File(location, runJar));
			}
			return fullVersion;
		}

	}

	public static class SOAPStandaloneServerTypeCondition extends EAPServerTypeCondition {
		
		public boolean isServerRoot(File location) {
			File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
			File soaStdSystemJar = new File(location,JBOSS_ESB_PATH + File.separatorChar + BIN_PATH + File.separatorChar + RUN_JAR_NAME);			
			return 
				jbpmFolder.exists() && jbpmFolder.isDirectory() 
					&& 
				soaStdSystemJar.exists() && soaStdSystemJar.isFile();
		}
	}
	
	public static class EWPTypeCondition extends EAPServerTypeCondition {
		public boolean isServerRoot(File location) {
			File ewpSystemJar = new File(location,JBossServerType.EWP.getSystemJarPath());
			return ewpSystemJar.exists() && ewpSystemJar.isFile();
		}
	}
	
	public static class EPPTypeCondition extends EAPServerTypeCondition {
		public boolean isServerRoot(File location) {
			if( !super.isServerRoot(location))
				return false;
			
			File portletBridgeFolder = new File(location, JBOSS_PORTLETBRIDGE_PATH);
			IJBossRuntimeResourceConstants CONSTANTS = new IJBossRuntimeResourceConstants(){}; 
			File portlalSarFolder = new File(location, JBOSS_AS_PATH + File.separatorChar + CONSTANTS.SERVER + File.separatorChar + CONSTANTS.DEFAULT_CONFIGURATION + File.separatorChar + CONSTANTS.DEPLOY + File.separatorChar + JBOSS_PORTAL_SAR );			
			File asStdSystemJar = new File(location,JBOSS_AS_PATH + File.separatorChar + BIN_PATH + File.separatorChar + RUN_JAR_NAME);			
			return 
				(portletBridgeFolder.exists() && portletBridgeFolder.isDirectory() 
					||
					portlalSarFolder.exists() && portlalSarFolder.isDirectory())
					&& 
				asStdSystemJar.exists() && asStdSystemJar.isFile();
		}
	}

	public String getId() {
		return id;
	}
}