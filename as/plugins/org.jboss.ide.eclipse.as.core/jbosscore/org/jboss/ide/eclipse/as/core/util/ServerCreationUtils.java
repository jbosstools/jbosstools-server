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
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;

public class ServerCreationUtils {

	public static IServer createDeployOnlyServer(String deployLocation, String tempDeployLocation, 
			String rtName, String serverName) throws CoreException {
		IRuntimeType rt = ServerCore.findRuntimeType(IJBossToolingConstants.DEPLOY_ONLY_RUNTIME);
		IRuntimeWorkingCopy wc = rt.createRuntime(rtName, null);
		IRuntime runtime = wc.save(true, null);
		IServerType st = ServerCore.findServerType(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
		ServerWorkingCopy swc = (ServerWorkingCopy) st.createServer(serverName, null, null);
		swc.setServerConfiguration(null);
		swc.setName(serverName);
		swc.setRuntime(runtime);
		swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLocation);
		swc.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempDeployLocation);
		IServer server = swc.save(true, null);
		return server;
	}
	
	public static IServer createServer2(IRuntime currentRuntime, String serverTypeId) throws CoreException {
		return createServer2(currentRuntime, serverTypeId, serverTypeId);
	}

	public static IServer createServer2(IRuntime currentRuntime, String serverTypeId, String serverName) throws CoreException {
		IServerType serverType = ServerCore.findServerType(serverTypeId);
		return createServer2(currentRuntime, serverType, serverName);
	}
	public static IServer createServer2(IRuntime currentRuntime, String serverTypeId, String serverName, String mode) throws CoreException {
		IServerType serverType = ServerCore.findServerType(serverTypeId);
		return createServer2(currentRuntime, serverType, serverName, mode);
	}
	
	public static IServer createServer2(IRuntime currentRuntime, IServerType serverType, String serverName) throws CoreException {
		return createServer2(currentRuntime, serverType, serverName, "local"); //$NON-NLS-1$
	}
	public static IServer createServer2(IRuntime currentRuntime, IServerType serverType, String serverName, String mode) throws CoreException {
		IServerWorkingCopy serverWC = serverType.createServer(null, null,
				new NullProgressMonitor());
		serverWC.setRuntime(currentRuntime);
		serverWC.setName(serverName);
		serverWC.setServerConfiguration(null);
		serverWC.setAttribute(IDeployableServer.SERVER_MODE, mode); 
		return serverWC.save(true, new NullProgressMonitor());
	}
	
	public static IRuntime createRuntime(String runtimeId, String homeDir,
			String config, IExecutionEnvironment environment) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null,runtimeId);
		IRuntimeType runtimeType = runtimeTypes[0];
		IRuntimeWorkingCopy runtimeWC = runtimeType.createRuntime(null,
				new NullProgressMonitor());
		runtimeWC.setName(runtimeId);
		runtimeWC.setLocation(new Path(homeDir));
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_EXECUTION_ENVIRONMENT, environment.getId());
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);

		IRuntime savedRuntime = runtimeWC.save(true, new NullProgressMonitor());
		return savedRuntime;
	}

}
