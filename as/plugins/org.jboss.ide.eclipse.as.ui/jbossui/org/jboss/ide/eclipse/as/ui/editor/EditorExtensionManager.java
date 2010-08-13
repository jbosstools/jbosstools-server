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
package org.jboss.ide.eclipse.as.ui.editor;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class EditorExtensionManager {

	private static EditorExtensionManager instance;
	public static EditorExtensionManager getDefault() {
		if( instance == null )
			instance = new EditorExtensionManager();
		return instance;
	}
	
	private HashMap<String, IDeploymentTypeUI> publishMethodUIMap = null;
	public IDeploymentTypeUI getPublishPreferenceUI(String deployType) {
		if( publishMethodUIMap == null )
			loadPublishPreferenceEditors();
		return publishMethodUIMap.get(deployType);
	}
	
	public IDeploymentTypeUI[] getPublishPreferenceUIs() {
		if( publishMethodUIMap == null )
			loadPublishPreferenceEditors();
		Collection<IDeploymentTypeUI> col = publishMethodUIMap.values();
		return (IDeploymentTypeUI[]) col.toArray(new IDeploymentTypeUI[col.size()]);
	}
	private void loadPublishPreferenceEditors() {
		publishMethodUIMap = new HashMap<String, IDeploymentTypeUI>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(
				JBossServerUIPlugin.PLUGIN_ID, "DeployMethodUI"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			try {
				IDeploymentTypeUI ui = (IDeploymentTypeUI) cf[i].createExecutableExtension("class"); //$NON-NLS-1$
				if( ui != null && cf[i].getAttribute("deployMethodId") != null) { //$NON-NLS-1$
					publishMethodUIMap.put(cf[i].getAttribute("deployMethodId"), ui); //$NON-NLS-1$
				}
			} catch(CoreException ce ) {
				// TODO log
			}
		}
	}
}
