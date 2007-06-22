package org.jboss.ide.eclipse.as.ui.views.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.Trace;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.view.servers.DeleteAction;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.TwiddleDialog;

public class JBossServerView extends StrippedServerView {

	private static final String TAG_SASHFORM_HEIGHT = "sashformHeight"; 
		
	protected int[] sashRows; // For the sashform sashRows
	
	protected SashForm form;
	
	protected JBossServerTableViewer jbViewer;
	protected Tree jbTreeTable;
	
	
	protected Action editLaunchConfigAction, twiddleAction, cloneServerAction;
	
	public static JBossServerView instance;
	public static JBossServerView getDefault() {
		return instance;
	}
	
	
	public JBossServerView() {
		super();
		instance = this;		
	}
	
	public JBossServerTableViewer getJBViewer() {
		return jbViewer;
	}
	
	public void createPartControl(Composite parent) {
				
		form = new SashForm(parent, SWT.VERTICAL);
		form.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
		
		form.setLayout(new FillLayout());
		
		addServerViewer(form);
		addSecondViewer(form);
		form.setWeights(sashRows);
		createActions();
		addListeners();
		doMenuStuff(parent);
	}

	private void addServerViewer(Composite form) {
		Composite child1 = new Composite(form,SWT.NONE);
		child1.setLayout(new GridLayout());
		super.createPartControl(child1);
	}
	
	private void addSecondViewer(Composite form) {
		Composite child2 = new Composite(form,SWT.NONE);
		child2.setLayout(new FillLayout());
		jbTreeTable = new Tree(child2, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		jbViewer = new JBossServerTableViewer(jbTreeTable);
		getSite().setSelectionProvider(jbViewer);
	}
	
	
	public void createActions() {
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
							Trace.trace(Trace.SEVERE, "Could not create launch configuration", ce);
						}

					}
				});
			}
		};
		editLaunchConfigAction.setText(Messages.EditLaunchConfigurationAction);
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
		twiddleAction.setText(Messages.TwiddleServerAction);
		twiddleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.TWIDDLE_IMAGE));
		
	}
	
	
	// for superclass, for the top viewer
	protected void fillContextMenu(Shell shell, IMenuManager menu) {
		menu.add(newServerAction);
		menu.add(new Separator());
		if( getSelectedServer() != null ) {
			menu.add(new DeleteAction(new Shell(), getSelectedServer()));
			menu.add(new Separator());
		}
		menu.add(actions[1]);
		menu.add(actions[0]);
		menu.add(actions[4]);
		menu.add(actions[5]);
		menu.add(new Separator());
		menu.add(twiddleAction);
		menu.add(editLaunchConfigAction);
		//menu.add(actions[6]);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		if( getSelectedServer() != null ) {
			boolean twiddleEnabled = getSelectedServer().getServerState() == IServer.STATE_STARTED
									&& ServerConverter.getJBossServer(getSelectedServer()) != null;
			boolean editLaunchEnabled = (JBossServerBehavior)getSelectedServer().loadAdapter(JBossServerBehavior.class, new NullProgressMonitor()) != null;
			twiddleAction.setEnabled(twiddleEnabled);
			editLaunchConfigAction.setEnabled(true);
		} else {
			twiddleAction.setEnabled(false);
			editLaunchConfigAction.setEnabled(false);
		}
	}

	
	
	public IServer getSelectedServer() {
		return (IServer)jbViewer.getInput();
	}
	
	public void addListeners() {
		
		/*
		 * Handles the selection of the server viewer which is embedded in my sashform
		 */
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((TreeSelection)event.getSelection()).getFirstElement();
				Object server = selection;
				if( selection instanceof ModuleServer ) {
					server = ((ModuleServer)selection).server;
				}

				if( selection == null ) return;
				if( server != jbViewer.getInput()) {
					jbViewer.setInput(server); 
				} else {
					jbViewer.refresh();
//					Object[] expanded = jbViewer.getExpandedElements();
//					jbViewer.setInput(server);
//					jbViewer.setExpandedElements(expanded);
				}
			} 
			
		});
	}
	protected void doMenuStuff(Composite parent) {
		MenuManager menuManager = new MenuManager("#PopupMenu"); 
		menuManager.setRemoveAllWhenShown(true);
		final Shell shell = jbTreeTable.getShell();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				jbViewer.fillSelectedContextMenu(shell, mgr);
				mgr.add(new Separator());
				jbViewer.fillJBContextMenu(shell, mgr);
			}
		});
		Menu menu = menuManager.createContextMenu(parent);
		jbTreeTable.setMenu(menu);
	}
	
	/**
	 * Save / Load some state  (width / height of boxes)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ServerUIPlugin.getPreferences().setShowOnActivity(false);
		int sum = 0;
		sashRows = new int[2];
		for (int i = 0; i < sashRows.length; i++) {
			sashRows[i] = 50;
			if (memento != null) {
				Integer in = memento.getInteger(TAG_SASHFORM_HEIGHT + i);
				if (in != null && in.intValue() > 5)
					sashRows[i] = in.intValue();
			}
			sum += sashRows[i];
		}
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		int[] weights = form.getWeights();
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0)
				memento.putInteger(TAG_SASHFORM_HEIGHT + i, weights[i]);
		}
	}

	
	
	public Object getAdapter(Class adaptor) {
		if( adaptor == IPropertySheetPage.class) {
			return jbViewer.getPropertySheet();
		}
		return super.getAdapter(adaptor);
	}
	
    public void dispose() {
    	super.dispose();
    	jbViewer.dispose();
    }



}