/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.local.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.tools.jmx.local.ui.JVMLabelProviderDelegate;

public class JmxLocalExtensionManager {
	private static JmxLocalExtensionManager instance;
	public static synchronized JmxLocalExtensionManager getDefault() {
		if( instance == null ) {
			instance = new JmxLocalExtensionManager();
		}
		return instance;
	}
	
	private ArrayList<WeightedLabelProvider> weightedLabelProviders = null;
	public JmxLocalExtensionManager() {
		loadLabelProviders();
	}
	
	/** The method used to load / instantiate the pollers */
	public void loadLabelProviders() {
		weightedLabelProviders = new ArrayList<WeightedLabelProvider>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(Activator.PLUGIN_ID, "jvmConnectionLabelProvider"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			WeightedLabelProvider sspt = new WeightedLabelProvider(cf[i]);
			weightedLabelProviders.add(sspt);
		}
		// Sort
		Collections.sort(weightedLabelProviders, new Comparator<WeightedLabelProvider>(){
			public int compare(WeightedLabelProvider o1, WeightedLabelProvider o2) {
				return o1.getWeight() - o2.getWeight();
			}});
	}
	
	public JVMLabelProviderDelegate[] getJvmConnectionLabelProviders() {
		ArrayList<JVMLabelProviderDelegate> ret = new ArrayList<JVMLabelProviderDelegate>();
		JVMLabelProviderDelegate h;
		for( Iterator<WeightedLabelProvider> i = weightedLabelProviders.iterator(); i.hasNext(); ) {
			h = i.next().getLabelProvider();
			if( h != null )
				ret.add(h);
		}
		return ret.toArray(new JVMLabelProviderDelegate[ret.size()]);
	}
	
	private static class WeightedLabelProvider {
		private IConfigurationElement el;
		private JVMLabelProviderDelegate handler;
		private int weight = Integer.MIN_VALUE;
		private boolean loadFailed = false;
		
		public WeightedLabelProvider(IConfigurationElement el) {
			this.el = el;
		}
		public int getWeight() {
			if( weight == Integer.MIN_VALUE ) {
				String s = el.getAttribute("weight");
				try {
					if( s != null ) { 
						int i = Integer.parseInt(s);
						weight = i;
					}
				} catch(NumberFormatException nfe) {
					weight = -1;
				}
			}
			return weight;
		}
		
		public JVMLabelProviderDelegate getLabelProvider() {
			if( handler == null && !loadFailed) {
				// load handler
				try {
					handler = (JVMLabelProviderDelegate)el.createExecutableExtension("class"); //$NON-NLS-1$
				} catch(CoreException ce) {
					loadFailed = true;
				}
			}
			return handler;
		}
	}
}
