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
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.as.core.Messages;

public interface IJBossToolingConstants {
	/* Server runtime types */
	public static final String AS_32 = "org.jboss.ide.eclipse.as.runtime.32"; //$NON-NLS-1$
	public static final String AS_40 = "org.jboss.ide.eclipse.as.runtime.40"; //$NON-NLS-1$
	public static final String AS_42 = "org.jboss.ide.eclipse.as.runtime.42"; //$NON-NLS-1$
	public static final String AS_50 = "org.jboss.ide.eclipse.as.runtime.50"; //$NON-NLS-1$
	public static final String AS_51 = "org.jboss.ide.eclipse.as.runtime.51"; //$NON-NLS-1$
	public static final String AS_60 = "org.jboss.ide.eclipse.as.runtime.60"; //$NON-NLS-1$
	public static final String EAP_43 = "org.jboss.ide.eclipse.as.runtime.eap.43"; //$NON-NLS-1$
	public static final String EAP_50 = "org.jboss.ide.eclipse.as.runtime.eap.50"; //$NON-NLS-1$
	
	public static final String SERVER_AS_32 = "org.jboss.ide.eclipse.as.32"; //$NON-NLS-1$
	public static final String SERVER_AS_40 = "org.jboss.ide.eclipse.as.40"; //$NON-NLS-1$
	public static final String SERVER_AS_42 = "org.jboss.ide.eclipse.as.42"; //$NON-NLS-1$
	public static final String SERVER_AS_50 = "org.jboss.ide.eclipse.as.50"; //$NON-NLS-1$
	public static final String SERVER_AS_51 = "org.jboss.ide.eclipse.as.51"; //$NON-NLS-1$
	public static final String SERVER_AS_60 = "org.jboss.ide.eclipse.as.60"; //$NON-NLS-1$
	public static final String SERVER_EAP_43 = "org.jboss.ide.eclipse.as.eap.43"; //$NON-NLS-1$
	public static final String SERVER_EAP_50 = "org.jboss.ide.eclipse.as.eap.50"; //$NON-NLS-1$
	
	
	
	/* Version Strings */
	public static final String V3_0 = "3.0"; //$NON-NLS-1$
	public static final String V3_2 = "3.2"; //$NON-NLS-1$
	public static final String V4_0 = "4.0"; //$NON-NLS-1$
	public static final String V4_2 = "4.2"; //$NON-NLS-1$
	public static final String V5_0 = "5.0"; //$NON-NLS-1$
	
	
	/* Files or folders inside the TOOLING */
	public static final String LOG = "log"; //$NON-NLS-1$
	public static final String TEMP_DEPLOY = "tempDeploy"; //$NON-NLS-1$
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
	public static final String DEFAULT_PROPS_EAP_43 = "jboss.eap.43.default.ports.properties"; //$NON-NLS-1$
	public static final String DEFAULT_PROPS_EAP_50 = "jboss.eap.50.default.ports.properties"; //$NON-NLS-1$
	
	// Poller constants
	public static final String DEFAULT_STARTUP_POLLER = "org.jboss.ide.eclipse.as.core.runtime.server.JMXPoller"; //$NON-NLS-1$
	public static final String DEFAULT_SHUTDOWN_POLLER = "org.jboss.ide.eclipse.as.core.runtime.server.processTerminatedPoller"; //$NON-NLS-1$
	
	/* 
	 * Property keys stored in the server object
	 */
	public static final String DEPLOYMENT_METHOD = "org.jboss.ide.eclipse.as.core.server.attributes.deploymentMethod"; //$NON-NLS-1$
	public static final String STARTUP_POLLER_KEY = "org.jboss.ide.eclipse.as.core.server.attributes.startupPollerKey"; //$NON-NLS-1$
	public static final String SHUTDOWN_POLLER_KEY = "org.jboss.ide.eclipse.as.core.server.attributes.shutdownPollerKey"; //$NON-NLS-1$
	public static final String SERVER_USERNAME = "org.jboss.ide.eclipse.as.core.server.userName"; //$NON-NLS-1$
	public static final String SERVER_PASSWORD = "org.jboss.ide.eclipse.as.core.server.password"; //$NON-NLS-1$
	public static final String JNDI_PORT = "org.jboss.ide.eclipse.as.core.server.jndiPort"; //$NON-NLS-1$
	public static final String WEB_PORT = "org.jboss.ide.eclipse.as.core.server.webPort"; //$NON-NLS-1$
	public static final String JNDI_PORT_DETECT = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect"; //$NON-NLS-1$
	public static final String WEB_PORT_DETECT= "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect"; //$NON-NLS-1$
	public static final String JNDI_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String WEB_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String JNDI_PORT_DEFAULT_XPATH = Messages.Ports + IPath.SEPARATOR + "JNDI"; //$NON-NLS-1$
	public static final String WEB_PORT_DEFAULT_XPATH = Messages.Ports + IPath.SEPARATOR + "JBoss Web"; //$NON-NLS-1$
	public static final int JNDI_DEFAULT_PORT = 1099;
	public static final int JBOSS_WEB_DEFAULT_PORT = 8080;
	
	
	public static final String LOCAL_DEPLOYMENT_NAME = "name";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_LOC = "location";  //$NON-NLS-1$
	public static final String LOCAL_DEPLOYMENT_TEMP_LOC = "tempLocation";  //$NON-NLS-1$

}
