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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.ui.IBrowseBehavior;
import org.jboss.ide.eclipse.as.ui.IExploreBehavior;
import org.jboss.ide.eclipse.as.ui.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class EditorExtensionManager {

	private static final String EXTENSION_ID = "DeployMethodUI"; //$NON-NLS-1$
	private static final String ELEMENT_ID = "deployMethodId"; //$NON-NLS-1$
	private static final String CLAZZ = "class"; //$NON-NLS-1$
	private static final String LAUNCH_TAB_PROVIDER = "launchTabProvider"; //$NON-NLS-1$
	private static final String BROWSE_BEHAVIOR = "browseBehavior"; //$NON-NLS-1$
	private static final String EXPLORE_BEHAVIOR = "exploreBehavior"; //$NON-NLS-1$
	private static final String SERVER_TYPES = "serverTypes"; //$NON-NLS-1$
	
	private static EditorExtensionManager instance;
	public static EditorExtensionManager getDefault() {
		if( instance == null )
			instance = new EditorExtensionManager();
		return instance;
	}
	
	private HashMap<String, IDeploymentTypeUI> publishMethodUIMap = null;
	private HashMap<String, IBrowseBehavior> browseMap = null;
	private HashMap<String, IExploreBehavior> exploreMap = null;
	private HashMap<String, HashMap<String,List<IJBossLaunchTabProvider>>> launchTabs = null;
	
	public IDeploymentTypeUI getPublishPreferenceUI(String deployType) {
		ensureExtensionsLoaded();
		return publishMethodUIMap.get(deployType);
	}
	
	public List<IJBossLaunchTabProvider> getLaunchTabProviders(String mode, String serverType) {
		ensureExtensionsLoaded();
		HashMap<String, List<IJBossLaunchTabProvider>> modeProviders =
				launchTabs.get(mode);
		if( modeProviders != null ) {
			List<IJBossLaunchTabProvider> list = modeProviders.get(serverType);
			if( list != null )
				return new ArrayList<IJBossLaunchTabProvider>(list);
		}
		return null;
	}
	
	public IBrowseBehavior getBrowseBehavior(String mode) {
		ensureExtensionsLoaded();
		return browseMap.get(mode);
	}

	public IExploreBehavior getExploreBehavior(String mode) {
		ensureExtensionsLoaded();
		return exploreMap.get(mode);
	}

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
		loadBrowseMap(cf);
		loadExploreMap(cf);
		loadLaunchTabs(cf);
	}
	
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

	private void loadBrowseMap(IConfigurationElement[] cf) {
		browseMap = new HashMap<String, IBrowseBehavior>();
		for( int i = 0; i < cf.length; i++ ) {
			try {
				IBrowseBehavior ui = (IBrowseBehavior) cf[i].createExecutableExtension(BROWSE_BEHAVIOR);
				if( ui != null && cf[i].getAttribute(ELEMENT_ID) != null) { 
					browseMap.put(cf[i].getAttribute(ELEMENT_ID), ui); 
				}
			} catch(CoreException ce ) {
				// DO not log, as field is optional
			}
		}
	}

	private void loadExploreMap(IConfigurationElement[] cf) {
		exploreMap = new HashMap<String, IExploreBehavior>();
		for( int i = 0; i < cf.length; i++ ) {
			try {
				IExploreBehavior ui = (IExploreBehavior) cf[i].createExecutableExtension(EXPLORE_BEHAVIOR);
				if( ui != null && cf[i].getAttribute(ELEMENT_ID) != null) { 
					exploreMap.put(cf[i].getAttribute(ELEMENT_ID), ui); 
				}
			} catch(CoreException ce ) {
				// DO not log, as field is optional
			}
		}
	}
	private void loadLaunchTabs(IConfigurationElement[] cf) {
		launchTabs = new HashMap<String, HashMap<String,List<IJBossLaunchTabProvider>>>();
		for( int i = 0; i < cf.length; i++ ) {
			String mode = cf[i].getAttribute(ELEMENT_ID);
			IConfigurationElement[] providers = cf[i].getChildren(LAUNCH_TAB_PROVIDER);
			HashMap<String, List<IJBossLaunchTabProvider>> providerMap = launchTabs.get(mode);
			if( providerMap == null ) {
				providerMap = new HashMap<String, List<IJBossLaunchTabProvider>>();
				launchTabs.put(mode, providerMap);
			}
			for( int j = 0; j < providers.length; j++ ) {
				try {
					String serverTypes = providers[j].getAttribute(SERVER_TYPES);
					String[] types = serverTypes == null ? new String[0] :
						serverTypes.split(","); //$NON-NLS-1$
					IJBossLaunchTabProvider provider = (IJBossLaunchTabProvider) 
							providers[j].createExecutableExtension(CLAZZ);
					if( provider != null ) {
						for( int k = 0; k < types.length; k++ ) {
							List<IJBossLaunchTabProvider> l = providerMap.get(types[k]);
							if( l == null ) {
								l = new ArrayList<IJBossLaunchTabProvider>();
								providerMap.put(types[k], l);
							}
							l.add(provider);
						}
					}
				} catch(CoreException ce ) {
					// DO not log, as field is optional
				}

			}
		}
	}
}
