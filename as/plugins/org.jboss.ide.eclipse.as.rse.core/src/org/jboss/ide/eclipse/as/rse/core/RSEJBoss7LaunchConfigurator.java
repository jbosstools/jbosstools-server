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
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.IManagementPortProvider;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS7ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.debug.RemoteDebugUtils;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerTCPIPMonitorUtil;

/**
 * @author André Dietisheim
 */
public class RSEJBoss7LaunchConfigurator implements ILaunchConfigConfigurator {

	private IJBossServer jbossServer;

	public RSEJBoss7LaunchConfigurator(IServer server) throws CoreException {
		this.jbossServer = ServerConverter.checkedGetJBossServer(server);
	}

	@Override
	public void configure(ILaunchConfigurationWorkingCopy launchConfig) throws CoreException {
		RSELaunchConfigProperties propertyUtil = new RSELaunchConfigProperties();
		boolean detectStartupCommand = propertyUtil.isDetectStartupCommand(launchConfig, true);
		String currentStartupCmd = propertyUtil.getStartupCommand(launchConfig);
		
		// Set the default startup command
		String defaultStartup = getLaunchCommand((JBossServer)jbossServer);
		propertyUtil.setDefaultStartupCommand(defaultStartup, launchConfig);
		String defaultDebugStartup = getLaunchDebugCommand((JBossServer)jbossServer);
		
		// If we're auto-detecting, or user has not customized, use the default
		if( detectStartupCommand || !isSet(currentStartupCmd)) {
			propertyUtil.setStartupCommand(defaultStartup, launchConfig);
			propertyUtil.setDebugStartupCommand(defaultDebugStartup, launchConfig);
		}
		
		boolean detectShutdownCommand = propertyUtil.isDetectShutdownCommand(launchConfig, true);
		String currentShutdownCmd = propertyUtil.getShutdownCommand(launchConfig);
		
		// Set the default shutdown command
		String defaultShutdownCommand = getDefaultShutdownCommand(jbossServer.getServer());
		propertyUtil.setDefaultShutdownCommand(defaultShutdownCommand, launchConfig);
		
		// If we're auto-detecting, or user has not customized, use the default
		if( detectShutdownCommand || !isSet(currentShutdownCmd)) {
			propertyUtil.setShutdownCommand(defaultShutdownCommand, launchConfig);
		}
	}

	protected String getDefaultShutdownCommand(IServer server) throws CoreException {
		String rseHome = RSEUtils.getRSEHomeDir(server);
		IPath p = new Path(rseHome).append(IJBossRuntimeResourceConstants.BIN);
		String ret = p.toString() + "/" + getManagementScript(server);
		
		boolean exposeManagement = LaunchCommandPreferences.exposesManagement(server);
		if( exposeManagement ) {
			String host = server.getHost();
			ServerDelegate sd = (ServerDelegate) server.loadAdapter(ServerDelegate.class, new NullProgressMonitor());
			int defPort = 9999;
			int port = (sd instanceof IManagementPortProvider) ? 
					((IManagementPortProvider)sd).getManagementPort() : defPort;
					
			String host2 = ServerTCPIPMonitorUtil.getHostFor(host, port);
			int port2 = ServerTCPIPMonitorUtil.getPortFor(host, port);
			ret += " --controller=" + host2 + ":" + port2;
		}
		ret += " --connect command=:shutdown";
		return ret;
	}
	
	protected String getManagementScript(IServer server) {
		JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, null);
		if( props instanceof JBossAS7ExtendedProperties ) {
			return ((JBossAS7ExtendedProperties)props).getJBossAdminScript();
		}
		// Return a sensible default
		return IJBossRuntimeResourceConstants.AS_70_MANAGEMENT_SCRIPT;
	}
	
	protected JBossExtendedProperties getExtendedProperties() {
		JBossExtendedProperties props = (JBossExtendedProperties)jbossServer.getServer()
				.loadAdapter(JBossExtendedProperties.class,  new NullProgressMonitor());
		return props;
	}
	
	protected String getArgsOverrideHost(IServer server, String preArgs) {
		if( !getExtendedProperties().runtimeSupportsBindingToAllInterfaces() ) {
			return preArgs;
		}
		
		String host = server.getHost();
		if( LaunchCommandPreferences.listensOnAllHosts(jbossServer.getServer())) {
			host = "0.0.0.0";
		}
		
		return ArgsUtil.setArg(preArgs,
					IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT,
					null, host);
	}
	
	protected String getArgsOverrideConfigFile(IServer server, String preArgs) {
		// This is coded in this way bc rseutils doesn't have code to access the as7 stuff.
		// the same key is used, but a different default value is needed.
		// what a mess
		String rseConfigFile = server.getAttribute(
				RSEUtils.RSE_SERVER_CONFIG, LocalJBoss7ServerRuntime.CONFIG_FILE_DEFAULT);
		String programArguments = ArgsUtil.setArg(preArgs, null,
				IJBossRuntimeConstants.JB7_SERVER_CONFIG_ARG, rseConfigFile
				);
		return programArguments;
	}

	protected String getArgsOverrideBaseDir(IServer server, String preArgs) {
		Character c = RSEUtils.getRemoteSystemSeparatorCharacter(server);
		String basedir = RSEUtils.getBaseDirectory(server, c);
		String programArguments = ArgsUtil.setArg(preArgs, null,
				IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JBOSS_SERVER_BASE_DIR, basedir);
		return programArguments;
	}

	protected String getArgsOverrideExposedManagement(IServer server, String preArgs) {
		boolean overrides = LaunchCommandPreferences.exposesManagement(server);
		if( overrides ) {
			String newVal = overrides ? server.getHost() : null;
			String vmArguments = ArgsUtil.setArg(preArgs, null,
					IJBossRuntimeConstants.SYSPROP + IJBossRuntimeConstants.JB7_EXPOSE_MANAGEMENT, newVal );
			return vmArguments;
		}
		return preArgs;
	}
	
	protected String getLaunchCommand(JBossServer jbossServer) throws CoreException {
		String programArguments = getLaunchProgramArgs();
		String vmArguments = getLaunchVMArgs();
		String jar = getJar(jbossServer.getServer());
		String command = "java " + vmArguments + " -jar " + jar + IJBossRuntimeConstants.SPACE + programArguments + "&";
		return command;
	}
	
	protected String getLaunchDebugCommand(JBossServer jbossServer) throws CoreException {
		String programArguments = getLaunchProgramArgs();
		String vmArguments = getLaunchVMArgs();
		String jar = getJar(jbossServer.getServer());
		String debugPort = getDebugPort();
		String debugArgs = "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + debugPort + " -Xdebug ";
		String command = "java " + debugArgs + vmArguments + " -jar " + jar + IJBossRuntimeConstants.SPACE + programArguments + "&";
		return command;
	}

	protected boolean attachDebugger() {
		Boolean b = jbossServer.getServer().getAttribute(RemoteDebugUtils.ATTACH_DEBUGGER, true);
		return b;

	}
	protected String getDebugPort() {
		String dbugPort = jbossServer.getServer().getAttribute(RemoteDebugUtils.DEBUG_PORT, new Integer(RemoteDebugUtils.DEFAULT_DEBUG_PORT).toString());
		return dbugPort;
	}
	
	protected String getLaunchVMArgs() {
		String vmArguments = getDefaultVMArguments(jbossServer.getServer());
		vmArguments = getArgsOverrideExposedManagement(jbossServer.getServer(), vmArguments);
		return vmArguments;
	}
	
	protected String getLaunchProgramArgs() {
		String programArguments = getDefaultProgramArguments(jbossServer.getServer());
		programArguments = getArgsOverrideHost(jbossServer.getServer(), programArguments);
		programArguments = getArgsOverrideConfigFile(jbossServer.getServer(), programArguments);
		programArguments = getArgsOverrideBaseDir(jbossServer.getServer(), programArguments);
		return programArguments;
	}		
	
	protected String getDefaultVMArguments(IServer server) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultVMArgs();
	}

	protected String getDefaultProgramArguments(IServer server) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultProgramArgs();
	}
	
	protected String getJar(IServer server) {
		String rseHome = RSEUtils.getRSEHomeDir(server);
		return new Path(rseHome).append(getMainType()).toString();
	}
	
	protected String getMainType() {
		return IJBossRuntimeResourceConstants.JBOSS7_MODULES_JAR;
	}
	
	private boolean isSet(String value) {
		return value != null;// && value.length() > 0;
	}
}
