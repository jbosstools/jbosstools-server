/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.rse.core.xpl.ConnectAllSubsystemsUtil;

public class RSEHostShellModel {

	private static RSEHostShellModel instance;
	public static RSEHostShellModel getInstance() {
		if( instance == null )
			instance = new RSEHostShellModel();
		return instance;
	}
	
	private HashMap<String, ServerShellModel> map = 
		new HashMap<String, ServerShellModel>();
	RSEHostShellModel() {
		
	}
	
	public interface IJBASHostShellListener {
		public void writeToShell(String serverId, String[] lines);
	}
	
	private ArrayList<IJBASHostShellListener> listeners = new ArrayList<IJBASHostShellListener>();
	public void addHostShellListener(IJBASHostShellListener listener) {
		listeners.add(listener);
	}
	public void removeHostShellListener(IJBASHostShellListener listener) {
		listeners.remove(listener);
	}
	
	public ServerShellModel getModel(IServer server) {
		if( map.get(server.getId()) == null ) {
			map.put(server.getId(), new ServerShellModel(server.getId()));
		}
		return map.get(server.getId());
	}
	
	public static class ServerShellModel {
		private String serverId;
		private IHostShell startupShell;
		private IHostShell singleUseShell;
		private IHostShellOutputListener listener;
		public ServerShellModel(String id) {
			this.serverId = id;
		}
		public IHostShell getStartupShell() {
			return startupShell;
		}
		public void resetStartupShell() {
			if( startupShell != null && startupShell.isActive()) {
				startupShell.exit();
				startupShell = null;
			}
		}
		public IHostShell createStartupShell( 
				String initialWorkingDirectory, String command, 
				String[] environment, IProgressMonitor monitor) 
					throws CoreException, SystemMessageException {
			resetStartupShell();
			IServer s = ServerCore.findServer(serverId);
			IShellService service = findShellService(s);
			try {
				IHostShell hs = service.runCommand(initialWorkingDirectory, 
									command, environment, monitor);
				listener = new IHostShellOutputListener() {
					public void shellOutputChanged(IHostShellChangeEvent event) {
						IHostOutput[] lines = event.getLines();
						String[] lines2 = new String[lines.length];
						for(int i = 0; i < lines.length; i++ ) {
							lines2[i] = lines[i].getString();
						}
						writeToConsole(lines2);
					}
				};
				startupShell = hs;
				startupShell.addOutputListener(listener);
				return hs;
			} catch(RuntimeException re) {
				throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
						re.getMessage(), re));
			}
		}
			
		protected void writeToConsole(String[] lines) {
			Iterator<IJBASHostShellListener> i = RSEHostShellModel.getInstance().listeners.iterator();
			while(i.hasNext())
				i.next().writeToShell(serverId, lines);
		}
		
		public void executeRemoteCommand( 
				String initialWorkingDirectory, String command, 
				String[] environment, IProgressMonitor monitor) 
					throws CoreException {
			IServer s = ServerCore.findServer(serverId);
			IShellService service = findShellService(s);
			try {
				if( singleUseShell == null || !singleUseShell.isActive()) {
					singleUseShell = service.launchShell(initialWorkingDirectory, environment, monitor);
				} else {
					singleUseShell.writeToShell("cd " + initialWorkingDirectory);
				}
				singleUseShell.writeToShell(command);
			} catch(RuntimeException re) {
				String className = service.getClass().getName(); 
				if(className.endsWith(".DStoreShellService")) {
					throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
							"no remote daemon installed. Please install a remote daemon or use an RSE server configured for ssh rather than dstore"));
				}
			} catch( SystemMessageException sme) {
				Status s2 = new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, sme.getMessage(), sme);
				throw new CoreException(s2);
			}
		}
		
		public void executeRemoteCommand( 
				String initialWorkingDirectory, String command, 
				String[] environment, IProgressMonitor monitor,
				int delay, boolean exit)
					throws CoreException {
			executeRemoteCommand(initialWorkingDirectory, command, environment, monitor);
			ThreadUtils.sleepFor(delay);
			if( exit ) {
				singleUseShell.exit();
				singleUseShell = null;
			}
		}
		
	}
	
	/**
	 * @deprecated moved to {@link ThreadUtils#sleepFor(int)}
	 */
	@Deprecated
	public static void delay(int delay /* in ms */) {
		int x = 0;
		while( x < delay) {
			x+=200;
			try {
				Thread.sleep(200);
			} catch(InterruptedException ie) {
			}
		}
	}
	
	public static IShellService findShellService(DelegatingServerBehavior behaviour) throws CoreException {
		return findShellService(behaviour.getServer());
	}

	public static IShellService findShellService(IServer server) throws CoreException {
		RSEUtils.waitForFullInit();
		if( server != null ) {
			String connectionName = RSEUtils.getRSEConnectionName(server);
			IHost host = RSEUtils.findHost(connectionName);
			if( host == null ) {
				throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
						"Host not found. Host may have been deleted or RSE model may not be completely loaded"));
			}
			
			// ensure connections 
			new ConnectAllSubsystemsUtil(host).run(new NullProgressMonitor());
			
			ISubSystem[] systems = RSECorePlugin.getTheSystemRegistry().getSubSystems(host);
			for( int i = 0; i < systems.length; i++ ) {
				if( systems[i] instanceof IShellServiceSubSystem) {
					IShellService service = ((IShellServiceSubSystem)systems[i]).getShellService();
					return service;
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
				"No Shell Service Found"));
	}
}
