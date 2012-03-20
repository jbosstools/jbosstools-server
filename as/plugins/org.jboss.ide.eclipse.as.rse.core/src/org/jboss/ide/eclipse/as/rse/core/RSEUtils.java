/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

/* 
 * Some of this code will need to be abstracted out from JBossServer
 * and turned into a proper API, but in as simple a way as possible
 */
public class RSEUtils {
	public static final String RSE_SERVER_CONFIG = "org.jboss.ide.eclipse.as.rse.core.RSEServerConfig"; //$NON-NLS-1$
	public static final String RSE_SERVER_HOME_DIR = "org.jboss.ide.eclipse.as.rse.core.RSEServerHomeDir"; //$NON-NLS-1$
	public static final String RSE_SERVER_HOST = "org.jboss.ide.eclipse.as.rse.core.ServerHost"; //$NON-NLS-1$
	public static final String RSE_SERVER_DEFAULT_HOST = "Local"; //$NON-NLS-1$
	public static final String RSE_MODE = "rse";

	public static String getRSEConnectionName(IServer server) {
		return server.getAttribute(RSEUtils.RSE_SERVER_HOST, RSE_SERVER_DEFAULT_HOST);
	}

	public static String getRSEHomeDir(IServer server, boolean errorOnFail) throws CoreException {
		String rseHome = null;
		if (errorOnFail) {
			rseHome = RSEUtils.checkedGetRSEHomeDir(server);
		} else {
			rseHome = RSEUtils.getRSEHomeDir(server);
		}
		return rseHome == null ? "" : rseHome;
	}

	public static String getRSEHomeDir(IServerAttributes server) {
		return server.getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, server.getRuntime().getLocation().toString());
	}

	public static String checkedGetRSEHomeDir(IServerAttributes server) throws CoreException {
		String serverHome = server.getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, server.getRuntime().getLocation()
				.toString());
		if (serverHome == null) {
			throw new CoreException(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID,
					"Remote Server Home not set."));
		}
		return serverHome;
	}

	public static String getRSEConfigName(IServerAttributes server) {
		IJBossServerRuntime runtime = RuntimeUtils.getJBossServerRuntime(server);
		return server.getAttribute(RSEUtils.RSE_SERVER_CONFIG, 
				runtime == null ? null : runtime.getJBossConfiguration());
	}

	public static String getDeployRootFolder(IDeployableServer server) {
		return getDeployRootFolder(server.getServer(), server.getDeployLocationType());
	}

	/* Copied from JBossServer.getDeployFolder(etc) */
	public static String getDeployRootFolder(IServer server, String type) {
		if (JBossServer.DEPLOY_CUSTOM.equals(type)) {
			String val = server.getAttribute(JBossServer.DEPLOY_DIRECTORY, (String) null);
			if (val != null) {
				IPath val2 = new Path(val);
				return makeGlobal(server, val2).toString();
			}
			// if no value is set, default to metadata
			type = JBossServer.DEPLOY_SERVER;
		}
		// This should *NOT* happen, so if it does, we will default to server
		// location
		else if (JBossServer.DEPLOY_METADATA.equals(type)) {
			type = JBossServer.DEPLOY_SERVER;
		}
		else if (JBossServer.DEPLOY_SERVER.equals(type)) {
			// TODO !!!! Need API (nmaybe in JBossServer?) so servers can
			// override this behavior
			// Cannot move this code to JBossServer because this requires an
			// RSE-specific key!! Damn!

			if (ServerUtil.isJBoss7(server.getServerType())) {
				IPath p = new Path("standalone/deployments/");
				return makeGlobal(server, p).toString();
			} else {
				String loc = IConstants.SERVER;
				String config = getRSEConfigName(server);
				if( loc == null || config == null )
					return null;
				IPath p = new Path(loc).append(config)
						.append(IJBossRuntimeResourceConstants.DEPLOY);
				return makeGlobal(server, p).toString();
			}
		}
		return null;
	}

	public static IPath makeRelative(IServer server, IPath p) {
		if (p.isAbsolute()) {
			if (new Path(getRSEHomeDir(server)).isPrefixOf(p)) {
				int size = new Path(getRSEHomeDir(server)).toOSString().length();
				return new Path(p.toOSString().substring(size)).makeRelative();
			}
		}
		return p;
	}

	public static IPath makeGlobal(IServer server, IPath p) {
		if (!p.isAbsolute()) {
			return new Path(getRSEHomeDir(server)).append(p).makeAbsolute();
		}
		return p;
	}

	public static IHost findHost(String connectionName) {
		// TODO ensure that all hosts are actually loaded, christ
		IHost[] allHosts = RSECorePlugin.getTheSystemRegistry().getHosts();
		for (int i = 0; i < allHosts.length; i++) {
			if (allHosts[i].getAliasName().equals(connectionName))
				return allHosts[i];
		}
		return null;
	}

	public static void waitForFullInit() throws CoreException {
		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID,
					"The RSE model initialization has been interrupted."));
		}
	}

	public static IServer setServerToRSEMode(IServer server, IHost newHost) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.SERVER_MODE, RSE_MODE);
		wc.setAttribute(RSE_SERVER_HOST, newHost.getAliasName());
		wc.setAttribute("hostname", newHost.getHostName());
		return wc.save(false, new NullProgressMonitor());
	}

	public static IServer setServerToRSEMode(IServer server, IHost newHost,
			String jbossHome, String config) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.SERVER_MODE, RSE_MODE);
		wc.setAttribute(RSE_SERVER_CONFIG, config);
		wc.setAttribute(RSE_SERVER_HOME_DIR, jbossHome);
		wc.setAttribute(RSE_SERVER_HOST, newHost.getAliasName());
		wc.setAttribute("hostname", newHost.getHostName());
		wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE,
				IDeployableServer.DEPLOY_SERVER);
		return wc.save(false, new NullProgressMonitor());
	}
	
	public static boolean isActive(IHostShell shell) {
		return shell != null
				&& shell.isActive();
	}

}
