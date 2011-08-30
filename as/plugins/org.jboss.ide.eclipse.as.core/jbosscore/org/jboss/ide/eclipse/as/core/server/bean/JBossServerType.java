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

import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

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
			new String[]{V6_0,V5_1, V5_0, V4_2, V4_0, V3_2}, new ASServerTypeCondition());
	
	public static final JBossServerType EAP = new JBossServerType(
			"EAP",//$NON-NLS-1$
			"Enterprise Application Platform",//$NON-NLS-1$
			JBOSS_AS_PATH + File.separatorChar + BIN_PATH+ File.separatorChar + TWIDDLE_JAR_NAME, 
			new String[]{V4_2,V4_3,V5_0,V5_1}, new EAPServerTypeCondition());
	
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
			new String[]{V6_0, V6_1, V5_2, V5_1, V5_0, V4_3, V4_2, V4_0, V3_2}, null);

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
	
	public static final JBossServerType[] KNOWN_TYPES = {AS, EAP, SOAP, SOAP_STD, EWP, EPP};

	static interface Condition {
		public boolean isServerRoot(File location);
	}
	
	
	public static class EAPServerTypeCondition implements Condition {
		public boolean isServerRoot(File location) {
			File asSystemJar = new File(location, JBossServerType.EAP.getSystemJarPath());
			return asSystemJar.exists() && asSystemJar.isFile();
		}
	}
	
	public static class ASServerTypeCondition implements Condition {
		
		public boolean isServerRoot(File location) {
			File asSystemJar = new File(location, JBossServerType.AS.getSystemJarPath());
			return asSystemJar.exists() && asSystemJar.isFile();
		}
	}
	
	public static class SOAPServerTypeCondition extends EAPServerTypeCondition{
		
		public boolean isServerRoot(File location) {
			File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
			return super.isServerRoot(location) && jbpmFolder.exists() && jbpmFolder.isDirectory();
		}
	}

	public static class SOAPStandaloneServerTypeCondition implements Condition {
		
		public boolean isServerRoot(File location) {
			File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
			File soaStdSystemJar = new File(location,JBOSS_ESB_PATH + File.separatorChar + BIN_PATH + File.separatorChar + RUN_JAR_NAME);			
			return 
				jbpmFolder.exists() && jbpmFolder.isDirectory() 
					&& 
				soaStdSystemJar.exists() && soaStdSystemJar.isFile();
		}
	}
	
	public static class EWPTypeCondition implements Condition {
		public boolean isServerRoot(File location) {
			File ewpSystemJar = new File(location,JBossServerType.EWP.getSystemJarPath());
			return ewpSystemJar.exists() && ewpSystemJar.isFile();
		}
	}
	
	public static class EPPTypeCondition implements Condition {
		public boolean isServerRoot(File location) {
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