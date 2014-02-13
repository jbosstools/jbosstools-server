/******************************************************************************* 
 * Copyright (c) 2010,2013 Red Hat, Inc. 
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

@Deprecated
public class EditorExtensionManager {

	private static final String EXTENSION_ID = "DeployMethodUI"; //$NON-NLS-1$
	private static final String ELEMENT_ID = "deployMethodId"; //$NON-NLS-1$
	private static final String CLAZZ = "class"; //$NON-NLS-1$
	
	private static EditorExtensionManager instance;
	public static EditorExtensionManager getDefault() {
		if( instance == null )
			instance = new EditorExtensionManager();
		return instance;
	}
	
	private HashMap<String, IDeploymentTypeUI> publishMethodUIMap = null;
	
	@Deprecated
	public IDeploymentTypeUI getPublishPreferenceUI(String deployType) {
		ensureExtensionsLoaded();
		return publishMethodUIMap.get(deployType);
	}
	
	@Deprecated
	public IDeploymentTypeUI[] getPublishPreferenceUIs() {
		ensureExtensionsLoaded();
		Collection<IDeploymentTypeUI> col = publishMethodUIMap.values();
		return (IDeploymentTypeUI[]) col.toArray(new IDeploymentTypeUI[col.size()]);
	}
	
	private void ensureExtensionsLoaded() {
		if( publishMethodUIMap == null )
			loadExtensions();		
	}
	private void loadExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(
				JBossServerUIPlugin.PLUGIN_ID, EXTENSION_ID);
		loadPublishPreferenceEditors(cf);
	}
	
	@Deprecated
	private void loadPublishPreferenceEditors(IConfigurationElement[] cf) {
		publishMethodUIMap = new HashMap<String, IDeploymentTypeUI>();
		for( int i = 0; i < cf.length; i++ ) {
			try {
				IDeploymentTypeUI ui = (IDeploymentTypeUI) cf[i].createExecutableExtension(CLAZZ);
				if( ui != null && cf[i].getAttribute(ELEMENT_ID) != null) { 
					publishMethodUIMap.put(cf[i].getAttribute(ELEMENT_ID), ui);
				}
			} catch(CoreException ce ) {
				JBossServerUIPlugin.log(ce.getStatus());
			}
		}
	}
}
