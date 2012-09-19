/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

public interface IJBossToolingConstants {
	/* Server runtime types */
	public static final String AS_32 = "org.jboss.ide.eclipse.as.runtime.32"; //$NON-NLS-1$
	public static final String AS_40 = "org.jboss.ide.eclipse.as.runtime.40"; //$NON-NLS-1$
	public static final String AS_42 = "org.jboss.ide.eclipse.as.runtime.42"; //$NON-NLS-1$
	public static final String AS_50 = "org.jboss.ide.eclipse.as.runtime.50"; //$NON-NLS-1$
	public static final String AS_51 = "org.jboss.ide.eclipse.as.runtime.51"; //$NON-NLS-1$
	public static final String AS_60 = "org.jboss.ide.eclipse.as.runtime.60"; //$NON-NLS-1$
	public static final String AS_70 = "org.jboss.ide.eclipse.as.runtime.70"; //$NON-NLS-1$
	public static final String AS_71 = "org.jboss.ide.eclipse.as.runtime.71"; //$NON-NLS-1$
	public static final String EAP_RUNTIME_PREFIX = "org.jboss.ide.eclipse.as.runtime.eap."; //$NON-NLS-1$
	public static final String EAP_43 = "org.jboss.ide.eclipse.as.runtime.eap.43"; //$NON-NLS-1$
	public static final String EAP_50 = "org.jboss.ide.eclipse.as.runtime.eap.50"; //$NON-NLS-1$
	public static final String EAP_60 = "org.jboss.ide.eclipse.as.runtime.eap.60"; //$NON-NLS-1$
	public static final String[] ALL_JBOSS_RUNTIMES = new String[] {
		AS_32,AS_40,AS_42,AS_50,AS_51,AS_60,AS_70,AS_71,EAP_43,EAP_50,EAP_60
	};
	// NEW_SERVER_ADAPTER Add the new runtime constant above this line
	
	public static final String SERVER_AS_PREFIX = "org.jboss.ide.eclipse.as."; //$NON-NLS-1$
	public static final String SERVER_AS_32 = "org.jboss.ide.eclipse.as.32"; //$NON-NLS-1$
	public static final String SERVER_AS_40 = "org.jboss.ide.eclipse.as.40"; //$NON-NLS-1$
	public static final String SERVER_AS_42 = "org.jboss.ide.eclipse.as.42"; //$NON-NLS-1$
	public static final String SERVER_AS_50 = "org.jboss.ide.eclipse.as.50"; //$NON-NLS-1$
	public static final String SERVER_AS_51 = "org.jboss.ide.eclipse.as.51"; //$NON-NLS-1$
	public static final String SERVER_AS_60 = "org.jboss.ide.eclipse.as.60"; //$NON-NLS-1$
	public static final String SERVER_AS_70 = "org.jboss.ide.eclipse.as.70"; //$NON-NLS-1$
	public static final String SERVER_AS_71 = "org.jboss.ide.eclipse.as.71"; //$NON-NLS-1$
	public static final String EAP_SERVER_PREFIX = "org.jboss.ide.eclipse.as.eap."; //$NON-NLS-1$
	public static final String SERVER_EAP_43 = "org.jboss.ide.eclipse.as.eap.43"; //$NON-NLS-1$
	public static final String SERVER_EAP_50 = "org.jboss.ide.eclipse.as.eap.50"; //$NON-NLS-1$
	public static final String SERVER_EAP_60 = "org.jboss.ide.eclipse.as.eap.60"; //$NON-NLS-1$
	public static final String[] ALL_JBOSS_SERVERS = new String[] {
		SERVER_AS_32,SERVER_AS_40,SERVER_AS_42,SERVER_AS_50,SERVER_AS_51,
		SERVER_AS_60,SERVER_AS_70,SERVER_AS_71,SERVER_EAP_43,SERVER_EAP_50,SERVER_EAP_60
	};
	// NEW_SERVER_ADAPTER Add the new server id above this line
	
	public static final String DEPLOY_ONLY_RUNTIME = "org.jboss.ide.eclipse.as.runtime.stripped"; //$NON-NLS-1$
	public static final String DEPLOY_ONLY_SERVER = "org.jboss.ide.eclipse.as.systemCopyServer"; //$NON-NLS-1$
	
	
	/* Version Strings */
	public static final String V3_0 = "3.0"; //$NON-NLS-1$
	public static final String V3_2 = "3.2"; //$NON-NLS-1$
	public static final String V4_0 = "4.0"; //$NON-NLS-1$
	public static final String V4_2 = "4.2"; //$NON-NLS-1$
	public static final String V4_3 = "4.3"; //$NON-NLS-1$
	public static final String V5_0 = "5.0"; //$NON-NLS-1$
	public static final String V5_1 = "5.1"; //$NON-NLS-1$
	public static final String V5_2 = "5.2"; //$NON-NLS-1$
	public static final String V6_0 = "6.0"; //$NON-NLS-1$
	public static final String V6_1 = "6.1"; //$NON-NLS-1$
	public static final String V7_0 = "7.0"; //$NON-NLS-1$
	public static final String V7_1 = "7.1"; //$NON-NLS-1$
	// NEW_SERVER_ADAPTER  Add the new version string above this line
	
	
	
	/* String constants for download runtime extensions */
	public static final String DOWNLOAD_RT_328 = "org.jboss.tools.runtime.core.as.328"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_405 = "org.jboss.tools.runtime.core.as.405"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_423 = "org.jboss.tools.runtime.core.as.423"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_501 = "org.jboss.tools.runtime.core.as.501"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_510 = "org.jboss.tools.runtime.core.as.510"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_610 = "org.jboss.tools.runtime.core.as.610"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_701 = "org.jboss.tools.runtime.core.as.701"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_702 = "org.jboss.tools.runtime.core.as.702"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_710 = "org.jboss.tools.runtime.core.as.710"; //$NON-NLS-1$
	public static final String DOWNLOAD_RT_711 = "org.jboss.tools.runtime.core.as.711"; //$NON-NLS-1$

	
	/* Files or folders inside the TOOLING */
	public static final String LOG = "log"; //$NON-NLS-1$
	public static final String TEMP_DEPLOY = "tempDeploy"; //$NON-NLS-1$
	public static final String TEMP_REMOTE_DEPLOY = "tempRemoteDeploy"; //$NON-NLS-1$
	public static final String JBOSSTOOLS_TMP = "jbosstoolsTemp"; //$NON-NLS-1$
	public static final String TMP = "tmp"; //$NON-NLS-1$
	public static final String CONFIG_IN_METADATA = "jbossConfig"; //$NON-NLS-1$
	public static final String XPATH_FILE_NAME = "xpaths.xml"; //$NON-NLS-1$

	
	// Inside the plugin (not metadata)
	public static final String PROPERTIES = "properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_32 = "jboss.32.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_40 = "jboss.40.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_42 = "jboss.42.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_50 = "jboss.50.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_51 = "jboss.51.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_60 = "jboss.60.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_70 = "jboss.70.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_71 = "jboss.71.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_EAP_43 = "jboss.eap.43.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_EAP_50 = "jboss.eap.50.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_EAP_60 = "jboss.eap.60.default.ports.properties"; //$NON-NLS-1$
	// NEW_SERVER_ADAPTER Add the new properties file for this server adapter above this line
	
	// Poller constants
	public static final String DEFAULT_STARTUP_POLLER = "org.jboss.ide.eclipse.as.core.runtime.server.WebPoller"; //$NON-NLS-1$
	public static final String DEFAULT_SHUTDOWN_POLLER = "org.jboss.ide.eclipse.as.core.runtime.server.processTerminatedPoller"; //$NON-NLS-1$
	
	/* 
	 * Property keys stored in the server object
	 */
	public static final String STARTUP_POLLER_KEY = "org.jboss.ide.eclipse.as.core.server.attributes.startupPollerKey"; //$NON-NLS-1$
	public static final String SHUTDOWN_POLLER_KEY = "org.jboss.ide.eclipse.as.core.server.attributes.shutdownPollerKey"; //$NON-NLS-1$
	public static final String SERVER_USERNAME = "org.jboss.ide.eclipse.as.core.server.userName"; //$NON-NLS-1$
	public static final String SERVER_PASSWORD = "org.jboss.ide.eclipse.as.core.server.password"; //$NON-NLS-1$
	public static final String JNDI_PORT = "org.jboss.ide.eclipse.as.core.server.jndiPort"; //$NON-NLS-1$
	public static final String JNDI_PORT_DETECT = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect"; //$NON-NLS-1$
	public static final String JNDI_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String JNDI_PORT_DEFAULT_XPATH = Messages.Ports + IPath.SEPARATOR + "JNDI"; //$NON-NLS-1$
	public static final int    JNDI_DEFAULT_PORT = 1099;
	
	public static final String WEB_PORT = "org.jboss.ide.eclipse.as.core.server.webPort"; //$NON-NLS-1$
	public static final String WEB_PORT_DETECT= "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect"; //$NON-NLS-1$
	public static final String WEB_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String WEB_PORT_DEFAULT_XPATH = Messages.Ports + IPath.SEPARATOR + "JBoss Web"; //$NON-NLS-1$
	public static final int    JBOSS_WEB_DEFAULT_PORT = 8080;
	
	public static final String AS7_MANAGEMENT_PORT = "org.jboss.ide.eclipse.as.core.server.as7.managementPort"; //$NON-NLS-1$
	public static final String AS7_MANAGEMENT_PORT_DETECT= "org.jboss.ide.eclipse.as.core.server.as7.managementPortAutoDetect"; //$NON-NLS-1$
	public static final String AS7_MANAGEMENT_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.as7.managementPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String AS7_MANAGEMENT_PORT_DEFAULT_XPATH = Messages.Ports + IPath.SEPARATOR + "JBoss Management"; //$NON-NLS-1$
	public static final int    AS7_MANAGEMENT_PORT_DEFAULT_PORT = 9999;
	
	public static final String IGNORE_LAUNCH_COMMANDS = "org.jboss.ide.eclipse.as.core.server.IGNORE_LAUNCH_COMMANDS"; //$NON-NLS-1$
	public static final String LISTEN_ALL_HOSTS = "org.jboss.ide.eclipse.as.core.server.LISTEN_ON_ALL_HOSTS"; //$NON-NLS-1$

	public static final String EXPOSE_MANAGEMENT_SERVICE = "org.jboss.ide.eclipse.as.core.server.EXPOSE_MANAGEMENT_SERVICE"; //$NON-NLS-1$

	public static final String DEFAULT_DEPLOYMENT_METHOD_TYPE = "local";  //$NON-NLS-1$

	public static final String LOCAL_DEPLOYMENT_NAME = "name";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_LOC = "location";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_TEMP_LOC = "tempLocation";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_OUTPUT_NAME = "outputName"; //$NON-NLS-1$
}
