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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;

/* 
 * Some of this code will need to be abstracted out from JBossServer
 * and turned into a proper API, but in as simple a way as possible
 */
public class RSEUtils {
	
	/**
	 * A key which represents either the configuration name of as < 7 server (default or all), 
	 * or, in as >= 7, represents the configuration FILE name (standalone.xml, etc)
	 */
	public static final String RSE_SERVER_CONFIG = "org.jboss.ide.eclipse.as.rse.core.RSEServerConfig"; //$NON-NLS-1$
	
	/**
	 * The home directory of the remote server
	 */
	public static final String RSE_SERVER_HOME_DIR = "org.jboss.ide.eclipse.as.rse.core.RSEServerHomeDir"; //$NON-NLS-1$
	
	/**
	 * A key to store which RSE host this server is attached to
	 */
	public static final String RSE_SERVER_HOST = "org.jboss.ide.eclipse.as.rse.core.ServerHost"; //$NON-NLS-1$
	/**
	 * The default host if one is not set
	 */
	public static final String RSE_SERVER_DEFAULT_HOST = "Local"; //$NON-NLS-1$
	
	/**
	 * The name of this mode, which is just 'rse' 
	 */
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

	
	public static String getRSEConfigFile(IServerAttributes server) {
		// At this time, both config name (as<7) and config file (as7) use the same key. 
		// this must remain this way or we will break users.  
		return getRSEConfigName(server);
	}
	
	public static String getRSEConfigName(IServerAttributes server) {
		IJBossServerRuntime runtime = RuntimeUtils.getJBossServerRuntime(server);
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		boolean isAS7Style =(sep != null && sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS);
		boolean useAS7 = isAS7Style && runtime instanceof LocalJBoss7ServerRuntime;
		String defVal = useAS7 ? ((LocalJBoss7ServerRuntime)runtime).getConfigurationFile() : runtime.getJBossConfiguration(); 
		return server.getAttribute(RSEUtils.RSE_SERVER_CONFIG,  runtime == null ? null : defVal);
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
		if (JBossServer.DEPLOY_SERVER.equals(type)) {
			// TODO !!!! Need API (nmaybe in JBossServer?) so servers can
			// override this behavior
			// Cannot move this code to JBossServer because this requires an
			// RSE-specific key!! Damn!
			ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
			if (sep != null && sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
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
		IHost[] allHosts = RSECorePlugin.getTheSystemRegistry().getHosts();
		return findHost(connectionName, allHosts);
	}
	
	public static IHost findHost(String connectionName, IHost[] hosts) {
		for (int i = 0; i < hosts.length; i++) {
			if (hosts[i].getAliasName().equals(connectionName))
				return hosts[i];
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
	
	   /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';
	public static String pathToRemoteSystem(IHost host, String path, String append) {
		boolean hostIsWindows = isHostWindows(host);
		char sep = hostIsWindows ? WINDOWS_SEPARATOR : UNIX_SEPARATOR;
		boolean endsWithSep = path.endsWith(Character.toString(WINDOWS_SEPARATOR)) || path.endsWith(Character.toString(UNIX_SEPARATOR));
		String path2 = append == null ? path :
			// ensure we have a trailing separator before appending the 'append'
			(endsWithSep ? path : path + sep) + append;
		String path3 = hostIsWindows ? separatorsToWindows(path2) : separatorsToUnix(path2);
		return path3;
	}
	private static boolean isHostWindows(IHost host) {
		String sysType = host.getSystemType().getId();
		if( sysType.equals("org.eclipse.rse.systemtype.windows"))
			return true;
		ISubSystem[] systems = RSECorePlugin.getTheSystemRegistry().getSubSystems(host);
		for( int i = 0; i < systems.length; i++ ) {
			if( systems[i].getConfigurationId().equals("dstore.windows.files"))
				return true;
		}
		return false;
	}
    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    /**
     * Converts all separators to the Windows separator of backslash.
     * 
     * @param path  the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToWindows(String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }
}
