/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.ui.internal.cnf.ServerActionProvider;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

public class ModuleActionProvider extends CommonActionProvider {
	private Action deleteModuleAction, fullPublishModuleAction, incrementalPublishModuleAction;
	private ModuleServer[] selection;

	private ICommonActionExtensionSite actionSite;
	public ModuleActionProvider() {
		super();
	}
	
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		createActions();
	}
	public void fillContextMenu(IMenuManager menu) {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider()
					.getSelection();
		}

		if( allAre(selection, ModuleServer.class)) {
			Object[] arr = selection.toArray();
			ModuleServer[] ms = new ModuleServer[arr.length];
			for( int i = 0; i < arr.length; i++ ) 
				ms[i] = (ModuleServer)arr[i];
			this.selection = ms;
			
			IContributionItem sep = menu.find(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR);
			if( sep == null ) {
				sep = new Separator(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR);
				sep.setVisible(false);
				menu.add(sep);
			}
			menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, incrementalPublishModuleAction);
			menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, fullPublishModuleAction);
			if( selection.size() > 1 ) {
				deleteModuleAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
				menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, deleteModuleAction);
			}
		}
	}
	
	protected boolean allAre(IStructuredSelection sel, Class c) {
		if( sel == null || sel.isEmpty())
			return false;
		
		Iterator i = sel.iterator();
		while(i.hasNext()) 
			if(!i.next().getClass().equals(c))
				return false;
		return true;
	}
	
	
	protected void createActions() {
		deleteModuleAction = new Action() {
			public void run() {
				deleteModule();
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
			IServer tmp = ServerCore.findServer(s.getId());
			for( int i = 0; i < selection.length; i++ ) {
				IModule[] mod = selection[i].module;
				s.setModulePublishState(mod, type);
				ArrayList<IModule[]> allChildren = ServerModelUtilities.getDeepChildren(s, mod);
				for( int j = 0; j < allChildren.size(); j++ ) {
					s.setModulePublishState((IModule[])allChildren.get(j), type);
				}
			}
			final IServer s2 = s;
			new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch(InterruptedException ie) {
						// Ignore
					}
					s2.publish(IServer.PUBLISH_INCREMENTAL, null, getUserInitiatedAdaptable(), null);
				}
			}.start();
		}
	}

	protected void deleteModule() {
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
						server2.publish(IServer.PUBLISH_INCREMENTAL, null, getUserInitiatedAdaptable(), null);
					}
				} catch (CoreException e) {
					// ignore
				}
			}};
			t.start();
		}
	}
	
	public IAdaptable getUserInitiatedAdaptable() {
		IAdaptable info = new IAdaptable() {
			public Object getAdapter(Class adapter) {
				if (String.class.equals(adapter))
					return "user";
				return null;
			}
		};
		return info;
	}
}
