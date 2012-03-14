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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;

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
		ArrayList<String> asList = new ArrayList<String>();
		asList.addAll(Arrays.asList(folders));
		ArrayList<String> added = new ArrayList<String>(); // list of the paths
		added.addAll(Arrays.asList(folders));
		ArrayList<String> removed = new ArrayList<String>(); // list of the scanner names
		
		IPath p = JBossServerCorePlugin.getServerStateLocation(server).append(SCANNER_PROP_FILE);
		Properties props = new Properties();
		if( p.toFile().exists()) {
			try {
				props.load(new FileInputStream(p.toFile()));
			} catch( IOException ioe) {
				// shouldnt happen. Log this
				Status failStat = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						"Unable to read deployment scanner property file " + p.toFile().getAbsolutePath(), ioe); //$NON-NLS-1$
				JBossServerCorePlugin.log(failStat);
			}
		}
		 
		Iterator<Object> i2 = props.keySet().iterator();
		String k = null;
		String v = null;
		while(i2.hasNext()) {
			k = (String)i2.next();
			v = (String)props.get(k);
			if( !asList.contains(v)) 
				removed.add(k);
			else {
				added.remove(v);
			}
		}

		 
		 // Do the removes
		Iterator<String> i = removed.iterator();
		String scannerName = null;
		while(i.hasNext()) {
			scannerName = i.next();
			IStatus s = removeOneFolder(server, scannerName);
			if( s.isOK()) {
				props.remove(scannerName);
			}
		}
		 
		// Do the adds
		i = added.iterator();
		String path;
		String newScannerName;
		while(i.hasNext()) {
			path = i.next();
			newScannerName = findNextScannerName(props);
			IStatus s = addOneFolder(server, newScannerName, path);
			if( s.isOK()){
				props.put(newScannerName, path);
			}
		}
		 
		 // Write the file out
		if( added.size() != 0 || removed.size() != 0 ) {
			try {
				props.store(new FileOutputStream(p.toFile()), "Deployment scanners for the application server"); //$NON-NLS-1$
			} catch( IOException ioe) {
				Status failStat = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						 "Unable to save deployment scanner property file " + p.toFile().getAbsolutePath(), ioe); //$NON-NLS-1$
				JBossServerCorePlugin.log(failStat);
			}
		}
	}
	
	private static final String SCANNER_PREFIX = "JBossToolsScanner"; //$NON-NLS-1$
	protected String findNextScannerName(Properties props) {
		int i = 1;
		while( props.get(SCANNER_PREFIX + i) != null ) {
			i++;
		}
		return SCANNER_PREFIX + i;
	}
	
	protected IStatus addOneFolder(final IServer server, String scannerName, final String folder) {
		ModelNode op = new ModelNode();
		op.get("operation").set("add"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", scannerName); //$NON-NLS-1$
		op.get("path").set(folder); //$NON-NLS-1$
		final String request = op.toJSONString(true);
		return execute(server, request);
	}

	protected IStatus removeOneFolder(final IServer server, String scannerName) {
		ModelNode op = new ModelNode();
		op.get("operation").set("remove"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", scannerName); //$NON-NLS-1$
		final String request = op.toJSONString(true);
		return execute(server, request);
	}

	protected IStatus execute(final IServer server, final String request) {
		try {
	        String resultJSON = JBoss7ManagerUtil.executeWithService(new JBoss7ManagerUtil.IServiceAware<String>() {
	            public String execute(IJBoss7ManagerService service) throws Exception {
	                return service.execute(new AS7ManagementDetails(server), request);
	            }
	        }, server);
	        ModelNode result = ModelNode.fromJSONString(resultJSON);
	        return Status.OK_STATUS;
		} catch( Exception e ) {
			// TODO Throw new checked exception
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e.getMessage(), e);
		}
	}

	
	public void serverChanged(ServerEvent event) {
		IServer server = event.getServer();
		if( accepts(server)) {
			int eventKind = event.getKind();
			if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
				// server change event
				if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
					if( event.getServer().getServerState() == IServer.STATE_STARTED ) {
						modifyDeploymentScanners(event);
					}
				}
			}
		}
	}
	
	protected void modifyDeploymentScanners(ServerEvent event){
		String[] folders = getDeployLocationFolders(event.getServer());
		ensureScannersAdded(event.getServer(), folders);
	}
	
	protected String[] getDeployLocationFolders(IServer server) {
		JBossServer ds = ServerConverter.getJBossServer(server);
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
