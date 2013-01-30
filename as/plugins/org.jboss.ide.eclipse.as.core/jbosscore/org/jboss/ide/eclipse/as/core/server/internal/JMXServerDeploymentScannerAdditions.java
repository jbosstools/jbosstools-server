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
package org.jboss.ide.eclipse.as.core.server.internal;

import java.net.URI;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunnable;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;

public class JMXServerDeploymentScannerAdditions extends AbstractDeploymentScannerAdditions {
	public JMXServerDeploymentScannerAdditions() {
		
	}
	public boolean accepts(IServer server) {
		if( !LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(getServerMode(server)))
			return false;
		
		ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, null);
		JBossServer jbs = (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		boolean hasJMXProvider = jbs != null && jbs.hasJMXProvider();
		boolean jmxDeploymentScanner = props != null && props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_JMX_SUPPORT;
		if(hasJMXProvider && jmxDeploymentScanner) {
			return true;
		}
		return false;
	}
	
	protected void modifyDeploymentScanners(ServerEvent event){
		String[] folders = getDeployLocationFolders(event.getServer());
		Trace.trace(Trace.STRING_FINER, "Adding " + folders.length + " Deployment Scanners via JMX"); //$NON-NLS-1$ //$NON-NLS-2$
		if( folders.length > 0 ) 
			ensureScannersAdded(event.getServer(), folders);
		Trace.trace(Trace.STRING_FINER, "Finished Adding Deployment Scanners via JMX"); //$NON-NLS-1$
	}

	protected void ensureScannersAdded(final IServer server, final String[] folders) {
		ExtensionManager.getDefault().getJMXRunner().beginTransaction(server, this);
		IServerJMXRunnable r = new IServerJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				ensureDeployLocationAdded(server, connection, folders);
			}
		};
		try {
			ExtensionManager.getDefault().getJMXRunner().run(server, r);
		} catch( CoreException jmxe ) {
			IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, 
					IEventCodes.ADD_DEPLOYMENT_FOLDER_FAIL, 
					Messages.AddingJMXDeploymentFailed, jmxe);
			ServerLogger.getDefault().log(server, status);
		}
	}
	
	private void ensureDeployLocationAdded(IServer server, 
			MBeanServerConnection connection, String[] folders2) throws Exception {
		for( int i = 0; i < folders2.length; i++ ) {
			String asURL = encode(folders2[i]);
			Trace.trace(Trace.STRING_FINER, "Adding Deployment Scanner: " + asURL); //$NON-NLS-1$
			ObjectName name = new ObjectName(IJBossRuntimeConstants.DEPLOYMENT_SCANNER_MBEAN_NAME);
			Object o = connection.invoke(name, IJBossRuntimeConstants.addURL, new Object[] { asURL }, new String[] {String.class.getName()});
			System.out.println(o);
		}
	}

	private String encode(String folder) throws Exception {
		folder = folder.replace("\\", "/");  //$NON-NLS-1$//$NON-NLS-2$
		if (! folder.startsWith("/")) { //$NON-NLS-1$
			folder = "/" + folder; //$NON-NLS-1$
		}
		URI uri = new URI("file", null, folder, null); //$NON-NLS-1$
		//return URLEncoder.encode(uri.toASCIIString());
		return uri.toASCIIString();
	}
}
