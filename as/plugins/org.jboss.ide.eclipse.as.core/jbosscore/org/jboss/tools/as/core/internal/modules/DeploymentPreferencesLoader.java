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
package org.jboss.tools.as.core.internal.modules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;

/**
 * Replacement class for class DeploymentPreferenceLoader 
 */
public class DeploymentPreferencesLoader {
	public static final String DEPLOYMENT_PREFERENCES_KEY = "org.jboss.ide.eclipse.as.core.util.deploymentPreferenceKey"; //$NON-NLS-1$

	/**
	 * Load the preferences from either a server or a working copy
	 * @param server
	 * @return
	 */
	public static DeploymentPreferences loadPreferencesFromServer(IServerAttributes server) {
		String xml = ((Server)server).getAttribute(DEPLOYMENT_PREFERENCES_KEY, (String)null);
		ByteArrayInputStream bis = null;
		if( xml != null ) {
			bis = new ByteArrayInputStream(xml.getBytes());
		}
		return new DeploymentPreferences(bis);
	}

	/**
	 * Save the preferences to an output stream
	 * @param os
	 * @param prefs
	 * @throws IOException
	 */
	public static void savePreferences(OutputStream os, DeploymentPreferences prefs) throws IOException {
		prefs.getMemento().save(os);
	}
	
	public static void savePreferencesToServerWorkingCopy(ServerAttributeHelper helper, DeploymentPreferences prefs) {
		savePreferencesToServerWorkingCopy(helper.getWorkingCopy(), prefs);
	}
	
	public static void savePreferencesToServerWorkingCopy(IServerWorkingCopy wc, DeploymentPreferences prefs) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DeploymentPreferencesLoader.savePreferences(bos, prefs);
			String asXML = new String(bos.toByteArray());
			wc.setAttribute(DeploymentPreferencesLoader.DEPLOYMENT_PREFERENCES_KEY, asXML);
		} catch(IOException ioe) {
			// Should never happen since this is a simple byte array output stream
			JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
				"Could not save module deployment preferences to server " + wc.getOriginal().getName(), ioe)); //$NON-NLS-1$
		}
	}

}
