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
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.rse.core.subsystems.RSEDeploymentOptionsController;

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
	 * The base directory as stored in rse-dependent constant
	 */
	public static final String RSE_BASE_DIR = "org.jboss.ide.eclipse.as.rse.core.RSEServerBaseDir"; //$NON-NLS-1$

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

	public static String getRSEConnectionName(IServerAttributes server) {
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
		RSEDeploymentOptionsController controller = new RSEDeploymentOptionsController();
		controller.initialize(server.getServer(), null, null);
		return controller.getDeploymentsRootFolder(true);
	}


	// This method is unsafe when accessing a remote windows machine from non-windows
	@Deprecated
	protected static IPath getBaseDirectoryPath(IServerAttributes server) {
		return getBaseDirectoryPath(server, java.io.File.pathSeparatorChar);
	}
	
	protected static IPath getBaseDirectoryPath(IServerAttributes server, char sep) {
		String val = server.getAttribute(RSE_BASE_DIR, IJBossRuntimeResourceConstants.AS7_STANDALONE);
		IPath valPath = new RemotePath(val, sep);
		if( valPath.isAbsolute())
			return valPath;
		IPath ret = makeGlobal(server, valPath, sep);
		return ret;
	}

	public static String getBaseDirectory(IServerAttributes server, char separator) {
		return getBaseDirectoryPath(server, separator).toOSString();
	}
	
	// This signature may not work very well when on linux connected to remote windows
	public static IPath makeRelative(IServerAttributes server, IPath p) {
		return makeRelative(server, p, java.io.File.pathSeparatorChar);
	}
	
	public static IPath makeRelative(IServerAttributes server, IPath p, char sep) {
		RemotePath p1 = new RemotePath(p.toString(), sep);
		if (p1.isAbsolute()) {
			RemotePath rseHome = new RemotePath(getRSEHomeDir(server), sep);
			if (rseHome.isPrefixOf(p1)) { 
				return p1.makeRelativeTo(rseHome);
			}
		}
		return p;
	}

	// This method may be error prone if running on linux against a remote windows
	public static IPath makeGlobal(IServerAttributes server, IPath p) {
		return makeGlobal(server, p, getRemoteSystemSeparatorCharacter(server));
	}
	
	public static IPath makeGlobal(IServerAttributes server, IPath p, char sep) {

		if( server.getRuntime() == null || server.getRuntime().getLocation() == null) {
			// Has nothing to be relative to, so just make the current path absolute
			return p.makeAbsolute();
		}

		if (!p.isAbsolute()) {
			return new RemotePath(getRSEHomeDir(server), sep).makeAbsolute().append(p);
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
	
	/**
	 * Is this server's stored host property an rse host that has windows subsystems
	 * @param server
	 * @return
	 */
	public static boolean connectedToWindowsHost(IServerAttributes server) {
		IHost host = findHost(RSEUtils.getRSEConnectionName(server));
		return host == null ? false : isHostWindows(host);
	}
	
	/**
	 * Get the separator character for the remote system
	 * @param server
	 * @return
	 */
	public static char getRemoteSystemSeparatorCharacter(IServerAttributes server) {
		return connectedToWindowsHost(server) ? '\\' : '/';
	}
	
	/**
	 * Is the given host a windows host, using windows subsystems
	 * @param host
	 * @return
	 */
	public static boolean isHostWindows(IHost host) {
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
