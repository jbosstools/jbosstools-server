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

import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.core.util.ModuleUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.SimplePropertiesViewExtension;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ModuleViewProvider extends SimplePropertiesViewExtension {

	private ModuleContentProvider contentProvider;
	private ModuleLabelProvider labelProvider;
	private Action deleteModuleAction, fullPublishModuleAction, incrementalPublishModuleAction;
	private ModuleServer[] selection;
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
					Thread t = new Thread() { public void run() { 
						try {
							if( selection.length > 0 && selection[0].server != null ) {
								IServer server = selection[0].server;
								ArrayList topModsToRemove = new ArrayList();
								IModule topModTmp;
								for( int i = 0; i < selection.length; i++ ) {
									if( !topModsToRemove.contains(selection[i].module[0]))
										topModsToRemove.add(selection[i].module[0]);
								}
								IServerWorkingCopy serverWC = server.createWorkingCopy();
								IModule[] modsToRemove = 
									(IModule[]) topModsToRemove.toArray(new IModule[topModsToRemove.size()]);
								ServerUtil.modifyModules(serverWC, new IModule[0], modsToRemove, new NullProgressMonitor());
								IServer server2 = serverWC.save(true, null);
								new PublishServerJob(server2, IServer.PUBLISH_INCREMENTAL, true).schedule();
							}
						} catch (CoreException e) {
							// ignore
						}
					}};
					t.start();
				}
			}
		};
		deleteModuleAction.setText(Messages.DeleteModuleText);
		deleteModuleAction.setDescription(Messages.DeleteModuleDescription);
		deleteModuleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.UNPUBLISH_IMAGE));
		
		fullPublishModuleAction = new Action() {
			public void run() {
				actionPublish(IServer.PUBLISH_STATE_FULL);
			}
		};
		fullPublishModuleAction.setText(Messages.FullPublishModuleText);
		fullPublishModuleAction.setDescription(Messages.PublishModuleDescription);
		fullPublishModuleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.PUBLISH_IMAGE));

	
		incrementalPublishModuleAction = new Action() {
			public void run() {
				actionPublish(IServer.PUBLISH_STATE_INCREMENTAL);
			}
		};
		incrementalPublishModuleAction.setText(Messages.IncrementalPublishModuleText);
		incrementalPublishModuleAction.setDescription(Messages.PublishModuleDescription);
		incrementalPublishModuleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.PUBLISH_IMAGE));
}
	
	protected void actionPublish(int type) {
		// Assumption: Anything selected is already on the server, or it wouldnt be in the view.
		if( selection != null && selection.length > 0 ) {
			Server s = ((Server)selection[0].server);
			for( int i = 0; i < selection.length; i++ ) {
				IModule[] mod = selection[i].module;
				s.setModulePublishState(mod, type);
				ArrayList<IModule[]> allChildren = ModuleUtil.getDeepChildren(s, mod);
				for( int j = 0; j < allChildren.size(); j++ ) {
					s.setModulePublishState((IModule[])allChildren.get(j), type);
				}
			}
			new PublishServerJob(s, IServer.PUBLISH_INCREMENTAL, true).schedule();
		}
	}

	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object[] selection) {
		if( allAre(selection, ModuleServer.class)) {
			ModuleServer[] ms = new ModuleServer[selection.length];
			for( int i = 0; i < selection.length; i++ ) 
				ms[i] = (ModuleServer)selection[i];
			this.selection = ms;
			menu.add(deleteModuleAction);
			menu.add(fullPublishModuleAction);
			menu.add(incrementalPublishModuleAction);
		}
	}

	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	public boolean supports(IServer server) {
		return true;
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
					return new Object[]{};
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
			return new Object[] {};
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

			return "unknown";
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
		return new String[] { Messages.ModulePropertyType, Messages.ModulePropertyProject, Messages.ModulePropertyName };
	}
	
	public Properties getProperties(Object selected) {
		Properties props = new Properties();
		if( selected != null && selected instanceof ModuleServer) {
			ModuleServer moduleServer = ((ModuleServer)selected);
			IModule mod = moduleServer.module[moduleServer.module.length-1];
			if( mod != null && mod.getProject() != null ) {
				props.setProperty(Messages.ModulePropertyType, mod.getModuleType().getId());
				props.setProperty(Messages.ModulePropertyProject, mod.getProject().getName());
				props.setProperty(Messages.ModulePropertyProject, mod.getName());
			}
		}
		return props;
	}

	private void addListeners() {
		UnitedServerListenerManager.getDefault().addListener(new UnitedServerListener() {
			public void serverChanged(ServerEvent event) {
				int eventKind = event.getKind();
				if ((eventKind & ServerEvent.MODULE_CHANGE) != 0) {
					// module change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0 || (eventKind & ServerEvent.PUBLISH_STATE_CHANGE) != 0) {
						refreshViewer();
					} 
				}
			}			
		});
	}
}
