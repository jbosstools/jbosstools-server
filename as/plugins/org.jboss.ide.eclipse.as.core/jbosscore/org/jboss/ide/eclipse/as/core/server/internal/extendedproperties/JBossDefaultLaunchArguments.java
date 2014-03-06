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
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.BIN;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.ENDORSED;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.LIB;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.NATIVE;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;
import org.jboss.ide.eclipse.as.core.util.PortalUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBossDefaultLaunchArguments implements IDefaultLaunchArguments, IJBossRuntimeConstants {
	protected IServer server;
	protected IRuntime runtime;
	
	private IPath serverHome;
	
	// If created with a server, we can try to get values from 
	// whatever 'mode' it is in (local vs rse). 
	public JBossDefaultLaunchArguments(IServer s) {
		this(s.getRuntime());
		this.server = s;
	}

	/*
	 * If created via just a runtime, only the local information
	 * can be used. This method is left here for API backwards 
	 * compatability ONLY, and is NOT endorsed API.
	 */
	public JBossDefaultLaunchArguments(IRuntime rt) {
		this.runtime = rt;
	}

	
	private void setServerHome(IPath path) {
		this.serverHome = path;
	}
	
	protected IPath getServerHome() {
		// If we have a set server home, use it
		if( serverHome != null)
			return serverHome;
		// Get from server-mode data (local/rse)
		if( server != null ) {
			IServerModeDetails det = (IServerModeDetails)Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class);
			String homeDir = det.getProperty(IServerModeDetails.PROP_SERVER_HOME);
			return new Path(homeDir);
		}
		// We have no server, so, just use the runtime's homedir
		return getLocalRuntimeHomeDirectory();
	}
		
	protected IRuntime getRuntime() {
		return server == null ? runtime : server.getRuntime();
	}
	
	protected IJBossServerRuntime getJBossRuntime() {
		IRuntime rt = getRuntime();
		return rt == null ? null : (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, null);
	}
	
	public String getStartDefaultProgramArgs() {
		String s1 = STARTUP_ARG_CONFIG_LONG + "=" + getJBossRuntime().getJBossConfiguration() + SPACE;  //$NON-NLS-1$
		if( PortalUtil.getServerPortalType(getJBossRuntime()) == PortalUtil.TYPE_GATE_IN) {
			s1 += IJBossRuntimeConstants.SPACE + "-Dexo.conf.dir.name=gatein"; //$NON-NLS-1$
		}
		return s1;
	}

	public String getStartDefaultVMArgs() {
		return getProgramNameArgs() + getServerFlagArgs() + 
				getMemoryArgs() + getJavaFlags() + getJBossJavaFlags();
	}

	protected String getProgramNameArgs() {
		String name = server == null ? runtime.getName() : server.getName();
		String ret = QUOTE + SYSPROP + PROGRAM_NAME_ARG + EQ +  
			"JBossTools: " + name + QUOTE + SPACE; //$NON-NLS-1$
		return ret;
	}
	
	protected String getServerFlagArgs() {
		// We assume that even if the server is in remote mode, the remote configuration
		// matches the local very closely. 
		// But if there's no local runtime, we'll just not include the -server flag
		if( getJBossRuntime() != null ) {
			if( JavaUtils.supportsServerMode(getJBossRuntime().getVM()))
				return SERVER_ARG + SPACE;
		}
		return new String();
	}
	
	protected boolean isLinux() {
		// For now, assume local and remote match. Additional API may be needed here. 
		return Platform.getOS().equals(Platform.OS_LINUX);
	}
	
	protected String getJavaFlags() {
		return getJavaFlags(isLinux());
	}
	
	protected String getJavaFlags(boolean forceIP4) {
		String ret = new String();
		if( forceIP4 )
			ret += SYSPROP + JAVA_PREFER_IP4_ARG + EQ + true + SPACE; 
		ret += SYSPROP + SUN_CLIENT_GC_ARG + EQ + 3600000 + SPACE;
		ret += SYSPROP + SUN_SERVER_GC_ARG + EQ + 3600000 + SPACE;
		return ret;
	}
	
	// Get flags that are java flags that point to files somewhere in the AS or dependent on runtime data
	protected String getJBossJavaFlags() {
		IPath serverHome = getServerHome();
		String ret = QUOTE + SYSPROP + ENDORSED_DIRS + EQ + 
				(serverHome.append(LIB).append(ENDORSED)) + QUOTE + SPACE;
		
		// Assume the remote also has a native folder if the local has a native folder
		// This avoids costly remote lookups. 
		if( serverHome.append(BIN).append(NATIVE).toFile().exists() ) 
			ret += SYSPROP + JAVA_LIB_PATH + EQ + QUOTE + 
				serverHome.append(BIN).append(NATIVE) + QUOTE + SPACE;
		return ret;
	}
	
	protected IPath getLocalRuntimeHomeDirectory() {
		IRuntime rt = runtime == null ? server.getRuntime() : runtime;
		return rt.getLocation();
	}
	
	// Subclasses can override
	protected String getMemoryArgs() {
		return DEFAULT_MEM_ARGS;
	}
	
	@Override
	public HashMap<String, String> getDefaultRunEnvVars() {
		HashMap<String, String> envVars = new HashMap<String, String>(1);
		envVars.put("PATH", NATIVE + System.getProperty("path.separator") + "${env_var:PATH}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return envVars;
	}

	/* The following methods are likely to be removed from the interface */
	@Override
	public String getStartDefaultProgramArgs(IPath serverHome) {
		setServerHome(serverHome);
		return getStartDefaultProgramArgs();
	}

	@Override
	public String getStartDefaultVMArgs(IPath serverHome) {
		setServerHome(serverHome);
		return getStartDefaultVMArgs();
	}

	protected String getShutdownServerUrl() {
		IJBossServer jbs = ServerConverter.getJBossServer(server);
		return jbs.getHost() + ":" + jbs.getJNDIPort(); //$NON-NLS-1$
	}
	
	protected boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	@Override
	public String getDefaultStopArgs() {
		IJBossServer jbs = ServerConverter.getJBossServer(server);
		String username = jbs.getUsername();
		String pw = jbs.getPassword();
		
		String serverUrl = getShutdownServerUrl();
		
		StringBuffer sb = new StringBuffer();
		sb.append(IJBossRuntimeConstants.SHUTDOWN_STOP_ARG );
		sb.append(IJBossRuntimeConstants.SPACE);
		sb.append(IJBossRuntimeConstants.SHUTDOWN_SERVER_ARG); 
		sb.append(IJBossRuntimeConstants.SPACE); 
		sb.append(serverUrl); 
		sb.append(IJBossRuntimeConstants.SPACE);
		
		if( !isEmpty(username)) {
			sb.append(IJBossRuntimeConstants.SHUTDOWN_USER_ARG); 
			sb.append(IJBossRuntimeConstants.SPACE);
			sb.append(username);
			sb.append(IJBossRuntimeConstants.SPACE);
		}
		if( !isEmpty(pw)) {
			sb.append(IJBossRuntimeConstants.SHUTDOWN_PASS_ARG);
			sb.append(IJBossRuntimeConstants.SPACE);
			sb.append(pw);
			sb.append(IJBossRuntimeConstants.SPACE);
		}
		return sb.toString();
	}

}
