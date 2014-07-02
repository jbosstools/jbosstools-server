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
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;

/**
 * This class should be more accurately labeled working with properties 
 * on an IServer object that relate to rse usage. It accomplishes things
 * like checking server mode, remote server home, deployment folder, 
 * etc. 
 */
public class RSEUtils {
	
   /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';
    
    
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
	 * This value is also the id of a server profile
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
		String defaultVal = "";
		if( server.getRuntime() != null && server.getRuntime().getLocation() != null ) {
			defaultVal = server.getRuntime().getLocation().toString();
		}
		return server.getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, defaultVal);
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
		String defVal = IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION;
		if( runtime != null ) {
			boolean isAS7Style =(sep != null && sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS);
			boolean useAS7 = isAS7Style && runtime instanceof LocalJBoss7ServerRuntime;
			defVal = useAS7 ? ((LocalJBoss7ServerRuntime)runtime).getConfigurationFile() : runtime.getJBossConfiguration(); 
		}
		return server.getAttribute(RSEUtils.RSE_SERVER_CONFIG, defVal);
	}

	public static String getDeployRootFolder(IDeployableServer server) {
		return getDeployRootFolder(server.getServer());
	}
	
	/**
	 * Get the deploy root for a given server 
	 * @param server
	 * @return
	 * @since 3.0
	 */
	public static String getDeployRootFolder(IServer server) {
		RSEDeploymentOptionsController controller = new RSEDeploymentOptionsController();
		controller.initialize(server, null, null);
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

	public static String makeRelativeString(IServerAttributes server, IPath p, char sep) {
		RemotePath p1 = new RemotePath(p.toString(), sep);
		String p1String = p1.toOSString();
		if (p1.isAbsolute()) {
			RemotePath rseHome = new RemotePath(getRSEHomeDir(server), sep);
			String rseHomeString = rseHome.toOSString();
			if (p1String.startsWith(rseHomeString)) { 
				return p1String.substring(rseHomeString.length());
			}
		}
		return p1String;
	}

	
	// This method may be error prone if running on linux against a remote windows
	public static IPath makeGlobal(IServerAttributes server, IPath p) {
		return makeGlobal(server, p, getRemoteSystemSeparatorCharacter(server));
	}
	
	public static IPath makeGlobal(IServerAttributes server, IPath p, char sep) {
		String home = getRSEHomeDir(server);
		if( home == null) {
			// Has nothing to be relative to, so just make the current path absolute
			return p.makeAbsolute();
		}

		if (!p.isAbsolute()) {
			return new RemotePath(home, sep).makeAbsolute().append(p);
		}
		return p;
	}


	public static IServer setServerToRSEMode(IServer server, IHost newHost) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerProfileModel.setProfile(wc, RSE_MODE);
		wc.setAttribute(RSE_SERVER_HOST, newHost.getAliasName());
		wc.setAttribute("hostname", newHost.getHostName());
		return wc.save(false, new NullProgressMonitor());
	}

	public static IServer setServerToRSEMode(IServer server, IHost newHost,
			String jbossHome, String config) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerProfileModel.setProfile(wc, RSE_MODE);
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
     * Returns an OS-compliant path for a remote system with the given base
     * and a tail to append on it.
     * 
     * @param host
     * @param path
     * @param append
     * @return
     */
	public static String pathToRemoteSystem(IHost host, String path, String tail) {
		char sep = getRemoteSystemSeparatorCharacter(host);
		IPath rp = new RemotePath(path, sep);
		if( tail != null ) {
			rp = rp.append(tail);
		}
		return rp.toOSString();
	}
	
	/**
	 * Is this server's stored host property an rse host that has windows subsystems
	 * @param server
	 * @return
	 */
	public static boolean connectedToWindowsHost(IServerAttributes server) {
		IHost host = RSEFrameworkUtils.findHost(RSEUtils.getRSEConnectionName(server));
		return host == null ? false : RSEFrameworkUtils.isHostWindows(host);
	}
	
	/**
	 * Get the separator character for the remote system
	 * @param server
	 * @return
	 */
	public static char getRemoteSystemSeparatorCharacter(IServerAttributes server) {
		return connectedToWindowsHost(server) ? WINDOWS_SEPARATOR :  UNIX_SEPARATOR;
	}
	
	/**
	 * 
	 * @param host
	 * @return
	 * @since 3.0
	 */
	public static char getRemoteSystemSeparatorCharacter(IHost host) {
		boolean win = (host == null ? false : RSEFrameworkUtils.isHostWindows(host));
		return win ? WINDOWS_SEPARATOR :  UNIX_SEPARATOR;
	}
}
