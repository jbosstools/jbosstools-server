package org.jboss.ide.eclipse.as.ui.preferencepages;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Node;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.SimpleTreeItem;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel.XPathTreeItem;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel.XPathTreeItem2;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper.SimpleXPathPreferenceTreeItem;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper.XPathPreferenceTreeItem;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathCategoryDialog;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathDialog;


public class JBossServersPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public JBossServersPreferencePage() {
		super();
	}

	public JBossServersPreferencePage(String title) {
		super(title);
	}

	public JBossServersPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.BORDER);
		main.setLayout(new FormLayout());
		
		createServerViewer(main);
		createTimeoutGroup(main);
		addListeners();
		
		
		// minimum width enforcer
		Label l = new Label(main, SWT.NONE);
		FormData lData = new FormData();
		lData.left = new FormAttachment(0,0);
		lData.right = new FormAttachment(0,600);
		lData.bottom = new FormAttachment(100,0);
		lData.top = new FormAttachment(100,0);
		l.setLayoutData(lData);
		main.layout();

		
		return main;
	}
	
	private JBossServer currentServer;

	private Group timeoutGroup;
	private Table serverTable;
	private TableViewer serverTableViewer;
	private Spinner stopSpinner, startSpinner;
	
	private HashMap workingCoppies;	
	
	private Action newXPathCategoryAction, newXPathAction, deleteXPathCategoryAction, 
					deleteXPathAction, editXPathAction;
		
	// where the page fold is
	int pageColumn = 55;
	
	
	protected void createServerViewer(Composite main) {
		
		workingCoppies = new HashMap();
		
		serverTable = new Table(main, SWT.BORDER);
		FormData lData = new FormData();
		lData.left = new FormAttachment(0,5);
		lData.right = new FormAttachment(pageColumn-2,0);
		lData.top = new FormAttachment(0,5);
		lData.bottom = new FormAttachment(0,80);
		serverTable.setLayoutData(lData);
		
		serverTableViewer = new TableViewer(serverTable);
		serverTableViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return JBossServerCore.getAllJBossServers();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		serverTableViewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				if( element instanceof JBossServer ) {
					return ServerUICore.getLabelProvider().getImage(((JBossServer)element).getServer());
				}
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if( element instanceof JBossServer ) return ((JBossServer)element).getServer().getName();
				return element.toString();
			}

			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		serverTableViewer.setInput("");
		
	}

	protected void createTimeoutGroup(Composite main) {
		timeoutGroup = new Group(main, SWT.NONE);
		timeoutGroup.setText("Server Timeouts");
		FormData groupData = new FormData();
		groupData.right = new FormAttachment(100, -5);
		groupData.left = new FormAttachment(pageColumn+2, 0);
		groupData.top = new FormAttachment(0,5);
		timeoutGroup.setLayoutData(groupData);
		
		timeoutGroup.setLayout(new FormLayout());
		
		// add two textboxes, two labels
		Label startTimeoutLabel, stopTimeoutLabel;
		
		startTimeoutLabel = new Label(timeoutGroup, SWT.NONE);
		stopTimeoutLabel = new Label(timeoutGroup, SWT.NONE);
		
		stopSpinner = new Spinner(timeoutGroup, SWT.BORDER);
		startSpinner = new Spinner(timeoutGroup, SWT.BORDER);

		FormData startTD = new FormData();
		startTD.left = new FormAttachment(0,5);
		startTD.top = new FormAttachment(0,5);
		startTimeoutLabel.setLayoutData(startTD);
		startTimeoutLabel.setText("Start Timeout");
		
		
		FormData stopTD = new FormData();
		stopTD.left = new FormAttachment(0,5);
		stopTD.top = new FormAttachment(startSpinner,4);
		stopTimeoutLabel.setLayoutData(stopTD);
		stopTimeoutLabel.setText("Stop Timeout");
		
		timeoutGroup.layout();
		int startWidth = startTimeoutLabel.getSize().x;
		int stopWidth = stopTimeoutLabel.getSize().x;
		
		Label widest = startWidth > stopWidth ? startTimeoutLabel : stopTimeoutLabel;
		
		FormData startD = new FormData();
		startD.left = new FormAttachment(0,widest.getSize().x + widest.getLocation().x + 5);
		startD.right = new FormAttachment(100, -5);
		startD.top = new FormAttachment(0,5);
		startSpinner.setLayoutData(startD);
		
		FormData stopD = new FormData();
		stopD.left = new FormAttachment(0,widest.getSize().x + widest.getLocation().x + 5);
		stopD.right = new FormAttachment(100, -5);
		stopD.top = new FormAttachment(startSpinner,5);
		stopSpinner.setLayoutData(stopD);
		
		
		stopSpinner.setMinimum(0);
		startSpinner.setMinimum(0);
		stopSpinner.setIncrement(1000);
		startSpinner.setIncrement(1000);
		
		
	}
	
		private void addListeners() {
		serverTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)serverTableViewer.getSelection();
				serverSelected(sel.getFirstElement() == null ? null : (JBossServer)sel.getFirstElement());
			}
		});
		
		startSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getSelectedWC().setStartTimeout(startSpinner.getSelection());
			} 
		});
		stopSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getSelectedWC().setStopTimeout(stopSpinner.getSelection());
			} 
		});
		
				
	}
	
	private void createActions() { }
		/*
		 * newXPathCategoryAction, newXPathAction, deleteXPathCategoryAction, 
					deleteXPathAction, editXPathCategoryAction, editXPathAction;
		 
}
	
	private void addViewerMenus() {
		MenuManager menuManager = new MenuManager("#PopupMenu"); 
		menuManager.setRemoveAllWhenShown(true);
		final Shell shell = xpathTree.getShell();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				xpathTreeMenuAboutToShow(shell, mgr);
			}
		});
		Menu menu = menuManager.createContextMenu(xpathTree);
		xpathTree.setMenu(menu);
	}
	
	private void xpathTreeMenuAboutToShow(Shell shell, IMenuManager menu) {
		menu.add(newXPathCategoryAction);
		menu.add(deleteXPathCategoryAction);
		menu.add(newXPathAction);
		menu.add(editXPathAction);
	}
	*/
	
	private void serverSelected(JBossServer server) {
		currentServer = server;
		ServerAttributeHelper wcHelper = getWCHelper(server);
		
		/* Handle spinners */
		startSpinner.setMaximum(((ServerType)server.getServer().getServerType()).getStartTimeout());
		stopSpinner.setMaximum(((ServerType)server.getServer().getServerType()).getStopTimeout());
		startSpinner.setSelection(wcHelper.getStartTimeout());
		stopSpinner.setSelection(wcHelper.getStopTimeout());
		

	}
	
	
	private ServerAttributeHelper getWCHelper(JBossServer server) {
		if( workingCoppies.get(server) == null ) {
			ServerAttributeHelper ret = server.getAttributeHelper();
			workingCoppies.put(server, ret);
			return ret;
		}
		
		return (ServerAttributeHelper)workingCoppies.get(server);
	}
	
	private ServerAttributeHelper getSelectedWC() {
		IStructuredSelection sel = (IStructuredSelection)serverTableViewer.getSelection();
		if( sel != null && sel.getFirstElement() != null ) {
			return getWCHelper((JBossServer)sel.getFirstElement());
		}
		return null;
	}
	
	public void init(IWorkbench workbench) {
	}
	
	public Object getFirstSelected(Viewer viewer) {
		ISelection sel = viewer.getSelection();
		if( sel instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection)sel).getFirstElement();
			return o;
		}
		return null;
	}
	
	
    public boolean performOk() {
    	super.performOk();
    	saveDirtyWorkingCoppies();
    	return true;
    }
    
    /* Saves the actual ServerWorkingCopy changes into the IServer it relates to. */
	private void saveDirtyWorkingCoppies() {
    	Collection c = workingCoppies.values();
    	Iterator i = c.iterator();
    	Object o;
    	IServerWorkingCopy copy;
    	while(i.hasNext()) {
    		o = i.next();
    		if( o instanceof ServerAttributeHelper) {
    			copy = ((ServerAttributeHelper)o).getServer();
    			if( copy.isDirty()) {
    				try {
    					copy.save(true, new NullProgressMonitor());
    				} catch( CoreException ce) {
    					ce.printStackTrace();
    				}
    			}
    		}
    	}
	}
	
    public boolean performCancel() {
    	super.performCancel();
    	// get rid of dirty working coppies (no action needed)
    	
        return true;
    }
}
