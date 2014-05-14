package org.jboss.tools.jmx.core;

import java.util.ArrayList;
import java.util.Iterator;

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
