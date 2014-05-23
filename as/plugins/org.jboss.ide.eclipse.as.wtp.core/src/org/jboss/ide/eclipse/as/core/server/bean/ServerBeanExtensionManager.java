/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.jboss.ide.eclipse.as.core.server.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public class ServerBeanExtensionManager {
	public static ServerBeanExtensionManager instance = null;
	public static ServerBeanExtensionManager getDefault() {
		if( instance == null ) {
			instance = new ServerBeanExtensionManager();
		}
		return instance;
	}
	
	
	private TypeProviderWrapper[] wrappers;
	public ServerBeanType[] getAllTypes() {
		if( wrappers == null ) {
			load();
		}
		return getAll(wrappers);
	}
	
	private void load() {
		ArrayList<TypeProviderWrapper> wrappers = new ArrayList<TypeProviderWrapper>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ASWTPToolsPlugin.PLUGIN_ID, "serverBeanTypeProvider"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			wrappers.add(new TypeProviderWrapper(cf[i]));
		}
		Collections.sort(wrappers, new Comparator<TypeProviderWrapper>() {
			public int compare(TypeProviderWrapper o1, TypeProviderWrapper o2) {
				return o1.getWeight() - o2.getWeight();
			}
		});
		this.wrappers = wrappers.toArray(new TypeProviderWrapper[wrappers.size()]);
	}
	
	private class TypeProviderWrapper {
		private IConfigurationElement element;
		private IServerBeanTypeProvider provider;
		private int weight;
		private Throwable errorLoadingProvider;
		public TypeProviderWrapper(IConfigurationElement el) {
			this.element = el;
			String weightS = el.getAttribute("weight");
			int weight = 0;
			if( weightS != null ) {
				try {
					weight = Integer.parseInt(weightS);
				} catch(NumberFormatException nfe) {
					// ignore
				}
			}
			this.weight = weight;
		}
		
		public int getWeight() {
			return weight;
		}
		
		private IServerBeanTypeProvider getProvider() {
			if( provider == null && errorLoadingProvider == null) {
				try {
					provider = (IServerBeanTypeProvider)element.createExecutableExtension("class");
				} catch(CoreException ce) {
					// todo log
					errorLoadingProvider = ce;
				}
			}
			return provider;
		}
		
	}
	
	private ServerBeanType[] getAll(TypeProviderWrapper[] providers) {
		ArrayList<ServerBeanType> list = new ArrayList<ServerBeanType>();
		IServerBeanTypeProvider p;
		for( int i = 0; i < providers.length; i++ ) {
			p = providers[i].getProvider();
			if( p != null ) {
				list.addAll(Arrays.asList(p.getServerBeanTypes()));
			}
		}
		return (ServerBeanType[]) list.toArray(new ServerBeanType[list.size()]);
	}
}
