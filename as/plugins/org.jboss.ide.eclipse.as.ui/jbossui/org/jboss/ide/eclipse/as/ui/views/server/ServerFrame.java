package org.jboss.ide.eclipse.as.ui.views.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.Trace;
import org.eclipse.wst.server.ui.internal.ContextIds;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.actions.NewServerWizardAction;
import org.eclipse.wst.server.ui.internal.view.servers.DeleteAction;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleSloshAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishCleanAction;
import org.eclipse.wst.server.ui.internal.view.servers.StartAction;
import org.eclipse.wst.server.ui.internal.view.servers.StopAction;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.dialogs.TwiddleDialog;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView.IServerViewFrame;

public class ServerFrame extends Composite implements IServerViewFrame {

	protected Action[] actions;
	protected IWorkbenchPartSite site;
	protected IViewSite viewSite;
	protected Tree treeTable;
	protected ServerTableViewer tableViewer;
	protected Action editLaunchConfigAction;
	protected Action twiddleAction;
	protected Action newServerAction;

	public ServerFrame(Composite parent, JBossServerView view) {
		super(parent, SWT.BORDER);
		setLayout(new FillLayout());
		this.site = view.getSite();
		this.viewSite = view.getViewSite();
		
		
		int cols[] = new int[] {150, 30};
		
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
		column2.setText(Messages.viewStatus);
		column2.setWidth(cols[1]);
					
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
		menu.add(newServerAction);
		if( getSelectedServer() != null ) {
			menu.add(new Separator());
			menu.add(new DeleteAction(new Shell(), getSelectedServer()));
			menu.add(new Separator());
			menu.add(actions[1]);
			menu.add(actions[0]);
			menu.add(actions[3]);
			menu.add(actions[4]);
			menu.add(actions[5]);
			menu.add(new Separator());
			menu.add(twiddleAction);
			menu.add(editLaunchConfigAction);
			menu.add(actions[6]);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			twiddleAction.setEnabled(true);
			editLaunchConfigAction.setEnabled(true);
		} else {
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

	public IServer getSelectedServer() {
		Object o = ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
		return (IServer)o;
	}
	
	public void initializeActions(ISelectionProvider provider) {
		Shell shell = site.getShell();

		createActions();
		
		actions = new Action[] {
				// create the start actions
				new StartAction(shell, provider, ILaunchManager.DEBUG_MODE),
				new StartAction(shell, provider, ILaunchManager.RUN_MODE),
				new StartAction(shell, provider, ILaunchManager.PROFILE_MODE),
		
				// create the stop action
				new StopAction(shell, provider),
		
				// create the publish actions
				new PublishAction(shell, provider),
				new PublishCleanAction(shell, provider),
				new ModuleSloshAction(shell, provider)
		};
	}
	
	protected void createActions() {
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
		return new IAction[] { actions[0], actions[1], actions[2], actions[3], actions[4], actions[5] };
	}

	public int getDefaultSize() {
		return 0;
	}
}
