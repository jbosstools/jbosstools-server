package org.jboss.ide.eclipse.as.ui.views.server;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Trace;
import org.eclipse.wst.server.ui.internal.ContextIds;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.actions.NewServerWizardAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishAction;
import org.eclipse.wst.server.ui.internal.view.servers.PublishCleanAction;
import org.eclipse.wst.server.ui.internal.view.servers.StartAction;
import org.eclipse.wst.server.ui.internal.view.servers.StopAction;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;

public class StrippedServerView extends ViewPart {
	private static final String TAG_COLUMN_WIDTH = "columnWidth";

	protected int[] cols;

	protected Tree treeTable;
	protected ServerTableViewer tableViewer;

	// actions on a server
	protected Action[] actions;
	protected MenuManager restartMenu;
	
	protected Action newServerAction;

	/**
	 * ServersView constructor comment.
	 */
	public StrippedServerView() {
		super();
	}

	/**
	 * createPartControl method comment.
	 * 
	 * @param parent a parent composite
	 */
	public void createPartControl(Composite parent) {
		treeTable = new Tree(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NONE);
		treeTable.setHeaderVisible(true);
		treeTable.setLinesVisible(false);
		treeTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		treeTable.setFont(parent.getFont());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(treeTable, ContextIds.VIEW_SERVERS);
		
		// add columns
		TreeColumn column = new TreeColumn(treeTable, SWT.SINGLE);
		column.setText(Messages.viewServer);
		column.setWidth(cols[0]);
		
		TreeColumn column2 = new TreeColumn(treeTable, SWT.SINGLE);
		column2.setText(Messages.viewStatus);
		column2.setWidth(cols[1]);
		
//		TreeColumn column3 = new TreeColumn(treeTable, SWT.SINGLE);
//		column3.setText(Messages.viewSync);
//		column3.setWidth(cols[2]);
		
		tableViewer = new ServerTableViewer(this, treeTable);
		initializeActions(tableViewer);
		
		treeTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
				} catch (Exception e) {
					getViewSite().getActionBars().getStatusLineManager().setMessage(null, "");
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
		getSite().registerContextMenu(menuManager, tableViewer);
		getSite().setSelectionProvider(tableViewer);
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		cols = new int[2];
		for (int i = 0; i < 2; i++) {
			cols[i] = 100;
			if (memento != null) {
				Integer in = memento.getInteger(TAG_COLUMN_WIDTH + i);
				if (in != null && in.intValue() > 5)
					cols[i] = in.intValue();
			}
		}
	}

	public void saveState(IMemento memento) {
		TreeColumn[] tc = treeTable.getColumns();
		for (int i = 0; i < 2; i++) {
			int width = tc[i].getWidth();
			if (width != 0)
				memento.putInteger(TAG_COLUMN_WIDTH + i, width);
		}
	}

	protected void selectServerProcess(Object process) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow() ;
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart part = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
				if (part != null) {
					IDebugView view = (IDebugView)part.getAdapter(IDebugView.class);
					if (view != null) {
						Viewer viewer = view.getViewer();
						if (viewer != null) {
							viewer.setSelection(new StructuredSelection(process));
						}
					}
				}
			}
		}
	}

	/**
	 * Initialize actions
	 * 
	 * @param provider a selection provider
	 */
	public void initializeActions(ISelectionProvider provider) {
		Shell shell = getSite().getShell();
		
		actions = new Action[6];
		// create the start actions
		actions[0] = new StartAction(shell, provider, ILaunchManager.DEBUG_MODE);
		actions[1] = new StartAction(shell, provider, ILaunchManager.RUN_MODE);
		actions[2] = new StartAction(shell, provider, ILaunchManager.PROFILE_MODE);
		
		// create the stop action
		actions[3] = new StopAction(shell, provider);
		
		// create the publish actions
		actions[4] = new PublishAction(shell, provider);
		actions[5] = new PublishCleanAction(shell, provider);
		
		// add toolbar buttons
		IContributionManager cm = getViewSite().getActionBars().getToolBarManager();
		for (int i = 0; i < actions.length - 1; i++)
			cm.add(actions[i]);
		
//		// create the debug action
//		Action debugAction = new StartAction(shell, provider, ILaunchManager.DEBUG_MODE);
//		debugAction.setToolTipText(Messages.actionDebugToolTip);
//		debugAction.setText(Messages.actionDebug);
//		debugAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START_DEBUG));
//		debugAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START_DEBUG));
//		debugAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START_DEBUG));
//	
//		// create the start action
//		Action runAction = new StartAction(shell, provider, ILaunchManager.RUN_MODE);
//		runAction.setToolTipText(Messages.actionStartToolTip);
//		runAction.setText(Messages.actionStart);
//		runAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START));
//		runAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START));
//		runAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START));
//		
//		// create the profile action
//		Action profileAction = new StartAction(shell, provider, ILaunchManager.PROFILE_MODE);
//		profileAction.setToolTipText(Messages.actionProfileToolTip);
//		profileAction.setText(Messages.actionProfile);
//		profileAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START_PROFILE));
//		profileAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START_PROFILE));
//		profileAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START_PROFILE));
//	
//		// create the restart menu
//		restartMenu = new MenuManager(Messages.actionRestart);
//		
//		Action restartAction = new RestartAction(shell, provider, ILaunchManager.DEBUG_MODE);
//		restartAction.setToolTipText(Messages.actionDebugToolTip);
//		restartAction.setText(Messages.actionDebug);
//		restartAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START_DEBUG));
//		restartAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START_DEBUG));
//		restartAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START_DEBUG));
//		restartMenu.add(restartAction);
//		
//		restartAction = new RestartAction(shell, provider, ILaunchManager.RUN_MODE);
//		restartAction.setToolTipText(Messages.actionRestartToolTip);
//		restartAction.setText(Messages.actionStart);
//		restartAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START));
//		restartAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START));
//		restartAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START));
//		restartMenu.add(restartAction);
//		
//		restartAction = new RestartAction(shell, provider, "restartProfile", ILaunchManager.PROFILE_MODE);
//		restartAction.setToolTipText(Messages.actionRestartToolTip);
//		restartAction.setText(Messages.actionProfile);
//		restartAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START_PROFILE));
//		restartAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_START_PROFILE));
//		restartAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_START_PROFILE));
//		restartMenu.add(restartAction);
//		
//		// create the restart action
//		restartAction = new RestartAction(shell, provider, "restart");
//		restartAction.setToolTipText(Messages.actionRestartToolTip);
//		restartAction.setText(Messages.actionRestart);
//		restartAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_RESTART));
//		restartAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_RESTART));
//		restartAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_RESTART));
//
//		// create the stop action
//		Action stopAction = new StopAction(shell, provider, "stop");
//		stopAction.setToolTipText(Messages.actionStopToolTip);
//		stopAction.setText(Messages.actionStop);
//		stopAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_STOP));
//		stopAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_STOP));
//		stopAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_STOP));
//
//		// create the publish action
//		Action publishAction = new PublishAction(shell, provider, "publish");
//		publishAction.setToolTipText(Messages.actionPublishToolTip);
//		publishAction.setText(Messages.actionPublish);
//		publishAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_PUBLISH));
//		publishAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CLCL_PUBLISH));
//		publishAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DLCL_PUBLISH));
//		
//		// create the module slosh dialog action
//		Action addModuleAction = new ModuleSloshAction(shell, provider, "modules");
//		addModuleAction.setToolTipText(Messages.actionModifyModulesToolTip);
//		addModuleAction.setText(Messages.actionModifyModules);
//		addModuleAction.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_ETOOL_MODIFY_MODULES));
//		addModuleAction.setHoverImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_CTOOL_MODIFY_MODULES));
//		addModuleAction.setDisabledImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_DTOOL_MODIFY_MODULES));
//		
//		actions = new Action[7];
//		actions[0] = debugAction;
//		actions[1] = runAction;
//		actions[2] = profileAction;
//		actions[3] = restartAction;
//		actions[4] = stopAction;
//		actions[5] = publishAction;
//		actions[6] = addModuleAction;
//		
//		// add toolbar buttons
//		IContributionManager cm = getViewSite().getActionBars().getToolBarManager();
//		for (int i = 0; i < actions.length - 1; i++) {
//			cm.add(actions[i]);
//		}
		
		newServerAction = new Action() {
			public void run() {
				IAction newServerAction = new NewServerWizardAction();
				newServerAction.run();
			}
		};
		newServerAction.setText("New Server");
		newServerAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.GENERIC_SERVER_IMAGE));
	}

	protected void fillContextMenu(Shell shell, IMenuManager menu) {
	}

	/**
	 * 
	 */
	public void setFocus() {
		if (treeTable != null)
			treeTable.setFocus();
	}
}