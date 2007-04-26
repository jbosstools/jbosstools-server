/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.ide.eclipse.as.ui.views.server.providers;

import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.SimplePropertiesViewExtension;

public class ModuleViewProvider extends SimplePropertiesViewExtension {

	private ModuleContentProvider contentProvider;
	private ModuleLabelProvider labelProvider;
	
	private Action deleteModuleAction, publishModuleAction;
	
	private ModuleServer selection;
	
	private IServerLifecycleListener serverResourceListener;
	private IServerListener serverListener;
	
	
	public ModuleViewProvider() {
		contentProvider = new ModuleContentProvider();
		labelProvider = new ModuleLabelProvider();
		createActions();
		addListeners();
	}

	private void createActions() {
		deleteModuleAction = new Action() {
			public void run() {
				if (MessageDialog.openConfirm(new Shell(), Messages.ServerDialogHeading, Messages.DeleteModuleConfirm)) {
					try {
						IServerWorkingCopy wc = selection.server.createWorkingCopy();
						wc.modifyModules(null, selection.module , null);
						wc.save(true, null);
						// Re-publish in case the configuration change has not been published yet.
						DeployableServerBehavior behavior = (DeployableServerBehavior)
							contentProvider.getServer().loadAdapter(DeployableServerBehavior.class, new NullProgressMonitor());
						behavior.publishOneModule(IServer.PUBLISH_FULL, selection.module, ServerBehaviourDelegate.REMOVED, new NullProgressMonitor());
					} catch (Exception e) {
						// ignore
					}
				}
			}
		};
		deleteModuleAction.setText(Messages.DeleteModuleText);
		deleteModuleAction.setDescription(Messages.DeleteModuleDescription);
		
		publishModuleAction = new Action() {
			public void run() {
				DeployableServerBehavior behavior = (DeployableServerBehavior)
					contentProvider.getServer().loadAdapter(DeployableServerBehavior.class, new NullProgressMonitor());
				behavior.publishOneModule(IServer.PUBLISH_FULL, selection.module, ServerBehaviourDelegate.CHANGED, new NullProgressMonitor());
			}
		};
		publishModuleAction.setText(Messages.PublishModuleText);
		publishModuleAction.setDescription(Messages.PublishModuleDescription);
	}
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
		if( selection instanceof ModuleServer) {
			this.selection = (ModuleServer)selection;
			menu.add(deleteModuleAction);
			menu.add(publishModuleAction);
		}
	}

	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	
	class ModuleContentProvider implements ITreeContentProvider {

		private IServer input;
		
		public Object[] getChildren(Object parentElement) {
			
			if (parentElement instanceof ModuleServer) {
				ModuleServer ms = (ModuleServer) parentElement;
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
					return null;
				}
			}

			
			
			if( parentElement instanceof ServerViewProvider && input != null ) {
				IModule[] modules = input.getModules(); 
				int size = modules.length;
				ModuleServer[] ms = new ModuleServer[size];
				for (int i = 0; i < size; i++) {
					ms[i] = new ModuleServer(input, new IModule[] { modules[i] });
				}
				return ms;
			}
			return null;
		}

		public Object getParent(Object element) {
			if( element instanceof ModuleServer ) {
				return provider;
			}
			
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false; 
		}

		// unused
		public Object[] getElements(Object inputElement) {
			return null;
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			input = (IServer)newInput;
		}
		
		public IServer getServer() {
			return input;
		}
	}
	
	class ModuleLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if( obj instanceof ModuleServer ) {
				ModuleServer ms = (ModuleServer)obj;
				int size = ms.module.length;
				return ms.module[size - 1].getName();
			}

			return "garbage";
		}
		public Image getImage(Object obj) {
			if( obj instanceof ModuleServer ) {
				ModuleServer ms = (ModuleServer)obj;
				int size = ms.module.length;
				return ServerUICore.getLabelProvider().getImage(ms.module[ms.module.length - 1]);
			}
			return null;
		}

	}


	public String[] getPropertyKeys(Object selected) {
		return new String[] { Messages.ModulePropertyType, Messages.ModulePropertyProject };
	}
	
	public Properties getProperties(Object selected) {
		Properties props = new Properties();
		if( selected != null && selected instanceof ModuleServer) {
			IModule mod = ((ModuleServer)selected).module[0];
			props.setProperty(Messages.ModulePropertyType, mod.getModuleType().getId());
			props.setProperty(Messages.ModulePropertyProject, mod.getProject().getName());
		}
		return props;
	}

	private void addListeners() {
		serverResourceListener = new IServerLifecycleListener() {
			public void serverAdded(IServer server) {
				if( ServerConverter.getJBossServer(server) != null ) 
					server.addServerListener(serverListener);
			}
			public void serverChanged(IServer server) {
			}
			public void serverRemoved(IServer server) {
				if( ServerConverter.getJBossServer(server) != null ) 
					server.removeServerListener(serverListener);
			}
		};
		ServerCore.addServerLifecycleListener(serverResourceListener);
		
		serverListener = new IServerListener() { 
			public void serverChanged(ServerEvent event) {
				int eventKind = event.getKind();
				if ((eventKind & ServerEvent.MODULE_CHANGE) != 0) {
					// module change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0 || (eventKind & ServerEvent.PUBLISH_STATE_CHANGE) != 0) {
						refreshViewer();
					} 
				}
			}
		};
		// add listeners to servers
		JBossServer[] servers = ServerConverter.getAllJBossServers();
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size; i++) {
				servers[i].getServer().addServerListener(serverListener);
			}
		}

		
	}
}
