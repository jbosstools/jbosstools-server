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
package org.jboss.ide.eclipse.as.jmx.integration;

import java.net.URI;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7DeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JMXServerLifecycleListener extends LocalJBoss7DeploymentScannerAdditions {
	protected boolean accepts(IServer server) {
		ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, null);
		JBossServer jbs = (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		boolean hasJMXProvider = jbs != null && jbs.hasJMXProvider();
		boolean jmxDeploymentScanner = props != null && props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_JMX_SUPPORT;
		if(hasJMXProvider && jmxDeploymentScanner) {
			return true;
		}
		return false;
	}
	
	protected void verifyPrimaryScannerEnablement() {
		// Do Nothing
	}

	protected void modifyDeploymentScanners(ServerEvent event){
		String[] folders = getDeployLocationFolders(event.getServer());
		Trace.trace(Trace.STRING_FINER, "Adding " + folders.length + " Deployment Scanners via JMX"); //$NON-NLS-1$
		if( folders.length > 0 ) 
			ensureScannersAdded(event.getServer(), folders);
		Trace.trace(Trace.STRING_FINER, "Finished Adding Deployment Scanners via JMX"); //$NON-NLS-1$
	}

	protected void ensureScannersAdded(final IServer server, final String[] folders) {
		IJMXRunnable r = new IJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				ensureDeployLocationAdded(server, connection, folders);
			}
		};
		try {
			JBossJMXConnectionProviderModel.getDefault().run(server, r);
		} catch( JMXException jmxe ) {
			IStatus s = jmxe.getStatus();
			IStatus newStatus = new Status(s.getSeverity(), s.getPlugin(), IEventCodes.ADD_DEPLOYMENT_FOLDER_FAIL, 
					Messages.AddingJMXDeploymentFailed, s.getException());
			ServerLogger.getDefault().log(server, newStatus);
		}
	}
	
	private void ensureDeployLocationAdded(IServer server, 
			MBeanServerConnection connection, String[] folders2) throws Exception {
		for( int i = 0; i < folders2.length; i++ ) {
			String asURL = encode(folders2[i]);
			Trace.trace(Trace.STRING_FINER, "Adding Deployment Scanner: " + asURL);
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
