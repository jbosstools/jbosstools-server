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
package org.jboss.ide.eclipse.as.core.server.internal;

import java.io.File;
import java.util.ArrayList;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class ServerListener extends UnitedServerListener {
	private static ServerListener instance;
	public static ServerListener getDefault() {
		if( instance == null )
			instance = new ServerListener();
		return instance;
	}
	
	public void serverAdded(IServer server) {
		ServerUtil.createStandardFolders(server);
	}

	public void serverRemoved(IServer server) {
		// delete metadata area
		File f = JBossServerCorePlugin.getServerStateLocation(server).toFile();
		FileUtil.safeDelete(f);
	}
	
	public void serverChanged(ServerEvent event) {
		IServer server = event.getServer();
		JBossServer jbs = (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		if( jbs != null ) {
			doDeploymentAddition(event);
		}
	}
	
	protected void doDeploymentAddition(final ServerEvent event) {
		int eventKind = event.getKind();
		if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
			// server change event
			if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
				if( event.getServer().getServerState() == IServer.STATE_STARTED ) {
					IJMXRunnable r = new IJMXRunnable() {
						public void run(MBeanServerConnection connection) throws Exception {
							ensureDeployLocationAdded(event.getServer(), connection);
						}
					};
					try {
						JBossServerConnectionProvider.run(event.getServer(), r);
					} catch( JMXException jmxe ) {
						IStatus s = jmxe.getStatus();
						IStatus newStatus = new Status(s.getSeverity(), s.getPlugin(), IEventCodes.ADD_DEPLOYMENT_FOLDER_FAIL, 
								Messages.AddingJMXDeploymentFailed, s.getException());
						ServerLogger.getDefault().log(event.getServer(), newStatus);
					}
				}
			}
		}
	}
	
	protected void ensureDeployLocationAdded(IServer server, MBeanServerConnection connection) throws Exception {
		JBossServer ds = ServerConverter.getJBossServer(server);
		ArrayList<String> folders = new ArrayList<String>();
		// add the server folder deploy loc. first
		String insideServer = JBossServer.getDeployFolder(ds, JBossServer.DEPLOY_SERVER);
		String metadata = JBossServer.getDeployFolder(ds, JBossServer.DEPLOY_METADATA);
		String custom = JBossServer.getDeployFolder(ds, JBossServer.DEPLOY_CUSTOM);
		String serverHome = null;
		if (server != null && server.getRuntime()!= null && server.getRuntime().getLocation() != null) {
			serverHome = server.getRuntime().getLocation().toString();
		}
		folders.add(insideServer);
		if( !folders.contains(metadata))
			folders.add(metadata);
		if( !folders.contains(custom) && !custom.equals(serverHome))
			folders.add(custom);

		IModule[] modules2 = org.eclipse.wst.server.core.ServerUtil.getModules(server.getServerType().getRuntimeType().getModuleTypes());
		if (modules2 != null) {
			int size = modules2.length;
			for (int i = 0; i < size; i++) {
				IModule[] module = new IModule[] { modules2[i] };
				IStatus status = server.canModifyModules(module, null, null);
				if (status != null && status.getSeverity() != IStatus.ERROR) {
					String tempFolder = PublishUtil.getDeployRootFolder(module, ds).toString(); 
					if( !folders.contains(tempFolder))
						folders.add(tempFolder);
				}
			}
		}
		folders.remove(insideServer); // doesn't need to be added to deployment scanner
		String[] folders2 = (String[]) folders.toArray(new String[folders.size()]);
		for( int i = 0; i < folders2.length; i++ ) {
			String asURL = new File(folders2[i]).toURL().toString(); 
			ObjectName name = new ObjectName(IJBossRuntimeConstants.DEPLOYMENT_SCANNER_MBEAN_NAME);
			connection.invoke(name, IJBossRuntimeConstants.addURL, new Object[] { asURL }, new String[] {String.class.getName()});
		}
	}
}
