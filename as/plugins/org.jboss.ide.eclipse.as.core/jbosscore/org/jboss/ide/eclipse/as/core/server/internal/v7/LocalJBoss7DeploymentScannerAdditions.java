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
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class LocalJBoss7DeploymentScannerAdditions extends UnitedServerListener {
	protected boolean accepts(IServer server) {
		ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, null);
		boolean usesManagement = props != null && 
			props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT;
		if(usesManagement) {
			return true;
		}
		return false;
	}
	
	private final static String SCANNER_PROP_FILE = "as7Scanners.properties"; //$NON-NLS-1$
	
	/**
	 * Ensure the following folders are added to a deployment scanner. 
	 * Depending on the impl and server version, you may simply add all of the folders, 
	 * or, you may need to discover what's been removed and added separately. 
	 * 
	 * @param server
	 * @param folders
	 */
	protected void ensureScannersAdded(final IServer server, final String[] folders) {
		Trace.trace(Trace.STRING_FINER, "Adding AS7 Deployment Scanners"); //$NON-NLS-1$
		ArrayList<String> asList = new ArrayList<String>();
		asList.addAll(Arrays.asList(folders));
		ArrayList<String> added = new ArrayList<String>(); // list of the paths
		added.addAll(Arrays.asList(folders));
		ArrayList<String> removed = new ArrayList<String>(); // list of the scanner names
		
		Map<String, String> props = loadScannersFromServer(server);
		
		// Properties file of format like:  JBossToolsScanner4=/some/folder
		Iterator<String> lastStartup = props.keySet().iterator();
		String k = null;
		String v = null;
		while(lastStartup.hasNext()) {
			k = (String)lastStartup.next();
			v = (String)props.get(k);
			if( !asList.contains(v)) 
				removed.add(k);
			else {
				added.remove(v);
				Trace.trace(Trace.STRING_FINEST, "Unchanged Deployment Scanner " + k + ":" + v); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		AS7DeploymentScannerUtility util = new AS7DeploymentScannerUtility(); 
		 
		 // Do the removes
		Iterator<String> i = removed.iterator();
		String scannerName = null;
		while(i.hasNext()) {
			scannerName = i.next();
			IStatus s = util.removeDeploymentScanner(server, scannerName);
			if( s.isOK()) {
				props.remove(scannerName);
			}
			Trace.trace(Trace.STRING_FINER, "Removed Deployment Scanner: success="+s.isOK() + ", " + scannerName + ":" + props.get(scannerName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		 
		// Do the adds
		i = added.iterator();
		String path;
		String newScannerName;
		while(i.hasNext()) {
			path = i.next();
			newScannerName = findNextScannerName(props);
			IStatus s = util.addDeploymentScanner(server, newScannerName, path);
			if( s.isOK()){
				props.put(newScannerName, path);
				util.updateDeploymentScannerInterval(server, newScannerName, 5000);
			}
			Trace.trace(Trace.STRING_FINER, "Added Deployment Scanner: success="+s.isOK() + ", " + scannerName + ":" + props.get(newScannerName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		Trace.trace(Trace.STRING_FINER, "Finished Adding AS7 Deployment Scanners"); //$NON-NLS-1$
	}
	
	protected Map<String, String> loadScannersFromServer(IServer server) {
		return new AS7DeploymentScannerUtility().getDeploymentScannersFromServer(server, false);
	}
	
	protected String findNextScannerName(Map<String,String> props) {
		int i = 1;
		while( props.get(AS7DeploymentScannerUtility.SCANNER_PREFIX + i) != null ) {
			i++;
		}
		return AS7DeploymentScannerUtility.SCANNER_PREFIX + i;
	}
	
	public void serverChanged(final ServerEvent event) {
		if( accepts(event.getServer()) && serverSwitchesToState(event, IServer.STATE_STARTED)){
			new Job(getJobName(event.getServer())) {
				protected IStatus run(IProgressMonitor monitor) {
					modifyDeploymentScanners(event);
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}
	
	protected String getJobName(IServer server) {
		return Messages.bind(Messages.UpdateDeploymentScannerJobName, server.getName() );
	}
	
	protected void modifyDeploymentScanners(ServerEvent event){
		String[] folders = getDeployLocationFolders(event.getServer());
		ensureScannersAdded(event.getServer(), folders);
	}
	
	protected String[] getDeployLocationFolders(IServer server) {
		JBossServer ds = (JBossServer)ServerConverter.getJBossServer(server);
		ArrayList<String> folders = new ArrayList<String>();
		// add the server folder deploy loc. first
		String insideServer = ds.getDeployFolder(JBossServer.DEPLOY_SERVER);
		String metadata = JBossServer.getDeployFolder(ds, JBossServer.DEPLOY_METADATA);
		String custom = JBossServer.getDeployFolder(ds, JBossServer.DEPLOY_CUSTOM);
		String type = ds.getDeployLocationType();
		String serverHome = null;
		if (server != null && server.getRuntime()!= null && server.getRuntime().getLocation() != null) {
			serverHome = server.getRuntime().getLocation().toString();
		}
		folders.add(insideServer);
		if( type.equals(JBossServer.DEPLOY_METADATA) && !folders.contains(metadata))
			folders.add(metadata);
		if( type.equals(JBossServer.DEPLOY_CUSTOM) && !folders.contains(custom) && !custom.equals(serverHome))
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
		return folders2;
	}
}
