/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Base Code
 *     Red Hat - Refactor for CNF
 *******************************************************************************/
package org.jboss.tools.as.wst.server.ui.xpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.UpdateServerJob;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.internal.Trace;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.eclipse.wst.server.ui.internal.viewers.BaseContentProvider;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * @deprecated
 */
public class ServerContentProvider extends BaseContentProvider implements ITreeContentProvider {
	public static Object INITIALIZING = new Object();
	protected IServerLifecycleListener serverResourceListener;
	protected IPublishListener publishListener;
	protected IServerListener serverListener;

	// servers that are currently publishing and starting
	protected static Set<String> publishing = new HashSet<String>(4);
	protected static Set<String> starting = new HashSet<String>(4);
	protected boolean animationActive = false;
	protected boolean stopAnimation = false;
	protected boolean initialized = false;
	
	
	private StructuredViewer viewer;
	
	public ServerContentProvider() {
		addListeners();
	}
	
	public Object[] getElements(Object element) {
		if( !initialized ) {
			deferInitialization();
			return new Object[] {INITIALIZING};
		}
		
		List<IServer> list = new ArrayList<IServer>();
		IServer[] servers = ServerCore.getServers();
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size; i++) {
				if (!((Server)servers[i]).isPrivate())
					list.add(servers[i]);
			}
		}
		return list.toArray();
	}

	public Object[] getChildren(Object element) {
		if (element instanceof ModuleServer) {
			ModuleServer ms = (ModuleServer) element;
			try {
				IModule[] children = ms.server.getChildModules(ms.module, null);
				int size = children.length;
				ModuleServer[] ms2 = new ModuleServer[size];
				for (int i = 0; i < size; i++) {
					int size2 = ms.module.length;
					IModule[] module = new IModule[size2 + 1];
					System.arraycopy(ms.module, 0, module, 0, size2);
					module[size2] = children[i];
					ms2[i] = new ModuleServer(ms.server, module);
				}
				return ms2;
			} catch (Exception e) {
				return new Object[]{};
			}
		}
		
		// TODO TELL ANGEL ABOUT THIS IF STATEMENT
		if( element instanceof IServer) {
			IServer server = (IServer) element;
			IModule[] modules = server.getModules(); 
			int size = modules.length;
			ModuleServer[] ms = new ModuleServer[size];
			for (int i = 0; i < size; i++) {
				ms[i] = new ModuleServer(server, new IModule[] { modules[i] });
			}
			return ms;
		}
		return new Object[]{};
	}

	public Object getParent(Object element) {
		if (element instanceof ModuleServer) {
			ModuleServer ms = (ModuleServer) element;
			return ms.server;
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ModuleServer) {
			// Check if the module server has child modules.
			ModuleServer curModuleServer = (ModuleServer)element;
			IServer curServer = curModuleServer.server;
			IModule[] curModule = curModuleServer.module;
			if (curServer != null &&  curModule != null) {
				IModule[] curChildModule = curServer.getChildModules(curModule, null);
				if (curChildModule != null && curChildModule.length > 0)
					return true;
				
				return false;
			}
			
			return false;
		}
		if( element instanceof IServer ) {
			return ((IServer) element).getModules().length > 0;
		}
		return false;
	}
	
	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		viewer = (StructuredViewer) aViewer;
	}

	public void dispose() {
		// remove all listeners!
		ServerCore.removeServerLifecycleListener(serverResourceListener);
		IServer[] servers = ServerCore.getServers();
		for( int i = 0;i < servers.length; i++ ) {
			servers[i].removePublishListener(publishListener);
			servers[i].removeServerListener(serverListener);
		}
	}

	
	
	// Listeners and refreshing the viewer
	protected void addListeners() {
		serverResourceListener = new IServerLifecycleListener() {
			public void serverAdded(IServer server) {
				refreshServer(null);
				server.addServerListener(serverListener);
				((Server) server).addPublishListener(publishListener);
			}
			public void serverChanged(IServer server) {
				refreshServer(server);
			}
			public void serverRemoved(IServer server) {
				refreshServer(null);
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
				if (event == null)
					return;
				
				int eventKind = event.getKind();
				IServer server = event.getServer();
				if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
					// server change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
						refreshServer(server, true);
						int state = event.getState();
						String id = server.getId();
						if (state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING) {
							boolean startThread = false;
							synchronized (starting) {
								if (!starting.contains(id)) {
									if (starting.isEmpty())
										startThread = true;
									starting.add(id);
								}
							}
							if (startThread)
								startThread();
						} else {
							boolean stopThread = false;
							synchronized (starting) {
								if (starting.contains(id)) {
									starting.remove(id);
									if (starting.isEmpty())
										stopThread = true;
								}
							}
							if (stopThread)
								stopThread();
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

	protected void deferInitialization() {
		Job job = new Job(org.eclipse.wst.server.ui.internal.Messages.jobInitializingServersView) {
			public IStatus run(IProgressMonitor monitor) {
				IServer[] servers = ServerCore.getServers();
				int size = servers.length;
				for (int i = 0; i < size; i++) {
					((Server)servers[i]).getAllModules().iterator();
				}
				
				for (int i = 0; i < size; i++) {
					IServer server = servers[i];
					if (server.getServerType() != null && server.getServerState() == IServer.STATE_UNKNOWN) {
						UpdateServerJob job2 = new UpdateServerJob(server);
						job2.schedule();
					}
				}
				initialized = true;
				refreshServer(null);
				return Status.OK_STATUS;
			}
		};
		
		job.setSystem(true);
		job.setPriority(Job.SHORT);
		job.schedule();
	}
	
	protected void updateServerLabel(final IServer server) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					if( viewer != null && !viewer.getControl().isDisposed()) {
						viewer.update(server, null);
					}
				} catch (Exception e) {
					// ignore
				}
			}
		});
	}
	
	protected void refreshServer(final IServer server) {
		refreshServer(server, false);
	}
	protected void refreshServer(final IServer server, final boolean resetSelection) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					if( viewer != null && !viewer.getControl().isDisposed()) {
						viewer.refresh(server);
						if( resetSelection ) {
							ISelection sel = viewer.getSelection();
							viewer.setSelection(sel);
						}
					}
				} catch (Exception e) {
					// ignore
				}
			}
		});
	}
	
	protected void handlePublishChange(IServer server, boolean isPublishing) {
		String serverId = server.getId();
		if (isPublishing)
			publishing.add(serverId);
		else
			publishing.remove(serverId);
	
		refreshServer(server);
	}

	
	protected void startThread() {
		if (animationActive)
			return;
		
		stopAnimation = false;
		
		final Display display = viewer == null ? Display.getDefault() : viewer.getControl().getDisplay();
		final int SLEEP = 200;
		final Runnable[] animator = new Runnable[1];
		animator[0] = new Runnable() {
			public void run() {
				if (!stopAnimation) {
					try {
						int size = 0;
						String[] servers;
						synchronized (starting) {
							size = starting.size();
							servers = new String[size];
							starting.toArray(servers);
						}
						
						for (int i = 0; i < size; i++) {
							IServer server = ServerCore.findServer(servers[i]);
							if (server != null ) {
								updateServerLabel(server);
							}
						}
					} catch (Exception e) {
						Trace.trace(Trace.FINEST, Messages.ServerContentProvider_ErrorInServersViewAnimation, e);
					}
					display.timerExec(SLEEP, animator[0]);
				}
			}
		};
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				display.timerExec(SLEEP, animator[0]);
			}
		});
	}

	protected void stopThread() {
		stopAnimation = true;
	}}
