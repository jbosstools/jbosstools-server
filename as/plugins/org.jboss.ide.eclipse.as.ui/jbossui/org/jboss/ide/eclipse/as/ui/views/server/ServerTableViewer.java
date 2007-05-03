package org.jboss.ide.eclipse.as.ui.views.server;

/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.internal.Trace;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.eclipse.wst.server.ui.internal.view.servers.ServerAction;
import org.eclipse.wst.server.ui.internal.view.servers.ServerActionHelper;
import org.eclipse.wst.server.ui.internal.view.servers.ServerTableLabelProvider;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
/**
 * Tree view showing servers and their associations.
 * This is for the TOP window
 */
public class ServerTableViewer extends TreeViewer {
	protected static final String ROOT = "root";

	protected IServerLifecycleListener serverResourceListener;
	protected IPublishListener publishListener;
	protected IServerListener serverListener;

	protected static Object deletedElement = null;

	// servers that are currently publishing and starting
	protected static List publishing = new ArrayList();
	protected static List starting = new ArrayList();
	
	protected ServerTableLabelProvider2 labelProvider;
	//protected ISelectionListener dsListener;

	protected StrippedServerView view;
	
	protected class ServerTableLabelProvider2 extends ServerTableLabelProvider {
		private int myCount = 0;
		protected Image getStateImage(IServerType serverType, int state, String mode) {
			if( serverType.getId().equals("org.jboss.ide.eclipse.as.systemCopyServer")) return null;
			return UIDecoratorManager.getUIDecorator(serverType).getStateImage(state, mode, myCount);
		}
		protected String getStateLabel(IServerType serverType, int state, String mode) {
			if( serverType.getId().equals("org.jboss.ide.eclipse.as.systemCopyServer")) return "N/A";
			return UIDecoratorManager.getUIDecorator(serverType).getStateLabel(state, mode, myCount);
		}
		protected void animate() {
			myCount ++;
			if (myCount > 2)
				myCount = 0;
		}

	}
	
	public class TrimmedServerContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public Object[] getElements(Object element) {
			return ServerCore.getServers();
		}

		public void inputChanged(Viewer theViewer, Object oldInput, Object newInput) {
			// do nothing
		}
		
		public void dispose() {
			// do nothing
		}

		public Object[] getChildren(Object element) {
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
				return false;
		}
	}

	protected Thread thread = null;
	protected boolean stopThread = false;
	
	protected void startThread() {
		stopThread = false;
		if (thread != null)
			return;
		
		thread = new Thread("Servers View Animator") {
			public void run() {
				while (!stopThread) {
					try {
						labelProvider.animate();
						final Object[] rootElements = ((ITreeContentProvider)getContentProvider()).getElements(null); 
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (getTree() != null && !getTree().isDisposed())
									update(rootElements, null);
							}
						});
						Thread.sleep(250);
					} catch (Exception e) {
						Trace.trace(Trace.FINEST, "Error in animated server view", e);
					}
					thread = null;
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	protected void stopThread() {
		stopThread = true;
	}

	/**
	 * ServerTableViewer constructor comment.
	 * 
	 * @param view the view 
	 * @param tree the tree
	 */
	public ServerTableViewer(final StrippedServerView view, final Tree tree) {
		super(tree);
		this.view = view;
		
		setContentProvider(new TrimmedServerContentProvider());
		labelProvider = new ServerTableLabelProvider2();
		labelProvider.addListener(new ILabelProviderListener() {
			public void labelProviderChanged(LabelProviderChangedEvent event) {
				Object[] obj = event.getElements();
				if (obj == null)
					refresh(true);
				else {
					int size = obj.length;
					for (int i = 0; i < size; i++)
						refresh(obj[i], true);
				}
			}
		});
		setLabelProvider(labelProvider);
		setSorter(new ViewerSorter() {
			// empty
		});
		
		setInput(ROOT);
		
		addListeners();
		
		IActionBars actionBars = view.getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), new ServerAction(getControl().getShell(), this, "Delete it!", ServerActionHelper.ACTION_DELETE));
	}

	protected void addListeners() {
		serverResourceListener = new IServerLifecycleListener() {
			public void serverAdded(IServer server) {
				addServer(server);
				server.addServerListener(serverListener);
				((Server) server).addPublishListener(publishListener);
			}
			public void serverChanged(IServer server) {
				refreshServer(server);
			}
			public void serverRemoved(IServer server) {
				removeServer(server);
				server.removeServerListener(serverListener);
				((Server) server).removePublishListener(publishListener);
			}
		};
		ServerCore.addServerLifecycleListener(serverResourceListener);
		
		publishListener = new PublishAdapter() {
			public void publishStarted(IServer server) {
				handlePublishChange(server, true);
			}
			
			public void publishFinished(IServer server, IStatus status) {
				handlePublishChange(server, false);
			}
		};
		
		serverListener = new IServerListener() {
			public void serverChanged(ServerEvent event) {
				if (event == null) {
					return;
				}
				int eventKind = event.getKind();
				IServer server = event.getServer();
				if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
					// server change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
						refreshServer(server);
						int state = event.getState();
						String id = server.getId();
						if (state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING) {
							if (!starting.contains(id)) {
								if (starting.isEmpty())
									startThread();
								starting.add(id);
							}
						} else {
							if (starting.contains(id)) {
								starting.remove(id);
								if (starting.isEmpty())
									stopThread();
							}
						}
					} else
						refreshServer(server);
				} else if ((eventKind & ServerEvent.MODULE_CHANGE) != 0) {
					// module change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0 || (eventKind & ServerEvent.PUBLISH_STATE_CHANGE) != 0) {
						refreshServer(server);
					}
				}
			}
		};
		
		// add listeners to servers
		IServer[] servers = ServerCore.getServers();
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size; i++) {
				servers[i].addServerListener(serverListener);
				((Server) servers[i]).addPublishListener(publishListener);
			}
		}
	}
	
	protected void refreshServer(final IServer server) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					refresh(server);
					
					// This ensures the icons are updated in case of a publish or state change
					ISelection sel = ServerTableViewer.this.getSelection();
					ServerTableViewer.this.setSelection(sel);
				} catch (Exception e) {
					// ignore
				}
			}
		});
	}

	protected void handleDispose(DisposeEvent event) {
		stopThread();
		//if (dsListener != null)
		//	view.getViewSite().getPage().removeSelectionListener(dsListener);
		
		ServerCore.removeServerLifecycleListener(serverResourceListener);
		
		// remove listeners from server
		IServer[] servers = ServerCore.getServers();
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size; i++) {
				servers[i].removeServerListener(serverListener);
				((Server) servers[i]).removePublishListener(publishListener);
			}
		}
	
		super.handleDispose(event);
	}

	/**
	 * Called when the publish state changes.
	 * @param server org.eclipse.wst.server.core.IServer
	 */
	protected void handlePublishChange(IServer server, boolean isPublishing) {
		String serverId = server.getId();
		if (isPublishing)
			publishing.add(serverId);
		else
			publishing.remove(serverId);
	
		refreshServer(server);
	}
	
	/**
	 * 
	 */
	protected void handleServerModulesChanged(IServer server2) {
		if (server2 == null)
			return;

		IServer[] servers = ServerCore.getServers();
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size; i++) {
				if (server2.equals(servers[i]))
					refresh(servers[i]);
			}
		}
	}
	
	/**
	 * Called when an element is added.
	 * @param server org.eclipse.wst.server.core.IServer
	 */
	protected void handleServerResourceAdded(IServer server) {
		add(null, server);
	}
	
	/**
	 * Called when an element is changed.
	 * @param server org.eclipse.wst.server.core.IServer
	 */
	protected void handleServerResourceChanged(IServer server) {
		refresh(server);
	}
		
	/**
	 * Called when an element is removed.
	 * @param server org.eclipse.wst.server.core.IServer
	 */
	protected void handleServerResourceRemoved(IServer server) {
		remove(server);

		String serverId = server.getId();
		publishing.remove(serverId);

		view.getViewSite().getActionBars().getStatusLineManager().setMessage(null, null);
	}
	
	protected void addServer(final IServer server) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				add(ROOT, server);
			}
		});
	}
	
	protected void removeServer(final IServer server) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				remove(server);
			}
		});
	}	
}