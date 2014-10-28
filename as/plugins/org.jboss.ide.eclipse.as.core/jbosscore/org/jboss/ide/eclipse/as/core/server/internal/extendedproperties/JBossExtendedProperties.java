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
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.resolvers.RuntimeVariableResolver;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;
import org.jboss.ide.eclipse.as.core.server.IDeploymentScannerModifier;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBean;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.JBossLT6ModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JMXServerDeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;

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
	
	/* 
	 * Get the version string for this runtime type. 
	 * Some subclasses may choose to respond with a .x suffix. 
	 */
	public String getRuntimeTypeVersionString() {
		return runtime.getRuntimeType().getVersion();
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
	 * or standalone/deployments folder depending on configuration location
	 */
	public String getServerDeployLocation() {
		String original = ConfigNameResolver.getVariablePattern(ConfigNameResolver.JBOSS_CONFIG_DIR) +
				"/" + IJBossRuntimeResourceConstants.DEPLOY;  //$NON-NLS-1$
		 RuntimeVariableResolver resolver = new RuntimeVariableResolver(runtime);
		 ExpressionResolver process = new ExpressionResolver(resolver);
		 return process.resolve(original);
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
	
	public IDeploymentScannerModifier getDeploymentScannerModifier() {
		return new JMXServerDeploymentScannerAdditions();
	}
	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		// to avoid making too many classes with one method, 
		// we'll handle below 6 here. Otherwise we need another 
		// almost-empty extended properties class
		String[] as5Types = new String[]{
				IJBossToolingConstants.AS_50,
				IJBossToolingConstants.AS_51,
				IJBossToolingConstants.EAP_50,
				IJBossToolingConstants.SERVER_AS_50,
				IJBossToolingConstants.SERVER_AS_51,
				IJBossToolingConstants.SERVER_EAP_50,
				
		};
		boolean isAS5 = false;
		String serverType = (server == null ? null : server.getServerType() == null ? null : server.getServerType().getId());
		String rtType = (runtime == null ? null : runtime.getRuntimeType() == null ? null : runtime.getRuntimeType().getId());
		for( int i = 0; i < as5Types.length; i++ ) {
			if( as5Types[i].equals(serverType) || as5Types[i].equals(rtType)) {
				isAS5 = true;
				break;
			}
		}
		
		// IF we're AS 5, return the 5x args
		if( isAS5 ) {
			// Special case workaround for soa-p 5.3.1
			ServerBean sb = new ServerBeanLoader(runtime.getLocation().toFile()).getServerBean();
			if( sb.getBeanType().getId().equals(JBossServerType.EAP_STD.getId())) {
				// load from the parent folder
				sb = new ServerBeanLoader(runtime.getLocation().toFile().getParentFile()).getServerBean();
				if( sb != null && JBossServerType.SOAP.getId().equals(sb.getBeanType().getId()) && sb.getVersion().startsWith("5.")) {  //$NON-NLS-1$
					if( server != null)
						return new JBossSoa5xDefaultLaunchArguments(server);
					return new JBossSoa5xDefaultLaunchArguments(runtime);
					
				}
			}
			
			if( server != null)
				return new JBoss5xDefaultLaunchArguments(server);
			return new JBoss5xDefaultLaunchArguments(runtime);
		}
		
		// else return the < 5 launch args
		if( server != null)
			return new JBossDefaultLaunchArguments(server);
		return new JBossDefaultLaunchArguments(runtime);
	}
	
	public boolean requiresJDK() {
		return false;
	}
	
	public int getFileStructure() {
		return FILE_STRUCTURE_SERVER_CONFIG_DEPLOY;
	}
	
	/**
	 * This is being used to indicate the MINIMUM execution environment, 
	 * not just the default!
	 * 
	 * @param rtType
	 * @return
	 */
	public IExecutionEnvironment getDefaultExecutionEnvironment() {
		// NEW_SERVER_ADAPTER  Subclasses override this
		return EnvironmentsManager.getDefault().getEnvironment("J2SE-1.4"); //$NON-NLS-1$
	}
}
