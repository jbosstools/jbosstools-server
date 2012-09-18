/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.ui.internal.view.servers.AbstractServerAction;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.internal.views.navigator.JMXNavigator;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerContentProvider;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerLabelProvider;

public class JMXProvider {

	public static class ActionProvider extends CommonActionProvider {
		private ICommonActionExtensionSite actionSite;
		private ShowInJMXViewAction showInJMXViewAction;
		public ActionProvider() {
			super();
		}
		
		public void init(ICommonActionExtensionSite aSite) {
			super.init(aSite);
			this.actionSite = aSite;
			createActions(aSite);
		}

		protected void createActions(ICommonActionExtensionSite aSite) {
			ICommonViewerSite site = aSite.getViewSite();
			if( site instanceof ICommonViewerWorkbenchSite ) {
				StructuredViewer v = aSite.getStructuredViewer();
				if( v instanceof CommonViewer ) {
					CommonViewer cv = (CommonViewer)v;
					ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
					showInJMXViewAction = new ShowInJMXViewAction(wsSite.getSelectionProvider());
				}
			}
		}

		public void fillContextMenu(IMenuManager menu) {
			ICommonViewerSite site = actionSite.getViewSite();
			IStructuredSelection selection = null;
			if (site instanceof ICommonViewerWorkbenchSite) {
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
				selection = (IStructuredSelection) wsSite.getSelectionProvider()
						.getSelection();
			}

			IContributionItem quick = menu.find("org.eclipse.ui.navigate.showInQuickMenu"); //$NON-NLS-1$
			if( quick != null && selection != null && selection.toArray().length == 1 ) {
				if( selection.getFirstElement() instanceof IServer ) {
					IServer server = (IServer)selection.getFirstElement();
						
					ServerExtendedProperties properties = (ServerExtendedProperties) server.loadAdapter(ServerExtendedProperties.class, null);
					if( properties != null ) {
						int i = properties.getJMXProviderType();
						if( i != ServerExtendedProperties.JMX_NULL_PROVIDER ) {
							if( menu instanceof MenuManager ) {
								((MenuManager)quick).add(showInJMXViewAction);
							}
						}
					}
				}
			}
		}
		
		public class ShowInJMXViewAction extends AbstractServerAction { 
			private IStructuredSelection previousSelection;
			private IServerListener serverListener;
			public ShowInJMXViewAction(ISelectionProvider sp) {
				super(sp, null);
				
				IViewRegistry reg = PlatformUI.getWorkbench().getViewRegistry();
				IViewDescriptor desc = reg.find(JMXNavigator.VIEW_ID);
				setText(desc.getLabel());
				setImageDescriptor(desc.getImageDescriptor());
				serverListener = new IServerListener() {
					public void serverChanged(final ServerEvent event) {
						// If this is the server that was / is selected
						if( previousSelection != null && previousSelection.size() > 0 
								&& previousSelection.getFirstElement().equals(event.getServer())) {
							// and it switches state, update enablement
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if( UnitedServerListener.serverSwitchesToState(event, IServer.STATE_STARTED)) {
										setEnabled(true);
									} else if( UnitedServerListener.serverSwitchesToState(event, IServer.STATE_STOPPED)) {
										setEnabled(false);
									}
								}
							});
						}
					}
				};
			}

			public boolean accept(IServer server) {
				boolean preconditions = (server.getServerType() != null && 
						server.loadAdapter(JBossServer.class, new NullProgressMonitor()) != null
						&& server.getServerState() == IServer.STATE_STARTED);
				IConnectionWrapper connection = JBossJMXConnectionProviderModel.getDefault().getConnection(server);
				return preconditions && connection.isConnected();
			}
			
			public synchronized void selectionChanged(IStructuredSelection sel) {
				if( sel.size() == 0 || sel.size() > 1 ) {
					setEnabled(false);
					return;
				}
				// size = 1
				switchListener(previousSelection, sel);
				previousSelection = sel;
				super.selectionChanged(sel);
			}

			private void switchListener(IStructuredSelection previousSelection, IStructuredSelection newSel) {
				if( previousSelection != null ) {
					Object o = previousSelection.getFirstElement();
					if( o instanceof IServer) {
						((IServer)o).removeServerListener(serverListener);
					}
				}
				Object newSel1 = newSel.getFirstElement();
				if( newSel1 instanceof IServer) {
					((IServer)newSel1).addServerListener(serverListener);
				}
			}
			
			public void perform(final IServer server) {
				IWorkbenchPart part = null;
				try {
					part = UIUtil.bringViewToFront(JMXNavigator.VIEW_ID);
				} catch(PartInitException pie) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error loading MBean Explorer", pie));
				}
				
				if( part != null ) {
					final JMXNavigator view = (JMXNavigator) part.getAdapter(JMXNavigator.class);
					if (view != null) {
						Display.getDefault().asyncExec(new Runnable() { 
							public void run() {
								IConnectionWrapper connection = JBossJMXConnectionProviderModel.getDefault().getConnection(server);
								if( connection != null ) {
									view.getCommonViewer().collapseAll();
									ISelection sel = new StructuredSelection(new Object[] { connection });
									view.getCommonViewer().setSelection(sel, true);
									view.getCommonViewer().expandToLevel(connection, 2);
								}
							}
						});
					}
				}
			}
		}
	}
	
	public static class ContentProvider implements ITreeContentProvider {
		private MBeanExplorerContentProvider delegate;
		public ContentProvider() {
			delegate = new MBeanExplorerContentProvider();
		}
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof IServer ) {
				Object sel = JBossJMXConnectionProviderModel.getDefault().getConnection((IServer)parentElement);
				if( sel != null )
					return new Object[] { sel };
			}
			return delegate.getChildren(parentElement);
		}
		public Object getParent(Object element) {
			return delegate.getParent(element);
		}
		public boolean hasChildren(Object element) {
			return delegate.hasChildren(element);
		}
		public Object[] getElements(Object inputElement) {
			return delegate.getElements(inputElement);
		}
		public void dispose() {
			delegate.dispose();
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			delegate.inputChanged(viewer, oldInput, newInput);
		}
	}
	
	public static  class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		private MBeanExplorerLabelProvider delegate;
		public LabelProvider() {
			delegate = new MBeanExplorerLabelProvider();
		}
		
		public void dispose() {
			delegate.dispose();
		}
		
		public String getText(Object element) {
			if( element instanceof IConnectionWrapper ) {
				return "JMX";  //$NON-NLS-1$
			}
			return delegate.getText(element);
		}
		
		public Image getImage(Object element) {
			if( element instanceof IConnectionWrapper ) {
				return JBossServerUISharedImages.getImage(JBossServerUISharedImages.JMX_IMAGE); 
			}
			return delegate.getImage(element);
		}
	}
	
}
