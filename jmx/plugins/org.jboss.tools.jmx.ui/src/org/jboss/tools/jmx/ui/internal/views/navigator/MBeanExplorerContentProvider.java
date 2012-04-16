/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.views.navigator;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.MBeanFeatureInfoWrapper;
import org.jboss.tools.jmx.core.tree.ErrorRoot;
import org.jboss.tools.jmx.core.tree.Node;
import org.jboss.tools.jmx.core.tree.ObjectNameNode;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.ui.Messages;

/**
 * Content provider for the view
 */
public class MBeanExplorerContentProvider implements IConnectionProviderListener,
        IStructuredContentProvider, ITreeContentProvider {
	
	public static class DelayProxy {
		public IConnectionWrapper wrapper;
		public DelayProxy(IConnectionWrapper wrapper) {
			this.wrapper = wrapper;
		}
	}
	
	private Viewer viewer;
	private HashMap<IConnectionWrapper, DelayProxy> loading;
    public MBeanExplorerContentProvider() {
    	ExtensionManager.addConnectionProviderListener(this);
    	loading = new HashMap<IConnectionWrapper, DelayProxy>();
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    	this.viewer = v;
    }

    public void dispose() {
    	ExtensionManager.removeConnectionProviderListener(this);
    }

    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    public Object getParent(Object child) {
    	if( child instanceof Root )
    		return ((Root)child).getConnection();
        if (child instanceof Node) 
            return ((Node) child).getParent();
        if( child instanceof MBeanFeatureInfoWrapper ) 
        	return ((MBeanFeatureInfoWrapper)child).getMBeanInfoWrapper().getParent();
        return null;
    }

    public Object[] getChildren(Object parent) {
    	if( parent == null ) return new Object[] {};
		if( parent instanceof IViewPart ) {
			return ExtensionManager.getAllConnections();
		}
		if( parent instanceof IConnectionWrapper && ((IConnectionWrapper)parent).isConnected()) {
			return loadAndGetRootChildren(parent);
		}
        if (parent instanceof ObjectNameNode) {
            ObjectNameNode node = (ObjectNameNode) parent;
            return node.getMbeanInfoWrapper().getMBeanFeatureInfos();
        }
        if( parent instanceof ErrorRoot ) {
        	return new Object[0];
        }
        if (parent instanceof Node) {
            Node node = (Node) parent;
            return node.getChildren();
        }
        return new Object[0];
    }

    protected synchronized Object[] loadAndGetRootChildren(final Object parent) {
		final IConnectionWrapper w = (IConnectionWrapper)parent;
		
		if( w.getRoot() != null ) 
			return getChildren(w.getRoot());
		
		// Must load the model
		Job job = new Job(Messages.LoadingJMXNodes) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					w.loadRoot(monitor);
				} catch( CoreException ce ) {
					return ce.getStatus();
				} finally {
					loading.remove(w);
				}
				Display.getDefault().asyncExec(new Runnable() { 
					public void run() {
						if( viewer instanceof StructuredViewer) 
							((StructuredViewer)viewer).refresh(parent);
						else
							viewer.refresh();
					}
				});
				return Status.OK_STATUS;
			}

		};
		
		if( loading.containsKey(((IConnectionWrapper)parent))) {
			return new Object[] { loading.get((IConnectionWrapper)parent)};
		}
		DelayProxy p = new DelayProxy(w);
		loading.put(w, p);
		job.schedule();
		return new Object[] { p };
    }

    public boolean hasChildren(Object parent) {
        if (parent instanceof ObjectNameNode) {
            ObjectNameNode node = (ObjectNameNode) parent;
            return (node.getMbeanInfoWrapper().getMBeanFeatureInfos().length > 0);
        }
        if (parent instanceof Node) {
            Node node = (Node) parent;
            return (node.getChildren().length > 0);
        }
        if (parent instanceof MBeanFeatureInfoWrapper) {
            return false;
        }
        if( parent instanceof IConnectionWrapper ) {
        	return ((IConnectionWrapper)parent).isConnected();
        }
        if( parent instanceof DelayProxy)
        	return false;
        return true;
    }

	public void connectionAdded(IConnectionWrapper connection) {
		fireRefresh(connection, true);
	}

	public void connectionChanged(IConnectionWrapper connection) {
		fireRefresh(connection, false);
	}

	public void connectionRemoved(IConnectionWrapper connection) {
		fireRefresh(connection, true);
	}

	private void fireRefresh(final IConnectionWrapper connection, final boolean full) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if( viewer != null ) {
					if(full || !(viewer instanceof StructuredViewer))
						viewer.refresh();
					else
						((StructuredViewer)viewer).refresh(connection);
				}
			}
		});
	}
}