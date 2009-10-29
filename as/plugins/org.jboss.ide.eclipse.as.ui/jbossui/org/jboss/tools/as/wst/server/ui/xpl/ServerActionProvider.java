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

import java.util.Iterator;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.Trace;
import org.eclipse.wst.server.ui.internal.actions.NewServerWizardAction;
import org.eclipse.wst.server.ui.internal.view.servers.CopyAction;
import org.eclipse.wst.server.ui.internal.view.servers.DeleteAction;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleSloshAction;
import org.eclipse.wst.server.ui.internal.view.servers.MonitorServerPortAction;
import org.eclipse.wst.server.ui.internal.view.servers.OpenAction;
import org.eclipse.wst.server.ui.internal.view.servers.PasteAction;
import org.eclipse.wst.server.ui.internal.view.servers.PropertiesAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishCleanAction;
import org.eclipse.wst.server.ui.internal.view.servers.RemoveModuleAction;
import org.eclipse.wst.server.ui.internal.view.servers.RenameAction;
import org.eclipse.wst.server.ui.internal.view.servers.RestartModuleAction;
import org.eclipse.wst.server.ui.internal.view.servers.ShowInConsoleAction;
import org.eclipse.wst.server.ui.internal.view.servers.ShowInDebugAction;
import org.eclipse.wst.server.ui.internal.view.servers.StartAction;
import org.eclipse.wst.server.ui.internal.view.servers.StartModuleAction;
import org.eclipse.wst.server.ui.internal.view.servers.StopAction;
import org.eclipse.wst.server.ui.internal.view.servers.StopModuleAction;
import org.jboss.ide.eclipse.as.ui.actions.ExploreAction;
/**
 * @deprecated
 */
public class ServerActionProvider extends CommonActionProvider {
	public static final String NEW_MENU_ID = "org.jboss.tools.as.wst.server.ui.internal.cnf.newMenuId"; //$NON-NLS-1$
	public static final String SHOW_IN_MENU_ID = "org.jboss.tools.as.wst.server.ui.internal.cnf.showInMenuId"; //$NON-NLS-1$
	public static final String TOP_SECTION_START_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.topSectionStart"; //$NON-NLS-1$
	public static final String TOP_SECTION_END_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.topSectionEnd"; //$NON-NLS-1$
	public static final String EDIT_SECTION_START_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnfeditSectionStart"; //$NON-NLS-1$
	public static final String EDIT_SECTION_END_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.editSectionEnd"; //$NON-NLS-1$
	public static final String CONTROL_SERVER_SECTION_START_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.controlServerSectionStart"; //$NON-NLS-1$
	public static final String CONTROL_SERVER_SECTION_END_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.controlServerSectionEnd"; //$NON-NLS-1$
	public static final String SERVER_ERATA_SECTION_START_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.serverErataSectionStart"; //$NON-NLS-1$
	public static final String SERVER_ERATA_SECTION_END_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.serverErataSectionEnd"; //$NON-NLS-1$
	public static final String CONTROL_MODULE_SECTION_START_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.controlModuleSectionStart"; //$NON-NLS-1$
	public static final String CONTROL_MODULE_SECTION_END_SEPARATOR = "org.jboss.tools.as.wst.server.ui.internal.cnf.controlModuleSectionEnd"; //$NON-NLS-1$
	
	private ICommonActionExtensionSite actionSite;
	private Clipboard clipboard;
	public ServerActionProvider() {
		super();
	}
	
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		ICommonViewerSite site = aSite.getViewSite();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = aSite.getStructuredViewer();
			if( v instanceof CommonViewer ) {
				CommonViewer cv = (CommonViewer)v;
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
				addListeners(cv);
				makeServerActions(cv, wsSite.getSelectionProvider());
			}
		}
	}


	// actions on a server
	protected Action[] actions;
	protected Action actionModifyModules;
	protected Action openAction, showInConsoleAction, showInDebugAction, propertiesAction, monitorPropertiesAction;
	protected Action copyAction, pasteAction, deleteAction, renameAction;
	protected Action noneAction = new Action(Messages.dialogMonitorNone) {
		// dummy action
	};

	private void addListeners(CommonViewer tableViewer) {
		tableViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				try {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					Object data = sel.getFirstElement();
					if (!(data instanceof IServer))
						return;
					IServer server = (IServer) data;
					ServerUIPlugin.editServer(server);
				} catch (Exception e) {
					Trace.trace(Trace.SEVERE, org.jboss.ide.eclipse.as.ui.Messages.ServerActionProvider_CouldNotOpenServerError, e);
				}
			}
		});
	}
	
	private void makeServerActions(CommonViewer tableViewer, ISelectionProvider provider) {
		clipboard = new Clipboard(tableViewer.getTree().getDisplay());
		Shell shell = tableViewer.getTree().getShell();
		
		actions = new Action[6];
		// create the start actions
		actions[0] = new StartAction(shell, provider, ILaunchManager.DEBUG_MODE);
		actions[1] = new StartAction(shell, provider, ILaunchManager.RUN_MODE);
		actions[2] = new StartAction(shell, provider, ILaunchManager.PROFILE_MODE);
		
		// create the stop action
		actions[3] = new StopAction(shell, provider);
		
		// create the publish actions
		actions[4] = new PublishAction(shell, provider);
		actions[5] = new ExploreAction(shell,provider);

		// create the open action
		openAction = new OpenAction(provider);

//		// create copy, paste, and delete actions
		pasteAction = new PasteAction(shell, provider, clipboard);
		copyAction = new CopyAction(provider, clipboard, pasteAction);
		deleteAction = new DeleteAction(shell, provider);
		renameAction = new RenameAction(shell, tableViewer, provider);
		
		// create the other actions
		actionModifyModules = new ModuleSloshAction(shell, provider);
		showInConsoleAction = new ShowInConsoleAction(provider);
		showInDebugAction = new ShowInDebugAction(provider);
		
		// create the properties action
		propertiesAction = new PropertiesAction(shell, provider);
		monitorPropertiesAction = new PropertiesAction(shell, "org.eclipse.wst.server.ui.properties.monitor", provider); //$NON-NLS-1$
	}

	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.debug", actions[0]); //$NON-NLS-1$
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.run", actions[1]); //$NON-NLS-1$
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.stop", actions[3]); //$NON-NLS-1$
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.publish", actions[4]); //$NON-NLS-1$
		actionBars.setGlobalActionHandler("org.eclipse.ui.navigator.Open", openAction); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
		actionBars.updateActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameAction);
		
		IContributionManager cm = actionBars.getToolBarManager();
		cm.removeAll();

		for (int i = 0; i < actions.length - 1; i++)
			cm.add(actions[i]);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		Shell shell = actionSite.getViewSite().getShell();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider().getSelection();
		}

		IServer server = null;
		IModule[] module = null;
		if (selection != null && !selection.isEmpty()) {
			Iterator iterator = selection.iterator();
			Object obj = iterator.next();
			if (obj instanceof IServer)
				server = (IServer) obj;
			if (obj instanceof ModuleServer) {
				ModuleServer ms = (ModuleServer) obj;
				server = ms.server;
				module = ms.module;
			}
			if (iterator.hasNext()) {
				server = null;
				module = null;
			}
		}
		
		menu.add(invisibleSeparator(TOP_SECTION_START_SEPARATOR));
		addTopSection(menu, server, module);
		menu.add(invisibleSeparator(TOP_SECTION_END_SEPARATOR));
		menu.add(new Separator());

		if (server != null && module == null) {
			menu.add(invisibleSeparator(EDIT_SECTION_START_SEPARATOR));
			menu.add(copyAction);
			menu.add(pasteAction);
			menu.add(deleteAction);
			menu.add(renameAction);
			menu.add(invisibleSeparator(EDIT_SECTION_END_SEPARATOR));

			menu.add(new Separator());
			
			menu.add(invisibleSeparator(CONTROL_SERVER_SECTION_START_SEPARATOR));
			for (int i = 0; i < actions.length; i++)
				menu.add(actions[i]);
			menu.add(invisibleSeparator(CONTROL_SERVER_SECTION_END_SEPARATOR));
			
			menu.add(new Separator());
			
			menu.add(invisibleSeparator(SERVER_ERATA_SECTION_START_SEPARATOR));
			menu.add(actionModifyModules);
			addMonitor(server, menu, shell);
			menu.add(invisibleSeparator(SERVER_ERATA_SECTION_END_SEPARATOR));
			menu.add(new Separator());
			menu.add(propertiesAction);
			
		} else if (server != null && module != null) {
			
			menu.add(invisibleSeparator(CONTROL_MODULE_SECTION_START_SEPARATOR));
			menu.add(new StartModuleAction(server, module));
			menu.add(new StopModuleAction(server, module));			
			menu.add(new RestartModuleAction(server, module));
			if(module.length == 1) {
				menu.add(new RemoveModuleAction(shell, server, module[0]));
			}
			menu.add(invisibleSeparator(CONTROL_MODULE_SECTION_END_SEPARATOR));
		}
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end")); //$NON-NLS-1$
	}

	protected void addTopSection(IMenuManager menu, IServer server, IModule[] module) {
		MenuManager newMenu = new MenuManager(Messages.actionNew, NEW_MENU_ID);
		IAction newServerAction = new NewServerWizardAction();
		newServerAction.setText(Messages.actionNewServer);
		newMenu.add(newServerAction);
		menu.add(newMenu);
		
		// open action
		if (server != null && module == null) {
			menu.add(openAction);
			
			String text = Messages.actionShowIn;
			final IWorkbench workbench = PlatformUI.getWorkbench();
			final IBindingService bindingService = (IBindingService) workbench
					.getAdapter(IBindingService.class);
			final TriggerSequence[] activeBindings = bindingService
					.getActiveBindingsFor("org.eclipse.ui.navigate.showInQuickMenu"); //$NON-NLS-1$
			if (activeBindings.length > 0) {
				text += "\t" + activeBindings[0].format(); //$NON-NLS-1$
			}
			
			MenuManager showInMenu = new MenuManager(text, "org.eclipse.ui.navigate.showInQuickMenu"); //$NON-NLS-1$
			showInMenu.add(showInConsoleAction);
			showInMenu.add(showInDebugAction);
			menu.add(showInMenu);
		}
	}
	
	protected void addMonitor(IServer server, IMenuManager menu, Shell shell) {

		if (server.getServerType() != null) {
			final MenuManager menuManager = new MenuManager(Messages.actionMonitor);
			
			final IServer server2 = server;
			final Shell shell2 = shell;
			menuManager.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					menuManager.removeAll();
					if (server2.getAdapter(ServerDelegate.class) != null) {
						ServerPort[] ports = server2.getServerPorts(null);
						if (ports != null) {
							int size = ports.length;
							for (int i = 0; i < size; i++) {
								if (!ports[i].isAdvanced())
									menuManager.add(new MonitorServerPortAction(shell2, server2, ports[i]));
							}
						}
					}
					
					if (menuManager.isEmpty())
						menuManager.add(noneAction);
					
					menuManager.add(new Separator());
					menuManager.add(monitorPropertiesAction);
				}
			});
			
			// add an initial menu item so that the menu appears correctly
			noneAction.setEnabled(false);
			menuManager.add(noneAction);
			menu.add(menuManager);
		}
	}
	
	
	private Separator invisibleSeparator(String s) {
		Separator sep = new Separator(s);
		sep.setVisible(false);
		return sep;
	}
}
