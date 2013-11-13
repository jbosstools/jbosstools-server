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
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractDeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.AS7DeploymentScannerUtility.Scanner;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class LocalJBoss7DeploymentScannerAdditions extends AbstractDeploymentScannerAdditions {
	public LocalJBoss7DeploymentScannerAdditions() {
		
	}
	public boolean accepts(IServer server) {
		ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, null);
		boolean usesManagement = props != null && 
			props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT;
		IServerModeDetails det = (IServerModeDetails)Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class);
		return usesManagement && det != null;
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
		Scanner defaultScanner = loadDefaultScannerFromServer(server);
		int existingScannerTimeout = (defaultScanner == null ? 5000 : defaultScanner.getTimeout());
		int existingScannerInterval = (defaultScanner == null ? AS7DeploymentScannerUtility.IGNORE : defaultScanner.getInterval());
		
		// Properties file of format like:  JBossToolsScanner4=/some/folder
		Iterator<String> lastStartup = props.keySet().iterator();
		String k = null;
		String v = null;
		while(lastStartup.hasNext()) {
			k = (String)lastStartup.next();
			v = (String)props.get(k); 
			
			// Sometimes the returned value from app server may have quotes
			String withoutQuotes = v.trim();
			withoutQuotes = withoutQuotes.startsWith("\"") ? withoutQuotes.substring(1) : withoutQuotes;  //$NON-NLS-1$
			withoutQuotes = withoutQuotes.endsWith("\"") ? withoutQuotes.substring(0, withoutQuotes.length()-1) : withoutQuotes;  //$NON-NLS-1$
			
			if( !asList.contains(v) && !asList.contains(withoutQuotes))
				removed.add(k);
			else {
				added.remove(withoutQuotes);
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
		 
		
		int defaultInterval = server.getAttribute(IJBossToolingConstants.PROPERTY_SCANNER_INTERVAL, existingScannerInterval);
		int defaultTimeout = server.getAttribute(IJBossToolingConstants.PROPERTY_SCANNER_TIMEOUT, existingScannerTimeout);
		
		// Do the adds
		i = added.iterator();
		String path;
		String newScannerName;
		while(i.hasNext()) {
			path = i.next();
			newScannerName = findNextScannerName(props);
			IStatus s = util.addDeploymentScanner(server, newScannerName, path, 
					defaultInterval, defaultTimeout);
			if( s.isOK()){
				props.put(newScannerName, path);
			}
			Trace.trace(Trace.STRING_FINER, "Added Deployment Scanner: success="+s.isOK() + ", " + scannerName + ":" + props.get(newScannerName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		Trace.trace(Trace.STRING_FINER, "Finished Adding AS7 Deployment Scanners"); //$NON-NLS-1$
	}
	
	protected void ensureScannersRemoved(final IServer server, final String[] folders) {
		Map<String, String> props = loadScannersFromServer(server);
		AS7DeploymentScannerUtility util = new AS7DeploymentScannerUtility(); 
		Iterator<String> i = props.keySet().iterator();
		while(i.hasNext()) {
			String scannerName = i.next();
			if( scannerName.startsWith(AS7DeploymentScannerUtility.SCANNER_PREFIX)) {
				util.removeDeploymentScanner(server, scannerName);
			}
		}
	}

	protected Map<String, String> loadScannersFromServer(IServer server) {
		return new AS7DeploymentScannerUtility().getDeploymentScannersFromServer(server, false);
	}
	
	protected Scanner loadDefaultScannerFromServer(IServer server) {
		return new AS7DeploymentScannerUtility().getDefaultDeploymentScanner(server);
	}

	
	protected String findNextScannerName(Map<String,String> props) {
		int i = 1;
		while( props.get(AS7DeploymentScannerUtility.SCANNER_PREFIX + i) != null ) {
			i++;
		}
		return AS7DeploymentScannerUtility.SCANNER_PREFIX + i;
	}
	
	/*
	 * An internal method which lets us know whether this app server version
	 * persists changes made to the deployment scanner model or not. 
	 */
	@Override
	public boolean persistsScannerChanges() {
		return true;
	}

	/*
	 * An internal method which lets us know whether this app server version
	 * can customize the scanner's interval 
	 */
	@Override
	public boolean canCustomizeInterval() {
		return true;
	}

	/*
	 * An internal method which lets us know whether this app server version
	 * can customize the scanner's timeout 
	 */
	@Override
	public boolean canCustomizeTimeout() {
		return true;
	}
	@Override
	public void suspendScanners(IServer server)
			throws UnsupportedOperationException {
		// Cannot implement easily without modifying a standalone.xml
		throw new UnsupportedOperationException();
	}
	@Override
	public void resumeScanners(IServer server)
			throws UnsupportedOperationException {
		// Cannot implement easily without modifying a standalone.xml
		throw new UnsupportedOperationException();
	}

}
