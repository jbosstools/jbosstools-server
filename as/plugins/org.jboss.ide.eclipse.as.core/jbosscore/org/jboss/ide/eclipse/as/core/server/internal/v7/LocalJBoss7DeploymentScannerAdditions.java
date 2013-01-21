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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractDeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;

public class LocalJBoss7DeploymentScannerAdditions extends AbstractDeploymentScannerAdditions {
	
	public boolean accepts(IServer server) {
		if( !LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(getServerMode(server)))
			return false;

		ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, null);
		boolean usesManagement = props != null && 
			props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT;
		return usesManagement;
	}
	
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
	
}
