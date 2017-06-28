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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionCategory;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.MBeanFeatureInfoWrapper;
import org.jboss.tools.jmx.core.tree.ErrorRoot;
import org.jboss.tools.jmx.core.tree.Node;
import org.jboss.tools.jmx.core.tree.ObjectNameNode;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.internal.actions.RefreshActionState;

/**
 * Content provider for the view
 */
public class MBeanExplorerContentProvider implements IConnectionProviderListener,
        IStructuredContentProvider, ITreeContentProvider {
	
	public static class DelayProxy {
		public Object wrapped;
		public DelayProxy(Object wrapped) {
			this.wrapped = wrapped;
		}
	}
	
	private Viewer viewer;
	private HashMap<IConnectionWrapper, DelayProxy> loading;
	private HashMap<ObjectNameNode, DelayProxy> loadingObjectNameNode;
    public MBeanExplorerContentProvider() {
    	ExtensionManager.addConnectionProviderListener(this);
    	loading = new HashMap<>();
    	loadingObjectNameNode = new HashMap<>();
    }
    
    @Override
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    	this.viewer = v;
    }
    
    @Override
    public void dispose() {
    	ExtensionManager.removeConnectionProviderListener(this);
    }
    
    @Override
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }
    
    @Override
    public Object getParent(Object child) {
    	if( child instanceof Root )
    		return ((Root)child).getConnection();
        if (child instanceof Node) 
            return ((Node) child).getParent();
        if( child instanceof MBeanFeatureInfoWrapper ) 
        	return ((MBeanFeatureInfoWrapper)child).getMBeanInfoWrapper().getParent();
        return null;
    }

    private ProviderCategory[] categories;
    
    public static class ProviderCategory {
    	private String id;
    	public ProviderCategory(String id) {
    		this.id = id;
    	}
    	public String getId() {
    		return id;
    	}
    }
    
    /**
     * This will return top level items as either categories (if they have them)
     * or any providers that have no categories
     * 
     * @return
     */
    private synchronized Object[] getTopLevelElements() {
    	if( this.categories != null ) {
    		return this.categories;
    	}
    	
		// Return the providers
    	IConnectionProvider[] providers = ExtensionManager.getProviders();
    	Set<String> categoryIds = new TreeSet<>();
    	ArrayList<ProviderCategory> categories = new ArrayList<>();
    	ArrayList<IConnectionProvider> unaffiliated = new ArrayList<>();
    	for( int i = 0; i < providers.length; i++ ) {
    		if( providers[i] instanceof IConnectionCategory ) {
    			String id = ((IConnectionCategory)providers[i]).getCategoryId();
    			if( !categoryIds.contains(id)) {
    				categoryIds.add(id);
    				categories.add(new ProviderCategory(id));
    			}
    		} else {
    			unaffiliated.add(providers[i]);
    		}
    	}
    	ArrayList<Object> toReturn = new ArrayList<>();
    	this.categories = categories.toArray(new ProviderCategory[categories.size()]);
    	toReturn.addAll(categories);
    	toReturn.addAll(unaffiliated);
    	return toReturn.toArray(new Object[toReturn.size()]);
    }
    
    private IConnectionWrapper[] findAllConnectionsWithCategory(String id) {
    	ArrayList<IConnectionWrapper> ret = new ArrayList<>();
    	IConnectionProvider[] providers = ExtensionManager.getProviders();
    	for( int i = 0; i < providers.length; i++ ) {
    		if( providers[i] instanceof IConnectionCategory ) {
    			String id2 = ((IConnectionCategory)providers[i]).getCategoryId();
    			if( id.equals(id2)) {
    				ret.addAll(Arrays.asList(providers[i].getConnections()));
    			}
    		}
    	}
    	return ret.toArray(new IConnectionWrapper[ret.size()]);
    }

	@Override
	public Object[] getChildren(Object parent) {
		if( parent == null ) {
			return new Object[] {};
		}
		if( parent instanceof IViewPart ) {
			return getTopLevelElements();
		}
		if( parent instanceof ProviderCategory) {
			String id = ((ProviderCategory)parent).getId();
			return findAllConnectionsWithCategory(id);
		}
		if( parent instanceof IConnectionProvider ) {
			// Return the providers
			return ((IConnectionProvider)parent).getConnections();
		}
		if( parent instanceof IConnectionWrapper && ((IConnectionWrapper)parent).isConnected()) {
			return loadAndGetRootChildren(parent);
		}
        if (parent instanceof ObjectNameNode) {
            ObjectNameNode node = (ObjectNameNode) parent;
            if( node.isLoaded()) {
            	node.getMbeanInfoWrapper().getMBeanFeatureInfos();
            }
            return loadAndGetMbeanInfoWrapper(node);
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

		if( loading.containsKey(parent)) {
			return new Object[] { loading.get(parent)};
		}
		
		// Must load the model
		Job job = new Job(Messages.LoadingJMXNodes) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					w.loadRoot(monitor);
				} catch( CoreException ce ) {
					return ce.getStatus();
				} finally {
					loading.remove(w);
				}
				asyncRefresh(w,  parent);
				return Status.OK_STATUS;
			}

		};
		
		DelayProxy p = new DelayProxy(w);
		loading.put(w, p);
		job.schedule();
		return new Object[] { p };
    }
    
    private void asyncRefresh(final IConnectionWrapper w, final Object parent) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if( viewer instanceof StructuredViewer) 
					((StructuredViewer)viewer).refresh(parent);
				else
					viewer.refresh();
				
				TreePath[] treePaths = RefreshActionState.getDefault().getExpansion(w);
				ISelection sel = RefreshActionState.getDefault().getSelection(w);
				if( treePaths != null )
					((TreeViewer)viewer).setExpandedTreePaths(treePaths);
				if( sel != null ) 
					((TreeViewer)viewer).setSelection(sel);
				
			}
		});

	}

	protected synchronized Object[] loadAndGetMbeanInfoWrapper(final ObjectNameNode parent) {
		if( parent.isLoaded()) {
			return parent.getMbeanInfoWrapper().getMBeanFeatureInfos();
		}

		if( loadingObjectNameNode.containsKey(parent)) {
			return new Object[] { loadingObjectNameNode.get(parent)};
		}

		// Must load the model
		Job job = new Job(Messages.LoadingJMXNodes) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					parent.getMbeanInfoWrapper(monitor);
				} finally {
					loadingObjectNameNode.remove(parent);
				}
				asyncRefresh(parent.getRoot().getConnection(),  parent);
				return Status.OK_STATUS;
			}
		};
		
		DelayProxy p = new DelayProxy(parent);
		loadingObjectNameNode.put(parent, p);
		job.schedule();
		return new Object[] { p };
	}

    @Override
    public boolean hasChildren(Object parent) {
    	if( parent instanceof ProviderCategory || parent instanceof IConnectionProvider ) {
    		return getChildren(parent).length > 0;
    	}
    	
        if (parent instanceof ObjectNameNode) {
            ObjectNameNode node = (ObjectNameNode) parent;
            if( node.isLoaded() )
            	return node.getMbeanInfoWrapper().getMBeanFeatureInfos().length > 0;
            return true;
        }
        if (parent instanceof Node) {
            Node node = (Node) parent;
            return node.getChildren().length > 0;
        }
        if (parent instanceof MBeanFeatureInfoWrapper) {
            return false;
        }
        if( parent instanceof IConnectionWrapper ) {
        	return ((IConnectionWrapper)parent).isConnected();
        }
        return !(parent instanceof DelayProxy);
    }

    private synchronized ProviderCategory findCategory(String type) {
    	if( categories != null ) {
    		for( int i = 0; i < categories.length; i++ ) {
    			if( categories[i].getId().equals(type))
    				return categories[i];
    		}
    	}
    	return null;
    }
    
	@Override
	public void connectionAdded(IConnectionWrapper connection) {
		IConnectionProvider provider = connection.getProvider();
		if( provider instanceof IConnectionCategory ) {
			String type = ((IConnectionCategory)provider).getCategoryId();
			ProviderCategory parent = findCategory(type);
			addConnection(connection, parent == null ? viewer.getInput() : parent);
		}
	}

	@Override
	public void connectionChanged(IConnectionWrapper connection) {
		fireRefresh(connection, false);
	}

	@Override
	public void connectionRemoved(IConnectionWrapper connection) {
		removeConnection(connection);
	}

	private void fireRefresh(final IConnectionWrapper connection, final boolean full) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if( viewer != null && !viewer.getControl().isDisposed()) {
					if(full || !(viewer instanceof StructuredViewer))
						viewer.refresh();
					else
						((StructuredViewer)viewer).refresh(connection);
				}
			}
		});
	}
	private void addConnection(final IConnectionWrapper connection, final Object parent) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if( viewer != null && !viewer.getControl().isDisposed()) {
					if(isJMXView()) {
						if( parent instanceof ProviderCategory) {
							((TreeViewer)viewer).refresh(parent, true);
						} else {
							((TreeViewer)viewer).add(parent, connection);
						}
					} else
						viewer.refresh();
				}
			}
		});
	}

	private boolean isJMXView() {
		IWorkbenchPart nav = findView(JMXNavigator.VIEW_ID);
		if(nav instanceof JMXNavigator) {
			JMXNavigator jmxn = (JMXNavigator)nav;
			CommonViewer navigatorViewer = jmxn.getCommonViewer();
			if( this.viewer == navigatorViewer && navigatorViewer != null ) {
				return true;
			}
		}
		return false;
	}
	

	private static final IWorkbenchPart findView(String viewId) {
		IWorkbenchPart part = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow() ;
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				part = page.findView(viewId);
			}
		}
		return part;
	}
	
	private void removeConnection(final IConnectionWrapper connection) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if( viewer != null && !viewer.getControl().isDisposed()) {
					if(isJMXView())
						((TreeViewer)viewer).remove(connection);
					else
						viewer.refresh();
				}
			}
		});
	}
}