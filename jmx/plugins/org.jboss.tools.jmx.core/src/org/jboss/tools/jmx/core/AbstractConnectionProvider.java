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
package org.jboss.tools.jmx.core;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * A convenience superclass for the purposes of implementing most of the
 * boiler-plate code required in an IConnectionProvider
 */
public abstract class AbstractConnectionProvider implements IConnectionProvider {

	private ArrayList<IConnectionProviderListener> listeners =
			new ArrayList<IConnectionProviderListener>();

	public AbstractConnectionProvider() {
		// no-arg constructor
	}

	@Override
	public void addListener(IConnectionProviderListener listener) {
		if( !listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void removeListener(IConnectionProviderListener listener) {
		listeners.remove(listener);
	}

	protected void fireAllAdded(IConnectionWrapper[] wrappers) {
		for( int i = 0; i < wrappers.length; i++ ) {
			fireAdded(wrappers[i]);
		}
	}

	protected void fireAllChanged(IConnectionWrapper[] wrappers) {
		for( int i = 0; i < wrappers.length; i++ ) {
			fireChanged(wrappers[i]);
		}
	}

	protected void fireAllRemoved(IConnectionWrapper[] wrappers) {
		for( int i = 0; i < wrappers.length; i++ ) {
			fireRemoved(wrappers[i]);
		}
	}

	public void fireAdded(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();) {
			try {
				i.next().connectionAdded(wrapper);
			} catch(RuntimeException re) {
				// Intentionally ignore. This is just to protect against a bad implementer blowing away the stack
			}
		}
	}

	public void fireChanged(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();) {
			try {
				i.next().connectionChanged(wrapper);
			} catch(RuntimeException re) {
				// Intentionally ignore. This is just to protect against a bad implementer blowing away the stack
			}
		}
	}

	public void fireRemoved(IConnectionWrapper wrapper) {
		for(Iterator<IConnectionProviderListener> i = listeners.iterator(); i.hasNext();) {
			try {
				i.next().connectionRemoved(wrapper);
			} catch(RuntimeException re) {
				// Intentionally ignore. This is just to protect against a bad implementer blowing away the stack
			}
		}
	}
}
