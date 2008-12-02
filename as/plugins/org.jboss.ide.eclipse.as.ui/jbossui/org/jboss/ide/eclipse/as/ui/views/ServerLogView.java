package org.jboss.ide.eclipse.as.ui.views;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.views.log.Messages;
import org.eclipse.ui.internal.views.log.SharedImages;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.IServerLogListener;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class ServerLogView extends ViewPart implements IServerLogListener {
	public static final String VIEW_ID = "org.jboss.ide.eclipse.as.ui.view.serverLogView";

	private IServer server;
	private TreeViewer viewer;
	private File fInputFile;
	private IMemento fMemento;
	private Action fDeleteLogAction, fOpenLogAction;
	private List<AbstractEntry> elements;
	public ServerLogView() {
		super();
		elements = new ArrayList<AbstractEntry>();
	}
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fMemento = memento;
	}
	public void createPartControl(Composite parent) {
		Composite child = new Composite(parent, SWT.NONE);
		child.setLayout(new FillLayout());
		viewer = new TreeViewer(child);
		viewer.setContentProvider(new LogContentProvider());
		viewer.setLabelProvider(new LogLabelProvider());
		viewer.setInput(this);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				Object[] arr = sel.toArray();
				viewer.update(arr, null);
			} 
		});
		
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		fDeleteLogAction = createDeleteLogAction();
		toolbar.add(fDeleteLogAction);
		fOpenLogAction = createOpenLogAction();
		toolbar.add(fOpenLogAction);
		
		setLogFile(null);
	}

	public void setServer(IServer server) {
		if( this.server != null )
			ServerLogger.getDefault().removeListener(this.server, this);
		this.server = server;
		ServerLogger.getDefault().addListener(server, this);
		setLogFile(ServerLogger.getDefault().getServerLogFile(server));
	}
	
	public void dispose() {
		super.dispose();
	}
	
	protected void setLogFile(File file) {
		fInputFile = file;
		elements.clear();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("Importing Server Log", IProgressMonitor.UNKNOWN);
				List<AbstractEntry> entries = new ArrayList<AbstractEntry>();
				LogReader.parseLogFile(fInputFile, entries, fMemento);
				elements.addAll(entries);
			}
		};
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getViewSite().getShell());
		try {
			pmd.run(true, true, op);
		} catch (InvocationTargetException e) { // do nothing
		} catch (InterruptedException e) { // do nothing
		} finally {
			asyncRefresh(false);
		}
		
		updateButtons();
	}

	private void updateButtons() {
		fDeleteLogAction.setEnabled(fInputFile != null && fInputFile.exists());
		fOpenLogAction.setEnabled(fInputFile != null && fInputFile.exists());
	}
	
	private void asyncRefresh(final boolean activate) {
		if (viewer.getTree().isDisposed())
			return;
		Display display = viewer.getTree().getDisplay();
		final ViewPart view = this;
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					IServer selected = ServerLogger.findServerForFile(fInputFile);
					setContentDescription(selected == null ? "No Log Selected" : selected.getName());
					if (!viewer.getTree().isDisposed()) {
						viewer.refresh();
						viewer.expandToLevel(2);
						if (activate) {
							IWorkbenchPage page = JBossServerUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
							if (page != null)
								page.bringToTop(view);
						}
					}
				}
			});
		}
	}

	public void logging(IStatus status, IServer server) {
		if( server != null && server.equals(this.server)) {
			LogEntry entry = new LogEntry(status);
			elements.add(entry);
			asyncRefresh(false);
		}
	}

	public void setFocus() {
	}
	
	// providers
	private class LogContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof AbstractEntry ) {
				return ((AbstractEntry)parentElement).getChildren(parentElement);
			}
			
			if( parentElement instanceof EventCategory ) {
				int type = ((EventCategory)parentElement).getType();
				int type2;
				ArrayList<AbstractEntry> returnable = new ArrayList<AbstractEntry>();
				AbstractEntry[] entries = ServerLogView.this.getElements();
				for( int i = 0; i < entries.length; i++ ) {
					if( entries[i] instanceof LogEntry ) {
						type2 = ((LogEntry)entries[i]).getCode() & IEventCodes.MAJOR_TYPE_MASK;
						if( type == type2 )
							returnable.add(entries[i]);
					}
				}
				return returnable.toArray(new AbstractEntry[returnable.size()]);
			}
			
			return new Object[]{};
		}

		public Object getParent(Object element) {
			if(element instanceof AbstractEntry )
				return ((AbstractEntry)element).getParent(null);
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			if( !shouldSort()) {
				return ServerLogView.this.getElements();
			}
			ArrayList<EventCategory> cats = new ArrayList<EventCategory>();
			AbstractEntry[] entries = ServerLogView.this.getElements();
			for( int i = 0; i < entries.length; i++ ) {
				if( entries[i] instanceof LogEntry ) {
					int type = ((LogEntry)entries[i]).getCode() & IEventCodes.MAJOR_TYPE_MASK;
					if( !cats.contains(new EventCategory(type)))
						cats.add(new EventCategory(type));
				}
			}
			return cats.toArray(new EventCategory[cats.size()]);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	public static class EventCategory {
		int val;
		EventCategory(int i) {
			this.val = i;
		}
		public int getType() {
			return val;
		}
		public boolean equals(Object other) {
			return other instanceof EventCategory && ((EventCategory)other).getType() == val;
		}
	}
	
	public boolean shouldSort() {
		return true;
	}
	
	public AbstractEntry[] getElements() {
		return elements.toArray(new AbstractEntry[elements.size()]);
	}

	
	
	/* Stolen from log view and can be changed but in a rush */
	private Action createOpenLogAction() {
		Action action = new Action() {
			public void run() {
				openLog();
			}
		};
		action.setText(Messages.LogView_view_currentLog);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_OPEN_LOG));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_OPEN_LOG_DISABLED));
		action.setEnabled(fInputFile != null && fInputFile.exists());
		action.setToolTipText(Messages.LogView_view_currentLog_tooltip);
		return action;
	}
	
	private void openLog() {
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = win.getActivePage();
			IFile eclipseFile = ResourcesPlugin.getWorkspace()
					.getRoot().getFileForLocation(
							new Path(fInputFile.getAbsolutePath()));
			IFileStore fileStore = EFS.getLocalFileSystem()
					.fromLocalFile(fInputFile);
			if (eclipseFile != null) {
				IEditorInput input = new FileEditorInput(
						eclipseFile);
				IEditorDescriptor desc = PlatformUI.getWorkbench()
						.getEditorRegistry().getDefaultEditor(
								fInputFile.getName());
				if (desc != null)
					page.openEditor(input, desc.getId());
			} else if (fileStore != null) {
				IEditorInput input = new FileStoreEditorInput(
						fileStore);
				IEditorDescriptor desc = PlatformUI.getWorkbench()
						.getEditorRegistry().getDefaultEditor(
								"dummy.txt");
				if (desc != null)
					page.openEditor(input, desc.getId());
			}
		} catch( PartInitException pie) {
			IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, pie.getMessage(), pie);
			JBossServerUIPlugin.getDefault().getLog().log(status);
		}
	}
	
	private Action createDeleteLogAction() {
		Action action = new Action(Messages.LogView_delete) {
			public void run() {
				doDeleteLog();
			}
		};
		action.setToolTipText(Messages.LogView_delete_tooltip);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_REMOVE_LOG));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_REMOVE_LOG_DISABLED));
		action.setEnabled(fInputFile != null && fInputFile.exists());
		return action;
	}
	
	private void doDeleteLog() {
		String title = Messages.LogView_confirmDelete_title;
		String message = Messages.LogView_confirmDelete_message;
		if (!MessageDialog.openConfirm(viewer.getTree().getShell(), title, message))
			return;
		if (fInputFile.delete() || elements.size() > 0) {
			handleClear();
		}
	}
	
	protected void handleClear() {
		BusyIndicator.showWhile(viewer.getTree().getDisplay(), new Runnable() {
			public void run() {
				elements.clear();
				asyncRefresh(false);
			}
		});
	}

}
