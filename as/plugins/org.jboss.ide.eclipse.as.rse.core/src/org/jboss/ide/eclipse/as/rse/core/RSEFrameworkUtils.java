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
package org.jboss.ide.eclipse.as.rse.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.rse.core.util.RemoteCallWrapperUtility;
import org.jboss.ide.eclipse.as.rse.core.util.RemoteCallWrapperUtility.NamedRunnableWithProgress;


/**
 * The following are utility methods that deal specifically with the RSE Framework.
 * @Since 3.0
 */
public class RSEFrameworkUtils {

	/*  
	 * approved files subsystems 
	 */
	protected static List<String> APPROVED_FILE_SYSTEMS = Arrays.asList(new String[]{ 
			"ftp.files", "local.files", "ssh.files", "dstore.files"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	
	/**
	 * Find the given host by name
	 * @param connectionName
	 * @return
	 */
	public static IHost findHost(String connectionName) {
		IHost[] allHosts = RSECorePlugin.getTheSystemRegistry().getHosts();
		return findHost(connectionName, allHosts);
	}
	
	/**
	 * Find hte given connection in an array of possible hosts
	 * @param connectionName
	 * @param hosts
	 * @return
	 */
	public static IHost findHost(String connectionName, IHost[] hosts) {
		for (int i = 0; i < hosts.length; i++) {
			if (hosts[i].getAliasName().equals(connectionName))
				return hosts[i];
		}
		return null;
	}

	/**
	 * Wait for the full initialization of rse
	 * @throws CoreException
	 */
	public static void waitForFullInit() throws CoreException {
		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID,
					"The RSE model initialization has been interrupted."));
		}
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

	

	/**
	 * Find the files service subsystem for this host
	 * @param host
	 * @return
	 */
	public static IFileServiceSubSystem findFileTransferSubSystem(IHost host) {
		ISubSystem[] systems = RSECorePlugin.getTheSystemRegistry().getSubSystems(host);
		for( int i = 0; i < systems.length; i++ ) {
			if( APPROVED_FILE_SYSTEMS.contains(systems[i].getConfigurationId()))
				return (IFileServiceSubSystem)systems[i];
		}
		return null;
	}

	
	/**
	 * Find the shell service subsystem for this host
	 * @param host
	 * @return
	 */
	public static IShellService findHostShellSystem(IHost host) {
		ISubSystem[] systems = RSECorePlugin.getTheSystemRegistry().getSubSystems(host);
		for( int i = 0; i < systems.length; i++ ) {
			if( systems[i] instanceof IShellServiceSubSystem) {
				IShellService service = ((IShellServiceSubSystem)systems[i]).getShellService();
				return service;
			}
		}
		return null;
	}

	
	
	/**
	 * Ensure the given filesubsystem on the given server is connected
	 * @param server
	 * @param fileSubSystem
	 * @param monitor
	 * @return
	 */
	public static IStatus ensureActiveConnection(IServer server, final IFileServiceSubSystem fileSubSystem, IProgressMonitor monitor) {
		monitor.beginTask("Verifying connectivity to remote server", 200);
		Exception caught = null;
		Trace.trace(Trace.STRING_FINER, "Ensuring connection to remote server for server " + server.getName());
		if (fileSubSystem != null && !fileSubSystem.isConnected()) {
		    try {
		    	fileSubSystem.connect(ProgressMonitorUtil.getSubMon(monitor, 100), false);
		    } catch (Exception e) {
				Trace.trace(Trace.STRING_FINER, "Exception connecting to remote server: " + e.getMessage());
		    	// I'd rather not catch raw Exception, but that's all they throw
				caught = e;
		    }
		}
		boolean isConnected = fileSubSystem != null && fileSubSystem.isConnected();
		String connectionName = RSEUtils.getRSEConnectionName(server);
		if( isConnected ) {
			// The RSE tools might be mistaken here. The user may in fact have lost internet connectivity
			NamedRunnableWithProgress run = new NamedRunnableWithProgress("Accessing Remote System Root") {
				public Object run(IProgressMonitor monitor) throws CoreException,
						SystemMessageException, RuntimeException {
					fileSubSystem.getFileService().getRoots(monitor);
					return Status.OK_STATUS;
				}
			};
			IProgressMonitor childMonitor = ProgressMonitorUtil.getSubMon(monitor, 100);
			Exception e = RemoteCallWrapperUtility.wrapRemoteCallStatusTimeLimit(server, run,  "null", null, 15000, childMonitor);
			if( e == null )
				return Status.OK_STATUS;
			return new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
					"The remote server " + connectionName + " is currently not responding to file system requests.", e);
		}
		return new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL,
				"Unable to communicate with remote connection: " + connectionName, caught);
	}

	
}
