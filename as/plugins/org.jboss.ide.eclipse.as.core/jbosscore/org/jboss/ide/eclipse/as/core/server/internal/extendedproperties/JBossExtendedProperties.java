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
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.JBossLT6ModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

/**
 * The superclass containing most functionality, to be overridden as necessary.
 * The contents of this are all sorts of errata that do not really fit anywhere
 * else, but need to be customized on a per-server or per-server-type basis
 *
 */
public class JBossExtendedProperties extends ServerExtendedProperties {
	public JBossExtendedProperties(IAdaptable adaptable) {
		super(adaptable);
	}

	public boolean runtimeSupportsBindingToAllInterfaces() {
		return true;
	}

	public boolean runtimeSupportsExposingManagement() {
		return false;
	}

	protected ServerBeanLoader getServerBeanLoader() {
		return runtime == null ? null : new ServerBeanLoader(runtime.getLocation().toFile());
	}
	
	/**
	 * Returns the full path of a local server's server/{config}/deploy folder
	 */
	public String getServerDeployLocation() {
		String original = ConfigNameResolver.getVariablePattern(ConfigNameResolver.JBOSS_CONFIG_DIR) +
				"/" + IJBossRuntimeResourceConstants.DEPLOY;  //$NON-NLS-1$
		return new ConfigNameResolver().performSubstitutions(
				original, server.getName());
	}

	public int getJMXProviderType() {
		return JMX_OVER_JNDI_PROVIDER;
	}
	
	public boolean hasWelcomePage() {
		return true;
	}
	
	protected static final String WELCOME_PAGE_URL_PATTERN = "http://{0}:{1}/"; //$NON-NLS-1$
	public String getWelcomePageUrl() {
		try {
			JBossServer jbossServer = ServerUtil.checkedGetServerAdapter(server, JBossServer.class);
			String host = jbossServer.getHost();
			host = ServerUtil.formatPossibleIpv6Address(host);
			int webPort = jbossServer.getJBossWebPort();
			String consoleUrl = MessageFormat.format(WELCOME_PAGE_URL_PATTERN, host, String.valueOf(webPort));
			return consoleUrl;
		} catch(CoreException ce) {
			return null;
		}
	}

	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_JMX_SUPPORT;
	}

	public IStatus verifyServerStructure() {
		try {
			String e = getVerifyStructureErrorMessage();
			if( e != null )
				return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e);
		} catch(CoreException ce ) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}
	
	protected String getVerifyStructureErrorMessage() throws CoreException{
		if( server.getRuntime() == null ) 
			return NLS.bind(Messages.ServerMissingRuntime, server.getName());
		if( !server.getRuntime().getLocation().toFile().exists())
			return NLS.bind(Messages.RuntimeFolderDoesNotExist, server.getRuntime().getLocation().toOSString());
		JBossServer jbossServer = ServerUtil.checkedGetServerAdapter(server, JBossServer.class);
		if( !new File(jbossServer.getConfigDirectory()).exists()) 
			return NLS.bind(Messages.JBossConfigurationFolderDoesNotExist, jbossServer.getConfigDirectory());
		return null;
	}

	public boolean canVerifyRemoteModuleState() {
		return true;
	}
	
	public IServerModuleStateVerifier getModuleStateVerifier() {
		return new JBossLT6ModuleStateVerifier();
	}

}
