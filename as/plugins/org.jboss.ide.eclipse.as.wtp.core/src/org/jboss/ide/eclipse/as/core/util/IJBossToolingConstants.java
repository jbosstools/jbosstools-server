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


/**
 * @noimplement This interface is not intended to be implemented by clients.
 */
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
	public static final String WILDFLY_80 = "org.jboss.ide.eclipse.as.runtime.wildfly.80"; //$NON-NLS-1$
	public static final String WILDFLY_90 = "org.jboss.ide.eclipse.as.runtime.wildfly.90"; //$NON-NLS-1$
	public static final String WILDFLY_100 = "org.jboss.ide.eclipse.as.runtime.wildfly.100"; //$NON-NLS-1$
	public static final String RUNTIME_PREFIX = "org.jboss.ide.eclipse.as.runtime."; //$NON-NLS-1$
	public static final String EAP_RUNTIME_PREFIX = "org.jboss.ide.eclipse.as.runtime.eap."; //$NON-NLS-1$
	public static final String WF_RUNTIME_PREFIX = "org.jboss.ide.eclipse.as.runtime.wildfly."; //$NON-NLS-1$
	public static final String EAP_43 = "org.jboss.ide.eclipse.as.runtime.eap.43"; //$NON-NLS-1$
	public static final String EAP_50 = "org.jboss.ide.eclipse.as.runtime.eap.50"; //$NON-NLS-1$
	public static final String EAP_60 = "org.jboss.ide.eclipse.as.runtime.eap.60"; //$NON-NLS-1$
	public static final String EAP_61 = "org.jboss.ide.eclipse.as.runtime.eap.61"; //$NON-NLS-1$
	public static final String EAP_70 = "org.jboss.ide.eclipse.as.runtime.eap.70"; //$NON-NLS-1$
	public static final String[] ALL_JBOSS_RUNTIMES = new String[] {
		AS_32,AS_40,AS_42,AS_50,AS_51,AS_60,
		AS_70,AS_71,WILDFLY_80,WILDFLY_90,WILDFLY_100,
		EAP_43,EAP_50,EAP_60, EAP_61, EAP_70
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
	public static final String SERVER_WILDFLY_80 = "org.jboss.ide.eclipse.as.wildfly.80"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_90 = "org.jboss.ide.eclipse.as.wildfly.90"; //$NON-NLS-1$
	public static final String SERVER_WILDFLY_100 = "org.jboss.ide.eclipse.as.wildfly.100"; //$NON-NLS-1$
	public static final String WF_SERVER_PREFIX = "org.jboss.ide.eclipse.as.wildfly."; //$NON-NLS-1$
	public static final String EAP_SERVER_PREFIX = "org.jboss.ide.eclipse.as.eap."; //$NON-NLS-1$
	public static final String SERVER_EAP_43 = "org.jboss.ide.eclipse.as.eap.43"; //$NON-NLS-1$
	public static final String SERVER_EAP_50 = "org.jboss.ide.eclipse.as.eap.50"; //$NON-NLS-1$
	public static final String SERVER_EAP_60 = "org.jboss.ide.eclipse.as.eap.60"; //$NON-NLS-1$
	public static final String SERVER_EAP_61 = "org.jboss.ide.eclipse.as.eap.61"; //$NON-NLS-1$
	public static final String SERVER_EAP_70 = "org.jboss.ide.eclipse.as.eap.70"; //$NON-NLS-1$
	public static final String[] ALL_JBOSS_SERVERS = new String[] {
		SERVER_AS_32,SERVER_AS_40,SERVER_AS_42,SERVER_AS_50,SERVER_AS_51,
		SERVER_AS_60,SERVER_AS_70,SERVER_AS_71,
		SERVER_WILDFLY_80,SERVER_WILDFLY_90,SERVER_WILDFLY_100,
		SERVER_EAP_43,SERVER_EAP_50,SERVER_EAP_60, SERVER_EAP_61, SERVER_EAP_70
	};
	// NEW_SERVER_ADAPTER Add the new server id above this line
	
	public static final String DEPLOY_ONLY_RUNTIME = "org.jboss.ide.eclipse.as.runtime.stripped"; //$NON-NLS-1$
	public static final String DEPLOY_ONLY_SERVER = "org.jboss.ide.eclipse.as.systemCopyServer"; //$NON-NLS-1$
	
	
	/* Version Strings.  These are sooo dumb and legacy. Ugh.  */
	public static final String V3_0 = "3.0"; //$NON-NLS-1$
	public static final String V3_2 = "3.2"; //$NON-NLS-1$
	public static final String V3_3 = "3.3"; //$NON-NLS-1$
	public static final String V3_4 = "3.4"; //$NON-NLS-1$
	public static final String V3_5 = "3.5"; //$NON-NLS-1$
	public static final String V4_0 = "4.0"; //$NON-NLS-1$
	public static final String V4_2 = "4.2"; //$NON-NLS-1$
	public static final String V4_3 = "4.3"; //$NON-NLS-1$
	public static final String V5_0 = "5.0"; //$NON-NLS-1$
	public static final String V5_1 = "5.1"; //$NON-NLS-1$
	public static final String V5_2 = "5.2"; //$NON-NLS-1$
	public static final String V5_3 = "5.3"; //$NON-NLS-1$
	public static final String V6_0 = "6.0"; //$NON-NLS-1$
	public static final String V6_1 = "6.1"; //$NON-NLS-1$
	/**
	 * @since 3.0
	 */
	public static final String V6_2 = "6.2"; //$NON-NLS-1$
	public static final String V7_0 = "7.0"; //$NON-NLS-1$
	public static final String V7_1 = "7.1"; //$NON-NLS-1$
	public static final String V7_2 = "7.2"; //$NON-NLS-1$
	public static final String V8_0 = "8.0"; //$NON-NLS-1$
	
	// LEGACY - unused. Previously: Add the new version string above this line
	
	
	
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
	// Above is legacy. 

	
	
	/* Files or folders inside the TOOLING */
	public static final String LOG = "log"; //$NON-NLS-1$
	public static final String TEMP_DEPLOY = "tempDeploy"; //$NON-NLS-1$
	public static final String TEMP_REMOTE_DEPLOY = "tempRemoteDeploy"; //$NON-NLS-1$
	public static final String JBOSSTOOLS_TMP = "jbosstoolsTemp"; //$NON-NLS-1$
	public static final String TMP = "tmp"; //$NON-NLS-1$
	public static final String CONFIG_IN_METADATA = "jbossConfig"; //$NON-NLS-1$
	public static final String XPATH_FILE_NAME = "xpaths.xml"; //$NON-NLS-1$

	
	// Inside the plugin (not metadata)
	/* The following are all deprecated and were never meant for external consumption */
	@Deprecated
	public static final String PROPERTIES = "properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_32 = "jboss.32.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_40 = "jboss.40.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_42 = "jboss.42.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_50 = "jboss.50.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_51 = "jboss.51.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_60 = "jboss.60.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_70 = "jboss.70.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_71 = "jboss.71.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_EAP_43 = "jboss.eap.43.default.ports.properties"; //$NON-NLS-1$
	@Deprecated
	public static final String DEFAULT_PROPS_EAP_50 = "jboss.eap.50.default.ports.properties"; //$NON-NLS-1$
	
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

	@Deprecated
	public static final String WEB_PORT = "org.jboss.ide.eclipse.as.core.server.webPort"; //$NON-NLS-1$
	@Deprecated
	public static final String WEB_PORT_DETECT= "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect"; //$NON-NLS-1$

	
	/*
	 * Should we add deployment scanners on startup?
	 */
	public static final String PROPERTY_ADD_DEPLOYMENT_SCANNERS = "org.jboss.ide.eclipse.as.core.server.addDeploymentScanner";
	/*
	 * Should we remove deployment scanners on shutdown?
	 */
	public static final String PROPERTY_REMOVE_DEPLOYMENT_SCANNERS = "org.jboss.ide.eclipse.as.core.server.removeDeploymentScanner";
	/**
	 * @since 3.0
	 */
	public static final String PROPERTY_SCANNER_TIMEOUT = "org.jboss.ide.eclipse.as.core.server.deploymentscanner.timeout";
	/**
	 * @since 3.0
	 */
	public static final String PROPERTY_SCANNER_INTERVAL= "org.jboss.ide.eclipse.as.core.server.deploymentscanner.interval";
	
	
	
	public static final String IGNORE_LAUNCH_COMMANDS = "org.jboss.ide.eclipse.as.core.server.IGNORE_LAUNCH_COMMANDS"; //$NON-NLS-1$
	public static final String LISTEN_ALL_HOSTS = "org.jboss.ide.eclipse.as.core.server.LISTEN_ON_ALL_HOSTS"; //$NON-NLS-1$

	public static final String EXPOSE_MANAGEMENT_SERVICE = "org.jboss.ide.eclipse.as.core.server.EXPOSE_MANAGEMENT_SERVICE"; //$NON-NLS-1$

	public static final String DEFAULT_DEPLOYMENT_METHOD_TYPE = "local";  //$NON-NLS-1$

	public static final String LOCAL_DEPLOYMENT_NAME = "name";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_LOC = "location";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_TEMP_LOC = "tempLocation";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_OUTPUT_NAME = "outputName"; //$NON-NLS-1$
}
