/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

/**
 * 
 * @author Rob Stryker
 *
 */
public interface IJBossServerConstants {
	// Metadata folders
	public static final String LOG = "log";
	public static final String TEMP_DEPLOY = "tempDeploy";
	public static final String JBOSSTOOLS_TMP = "jbosstoolsTemp";
	public static final String TMP = "tmp";
	public static final String CONFIG_IN_METADATA = "jbossConfig";
	
	
	// Preference codes
	public static final String USE_METADATA_CONFIG = "IJBossServerConstants.useMetadataConfig";
	
	// Launch configuration constants
	public static final String JBOSS_SERVER_HOME_DIR = "jboss.server.home.dir";
	public static final String JBOSS_SERVER_BASE_DIR = "jboss.server.base.dir";
	public static final String JBOSS_SERVER_NAME = "jboss.server.name";
	public static final String JBOSS_HOME_DIR = "jboss.home.dir";
	
	// Folder constants
	public static final String DEFAULT_SERVER_NAME = "default";
	public static final String DEPLOY = "deploy";
	public static final String SERVER = "server";
	public static final String WORK = "work";
	public static final String DATA = "data";
//	public static final String TMP = "tmp"; // repeat
//	public static final String LOG = "log"; // repeat
	public static final String[] JBOSS_TEMPORARY_FOLDERS = new String[] { WORK, DATA, TMP, LOG};
	
	// Property keys stored in the server object
	public static final String SERVER_USERNAME = "org.jboss.ide.eclipse.as.core.server.userName";
	public static final String SERVER_PASSWORD = "org.jboss.ide.eclipse.as.core.server.password";
	public static final String JNDI_PORT = "org.jboss.ide.eclipse.as.core.server.jndiPort";
	public static final String WEB_PORT = "org.jboss.ide.eclipse.as.core.server.webPort";
	public static final String JNDI_PORT_DETECT = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect";
	public static final String WEB_PORT_DETECT= "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect";
	public static final String JNDI_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect.XPath";
	public static final String WEB_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect.XPath";
	public static final String JNDI_PORT_DEFAULT_XPATH = "Ports" + Path.SEPARATOR + "JNDI";
	public static final String WEB_PORT_DEFAULT_XPATH = "Ports" + Path.SEPARATOR + "JBoss Web";
	public static final int JNDI_DEFAULT_PORT = 1099;
	public static final int JBOSS_WEB_DEFAULT_PORT = 8080;

	
	// Poller constants
	public static final String STARTUP_POLLER_KEY = "org.jboss.ide.eclipse.as.core.server.attributes.startupPollerKey";
	public static final String SHUTDOWN_POLLER_KEY = "org.jboss.ide.eclipse.as.core.server.attributes.shutdownPollerKey";
	public static final String DEFAULT_STARTUP_POLLER = "org.jboss.ide.eclipse.as.core.runtime.server.JMXPoller";
	public static final String DEFAULT_SHUTDOWN_POLLER = "org.jboss.ide.eclipse.as.core.runtime.server.processTerminatedPoller";

}
