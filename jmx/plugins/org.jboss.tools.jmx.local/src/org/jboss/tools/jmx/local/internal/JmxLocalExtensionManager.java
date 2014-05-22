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
import org.jboss.tools.jmx.local.IJvmConnectionHandler;

public class JmxLocalExtensionManager {
	private static JmxLocalExtensionManager instance;
	public static synchronized JmxLocalExtensionManager getDefault() {
		if( instance == null ) {
			instance = new JmxLocalExtensionManager();
		}
		return instance;
	}
	
	
	
	private ArrayList<HandlerWrapper> handlers = null;
	public JmxLocalExtensionManager() {
		loadJvmHandlers();
	}
	
	/** The method used to load / instantiate the pollers */
	public void loadJvmHandlers() {
		handlers = new ArrayList<HandlerWrapper>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(Activator.PLUGIN_ID, "jvmConnectionHandler"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			HandlerWrapper sspt = new HandlerWrapper(cf[i]);
			handlers.add(sspt);
		}
		// Sort
		Collections.sort(handlers, new Comparator<HandlerWrapper>(){
			public int compare(HandlerWrapper o1, HandlerWrapper o2) {
				return o1.getWeight() - o2.getWeight();
			}});
	}
	
	public IJvmConnectionHandler[] getJvmConnectionHandlers() {
		ArrayList<IJvmConnectionHandler> ret = new ArrayList<IJvmConnectionHandler>();
		IJvmConnectionHandler h;
		for( Iterator<HandlerWrapper> i = handlers.iterator(); i.hasNext(); ) {
			h = i.next().getHandler();
			if( h != null )
				ret.add(h);
		}
		return ret.toArray(new IJvmConnectionHandler[ret.size()]);
	}
	
	private static class HandlerWrapper {
		private IConfigurationElement el;
		private IJvmConnectionHandler handler;
		private boolean loadFailed = false;
		public HandlerWrapper(IConfigurationElement el) {
			this.el = el;
		}
		public int getWeight() {
			String s = el.getAttribute("weight");
			try {
				if( s != null ) { 
					int i = Integer.parseInt(s);
					return i;
				}
			} catch(NumberFormatException nfe) {
			}
			return -1;
		}
		public IJvmConnectionHandler getHandler() {
			if( handler == null && !loadFailed) {
				// load handler
				try {
					handler = (IJvmConnectionHandler)el.createExecutableExtension("class"); //$NON-NLS-1$
				} catch(CoreException ce) {
					loadFailed = true;
				}
			}
			return handler;
		}
	}
}
