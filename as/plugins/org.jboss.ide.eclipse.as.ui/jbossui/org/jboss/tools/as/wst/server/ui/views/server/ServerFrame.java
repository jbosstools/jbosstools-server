/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.tools.as.wst.server.ui.views.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.Trace;
import org.eclipse.wst.server.ui.internal.ContextIds;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.actions.NewServerWizardAction;
import org.eclipse.wst.server.ui.internal.view.servers.CopyAction;
import org.eclipse.wst.server.ui.internal.view.servers.DeleteAction;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleSloshAction;
import org.eclipse.wst.server.ui.internal.view.servers.OpenAction;
import org.eclipse.wst.server.ui.internal.view.servers.PasteAction;
import org.eclipse.wst.server.ui.internal.view.servers.PropertiesAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishCleanAction;
import org.eclipse.wst.server.ui.internal.view.servers.RenameAction;
import org.eclipse.wst.server.ui.internal.view.servers.ShowInConsoleAction;
import org.eclipse.wst.server.ui.internal.view.servers.ShowInDebugAction;
import org.eclipse.wst.server.ui.internal.view.servers.StartAction;
import org.eclipse.wst.server.ui.internal.view.servers.StopAction;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.dialogs.TwiddleDialog;
import org.jboss.tools.as.wst.server.ui.views.server.JBossServerView.IServerViewFrame;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServerFrame extends Composite implements IServerViewFrame {

	protected Action[] actions;
	protected IWorkbenchPartSite site;
	protected IViewSite viewSite;
	protected Tree treeTable;
	protected ServerTableViewer tableViewer;
	
	// custom
	protected Action editLaunchConfigAction, twiddleAction, newServerAction;

	// wtp
	protected Action actionModifyModules;
	protected Action openAction, showInConsoleAction, showInDebugAction, propertiesAction;
	protected Action copyAction, pasteAction, deleteAction, renameAction;

	public ServerFrame(Composite parent, JBossServerView view) {
		super(parent, SWT.BORDER);
		setLayout(new FillLayout());
		this.site = view.getSite();
		this.viewSite = view.getViewSite();
		
		
		int cols[] = new int[] {150, 100, 50};
		
		treeTable = new Tree(this, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NONE);
		treeTable.setHeaderVisible(true);
		treeTable.setLinesVisible(false);
		treeTable.setFont(parent.getFont());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(treeTable, ContextIds.VIEW_SERVERS);
		
		// add columns
		TreeColumn column = new TreeColumn(treeTable, SWT.SINGLE);
		column.setText(Messages.viewServer);
		column.setWidth(cols[0]);
		
		TreeColumn column2 = new TreeColumn(treeTable, SWT.SINGLE);
		column2.setText(Messages.viewState);
		column2.setWidth(cols[1]);
					
		TreeColumn column3 = new TreeColumn(treeTable, SWT.SINGLE);
		column3.setText(Messages.viewStatus);
		column3.setWidth(cols[2]);

		tableViewer = new ServerTableViewer(viewSite, treeTable);
		initializeActions(tableViewer);
		
		treeTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
				} catch (Exception e) {
					viewSite.getActionBars().getStatusLineManager().setMessage(null, "");
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				try {
					TreeItem item = treeTable.getSelection()[0];
					Object data = item.getData();
					if (!(data instanceof IServer))
						return;
					IServer server = (IServer) data;
					ServerUIPlugin.editServer(server);
				} catch (Exception e) {
					Trace.trace(Trace.SEVERE, "Could not open server", e);
				}
			}
		});

		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		final Shell shell = treeTable.getShell();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(shell, mgr);
			}
		});
		Menu menu = menuManager.createContextMenu(parent);
		treeTable.setMenu(menu);
		site.registerContextMenu(menuManager, tableViewer);
		site.setSelectionProvider(tableViewer);		
	}
	
	protected void fillContextMenu(Shell shell, IMenuManager menu) {
		if( getSelectedServer() != null ) {
			cloneFill(shell, menu);
		} else {
			menu.add(newServerAction);
		}
	}

	protected void cloneFill(Shell shell, IMenuManager menu) {
		
		/* Show in ... */
		String text = Messages.actionShowIn;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IBindingService bindingService = (IBindingService) workbench
				.getAdapter(IBindingService.class);
		final TriggerSequence[] activeBindings = bindingService
				.getActiveBindingsFor("org.eclipse.ui.navigate.showInQuickMenu");
		if (activeBindings.length > 0) {
			text += "\t" + activeBindings[0].format();
		}

		menu.add(newServerAction);
		menu.add(openAction);

		MenuManager showInMenu = new MenuManager(text);
		showInMenu.add(showInConsoleAction);
		showInMenu.add(showInDebugAction);
		menu.add(showInMenu);
		menu.add(new Separator());
		
		menu.add(copyAction);
		menu.add(pasteAction);
		menu.add(deleteAction);
		menu.add(renameAction);

		menu.add(new Separator());
		
		// server actions
		for (int i = 0; i < actions.length; i++)
			menu.add(actions[i]);
		
		menu.add(new Separator());
//		menu.add(actionModifyModules);

		menu.add(twiddleAction);
		menu.add(editLaunchConfigAction);
		menu.add(actionModifyModules);
		twiddleAction.setEnabled(true);
		editLaunchConfigAction.setEnabled(true);

	}
	
	public IServer getSelectedServer() {
		Object o = ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
		return (IServer)o;
	}
	
	public void initializeActions(ISelectionProvider provider) {
		createWTPActions(provider);
		createCustomActions();
	}
	
	protected void createWTPActions(ISelectionProvider provider) {
		Shell shell = viewSite.getShell();
		IActionBars actionBars = viewSite.getActionBars();
		
		actions = new Action[6];
		// create the start actions
		actions[0] = new StartAction(shell, provider, ILaunchManager.DEBUG_MODE);
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.debug", actions[0]);
		actions[1] = new StartAction(shell, provider, ILaunchManager.RUN_MODE);
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.run", actions[1]);
		actions[2] = new StartAction(shell, provider, ILaunchManager.PROFILE_MODE);
		
		// create the stop action
		actions[3] = new StopAction(shell, provider);
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.stop", actions[3]);
		
		// create the publish actions
		actions[4] = new PublishAction(shell, provider);
		actionBars.setGlobalActionHandler("org.eclipse.wst.server.publish", actions[4]);
		actions[5] = new PublishCleanAction(shell, provider);
		
		// create the open action
		openAction = new OpenAction(provider);
		actionBars.setGlobalActionHandler("org.eclipse.ui.navigator.Open", openAction);
		
		// create copy, paste, and delete actions
		pasteAction = new PasteAction(shell, provider, tableViewer.clipboard);
		copyAction = new CopyAction(provider, tableViewer.clipboard, pasteAction);
		deleteAction = new DeleteAction(shell, provider);
		renameAction = new RenameAction(shell, tableViewer, provider);
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameAction);
		
		// create the other actions
		actionModifyModules = new ModuleSloshAction(shell, provider);
		showInConsoleAction = new ShowInConsoleAction(provider);
		showInDebugAction = new ShowInDebugAction(provider);
		
		// create the properties action
		propertiesAction = new PropertiesAction(shell, provider);
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
	}
	
	protected void createCustomActions() {
		newServerAction = new Action() {
			public void run() {
				IAction newServerAction = new NewServerWizardAction();
				newServerAction.run();
			}
		};
		newServerAction.setText("New Server");
		newServerAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.GENERIC_SERVER_IMAGE));

		editLaunchConfigAction = new Action() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() { 
					public void run() {
						try {
							final Object selected = getSelectedServer();
							IServer s = null;
							if( selected instanceof JBossServer ) {
								s = ((JBossServer)selected).getServer();
							} else if( selected instanceof IServer ) {
								s = (IServer)selected;
							}
							
							if( s != null ) {
								ILaunchConfiguration launchConfig = ((Server) s).getLaunchConfiguration(true, null);
								// TODO: use correct launch group
								DebugUITools.openLaunchConfigurationPropertiesDialog(new Shell(), launchConfig, "org.eclipse.debug.ui.launchGroup.run");
							}
						} catch (CoreException ce) {
						}
					}
				});
			}
		};
		editLaunchConfigAction.setText(org.jboss.ide.eclipse.as.ui.Messages.EditLaunchConfigurationAction);
		editLaunchConfigAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.IMG_JBOSS_CONFIGURATION));
		
		twiddleAction = new Action() {
			public void run() {
				final IStructuredSelection selected = ((IStructuredSelection)tableViewer.getSelection());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						TwiddleDialog dialog = new TwiddleDialog(tableViewer.getTree().getShell(), selected.getFirstElement());
						dialog.open();
					} 
				} );

			}
		};
		twiddleAction.setText( org.jboss.ide.eclipse.as.ui.Messages.TwiddleServerAction);
		twiddleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.TWIDDLE_IMAGE));

	}
	
	public ServerTableViewer getViewer() {
		return tableViewer;
	}

	public void refresh() {
		tableViewer.refresh();
	}
	
	public IAction[] getActionBarActions() {
		return actions;
	}

	public int getDefaultSize() {
		return 0;
	}
}
