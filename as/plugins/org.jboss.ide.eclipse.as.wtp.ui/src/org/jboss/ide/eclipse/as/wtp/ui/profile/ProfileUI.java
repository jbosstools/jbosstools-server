/*************************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.wtp.ui.WTPOveridePlugin;

public class ProfileUI {
	
	private static ProfileUI instance;
	public static ProfileUI getDefault() {
		if( instance == null ) 
			instance = new ProfileUI();
		return instance;
	}
	
	ProfileUI() {
		
	}
	
	private HashMap<String, FragmentWrapper> map;
	private class FragmentWrapper {
		private String id;
		private String serverTypes;
		private String profile;
		private WizardFragment fragment;
		private IConfigurationElement element;
		private int weight;
		
		public FragmentWrapper(IConfigurationElement element) {
			id = element.getAttribute("id");
			profile = element.getAttribute("profile");
			serverTypes = element.getAttribute("serverTypes");
			this.element = element;
			String w = element.getAttribute("weight");
			try {
				Integer i = Integer.parseInt(w);
				weight = i.intValue();
			} catch(NumberFormatException nfe ) {
				// TODO log
				weight = 100;
			}
		}
		
		protected boolean supportsServerType(String servertype) {
			String[] typesAsArr = serverTypes.split(",");
			for( int i = 0; i < typesAsArr.length; i++ ) {
				if( typesAsArr[i].equals(servertype))
					return true;
			}
			return false;
		}
		
		public int getWeight() {
			return weight;
		}
		public WizardFragment getFragment() {
			try {
				if( fragment == null ) {
					fragment = (WizardFragment)element.createExecutableExtension("class");
				}
			} catch(CoreException ce) {
				// TODO log
				ce.printStackTrace();
			}
			return fragment;
		}
		
	}
	
	
	public WizardFragment[] getWizardFragments(String serverType, String profile) {
		getDefault().ensureLoaded();
		Collection<FragmentWrapper> all = map.values();
		ArrayList<FragmentWrapper> all2 = new ArrayList<FragmentWrapper>(all);
		Collections.sort(all2, new Comparator<FragmentWrapper>() {
			public int compare(FragmentWrapper arg0, FragmentWrapper arg1) {
				return arg0.getWeight() - arg1.getWeight();
			}
		});
		Iterator<FragmentWrapper> it = all.iterator();
		FragmentWrapper temp;
		WizardFragment tempFragment;
		ArrayList<WizardFragment> ret = new ArrayList<WizardFragment>();
		while(it.hasNext()) {
			temp = it.next();
			if( profile.equals(temp.profile) && temp.supportsServerType(serverType)) {
				tempFragment = temp.getFragment();
				if( tempFragment != null )
					ret.add(tempFragment);
			}
		}
		return (WizardFragment[]) ret.toArray(new WizardFragment[ret.size()]);
	}
	
	private void ensureLoaded() {
		HashMap<String, FragmentWrapper> temp = new HashMap<String, FragmentWrapper>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(WTPOveridePlugin.PLUGIN_ID, "ServerProfileWizardFragments"); //$NON-NLS-1$
		FragmentWrapper oneWrapper;
		for( int i = 0; i < cf.length; i++ ) {
			oneWrapper = new FragmentWrapper(cf[i]);
			temp.put(oneWrapper.id, oneWrapper);
		}
		map = temp;
	}
}