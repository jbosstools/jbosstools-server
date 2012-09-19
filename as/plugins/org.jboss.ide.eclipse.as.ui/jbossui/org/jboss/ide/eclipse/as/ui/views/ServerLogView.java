package org.jboss.ide.eclipse.as.ui.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.views.log.Messages;
import org.eclipse.ui.internal.views.log.SharedImages;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IServerLogListener;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class ServerLogView extends ViewPart implements IServerLogListener, ISelectionListener {
	public static final String VIEW_ID = "org.jboss.ide.eclipse.as.ui.view.serverLogView"; //$NON-NLS-1$
	public final static byte MESSAGE = 0x0;
	public final static byte DATE = 0x1;
	public static int ASCENDING = 1;
	public static int DESCENDING = -1;
	
	private int MESSAGE_ORDER;
	private int DATE_ORDER;
	private static final String P_COLUMN_1 = "column1"; //$NON-NLS-1$
	private static final String P_COLUMN_2 = "column2"; //$NON-NLS-1$
	public static final String P_ORDER_TYPE = "orderType"; //$NON-NLS-1$
	public static final String P_ORDER_VALUE = "orderValue"; //$NON-NLS-1$

	private IServer server;
	private TreeViewer viewer;
	private File fInputFile;
	private String fDirectory;
	private IMemento fMemento;
	private Action fDeleteLogAction, fOpenLogAction, 
					fReadLogAction, fExportLogAction,
					fReLogErrorsAction;
	private TreeColumn fColumn1;
	private TreeColumn fColumn2;
	private Comparator fComparator;
	private List<AbstractEntry> elements;
	public ServerLogView() {
		super();
		elements = new ArrayList<AbstractEntry>();
	}
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ISelectionService service = (ISelectionService) getSite().getService(ISelectionService.class);
		service.addSelectionListener(this);
		if (memento == null)
			this.fMemento = XMLMemento.createWriteRoot("SERVERLOGVIEW"); //$NON-NLS-1$
		else
			this.fMemento = memento;
		fMemento.putInteger(P_COLUMN_1, fMemento.getInteger(P_COLUMN_1) != null && fMemento.getInteger(P_COLUMN_1) > 0 ? fMemento.getInteger(P_COLUMN_1) : 350);
		fMemento.putInteger(P_COLUMN_2, fMemento.getInteger(P_COLUMN_2) != null && fMemento.getInteger(P_COLUMN_2) > 0 ? fMemento.getInteger(P_COLUMN_2) : 150);
		fMemento.putInteger(P_ORDER_VALUE, fMemento.getInteger(P_ORDER_VALUE) != null && fMemento.getInteger(P_ORDER_VALUE) != 0 ? fMemento.getInteger(P_ORDER_VALUE) : DESCENDING);
		fMemento.putInteger(P_ORDER_TYPE, fMemento.getInteger(P_ORDER_TYPE) != null && fMemento.getInteger(P_ORDER_TYPE) != 0 ? fMemento.getInteger(P_ORDER_TYPE) : DATE);

		// initialize column ordering 
		final byte type = this.fMemento.getInteger(P_ORDER_TYPE).byteValue();
		switch (type) {
			case DATE :
				DATE_ORDER = this.fMemento.getInteger(P_ORDER_VALUE).intValue();
				MESSAGE_ORDER = DESCENDING;
				break;
			case MESSAGE :
				MESSAGE_ORDER = this.fMemento.getInteger(P_ORDER_VALUE).intValue();
				DATE_ORDER = DESCENDING;
				break;
			default :
				DATE_ORDER = DESCENDING;
				MESSAGE_ORDER = DESCENDING;
		}
		setComparator(fMemento.getInteger(P_ORDER_TYPE).byteValue());
	}
	public void saveState(IMemento memento) {
		if (this.fMemento == null || memento == null)
			return;
		this.fMemento.putInteger(P_COLUMN_1, fColumn1.getWidth());
		this.fMemento.putInteger(P_COLUMN_2, fColumn2.getWidth());
		memento.putMemento(this.fMemento);
	}
	
	public void createPartControl(Composite parent) {
		Composite child = new Composite(parent, SWT.NONE);
		child.setLayout(new FillLayout());
		viewer = new TreeViewer(child);
		createColumns(viewer);
		viewer.setContentProvider(new LogContentProvider());
		viewer.setLabelProvider(new LogLabelProvider());
		viewer.setInput(this);
		viewer.setAutoExpandLevel(2);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				Object[] arr = sel.toArray();
				viewer.update(arr, null);
			} 
		});
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object[] o = ((IStructuredSelection)viewer.getSelection()).toArray();
				if( o != null && o.length > 0 && o[0] != null && o[0] instanceof AbstractEntry) 
					new EventDetailsDialog(viewer.getTree().getShell(), (AbstractEntry)o[0], viewer).open();
			}
		});
		
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		fReLogErrorsAction = createReLogErrorAction();
		updateReLogActionText();
		toolBarManager.add(fReLogErrorsAction);
		toolBarManager.add(new Separator());
		fExportLogAction = createExportLogAction();
		toolBarManager.add(fExportLogAction);
		toolBarManager.add(new Separator());
		final Action clearAction = createClearAction();
		toolBarManager.add(clearAction);
		fDeleteLogAction = createDeleteLogAction();
		toolBarManager.add(fDeleteLogAction);
		fOpenLogAction = createOpenLogAction();
		toolBarManager.add(fOpenLogAction);
		fReadLogAction = createReadLogAction();
		toolBarManager.add(fReadLogAction);
		setLogFile(null);
	}

	protected void createColumns(final TreeViewer viewer) {
		fColumn1 = new TreeColumn(viewer.getTree(), SWT.LEFT);
		fColumn1.setText(Messages.LogView_column_message);
		fColumn1.setWidth(fMemento.getInteger(P_COLUMN_1).intValue());
		fColumn1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MESSAGE_ORDER *= -1;
				ViewerComparator comparator = getViewerComparator(MESSAGE);
				viewer.setComparator(comparator);
				setComparator(MESSAGE);
				fMemento.putInteger(P_ORDER_VALUE, MESSAGE_ORDER);
				fMemento.putInteger(P_ORDER_TYPE, MESSAGE);
				setColumnSorting(fColumn1, MESSAGE_ORDER);
			}
		});


		fColumn2 = new TreeColumn(viewer.getTree(), SWT.LEFT);
		fColumn2.setText(Messages.LogView_column_date);
		fColumn2.setWidth(fMemento.getInteger(P_COLUMN_2).intValue());
		fColumn2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DATE_ORDER *= -1;
				ViewerComparator comparator = getViewerComparator(DATE);
				viewer.setComparator(comparator);
				setComparator(DATE);
				fMemento.putInteger(P_ORDER_VALUE, DATE_ORDER);
				fMemento.putInteger(P_ORDER_TYPE, DATE);
				setColumnSorting(fColumn2, DATE_ORDER);
			}
		});

		viewer.getTree().setHeaderVisible(true);
	}
	
	private void setColumnSorting(TreeColumn column, int order) {
		viewer.getTree().setSortColumn(column);
		viewer.getTree().setSortDirection(order == ASCENDING ? SWT.UP : SWT.DOWN);
	}
	
	public void setServer(IServer server) {
		if( this.server != null )
			ServerLogger.getDefault().removeListener(this.server, this);
		this.server = server;
		ServerLogger.getDefault().addListener(server, this);
		setLogFile(ServerLogger.getDefault().getServerLogFile(server));
	}
	
	public void dispose() {
		ISelectionService service = (ISelectionService) getSite().getService(ISelectionService.class);
		service.removeSelectionListener(this);
		super.dispose();
	}
	
	protected void setLogFile(File file) {
		fInputFile = file;
		fDirectory = fInputFile == null ? null : fInputFile.getParent();
		elements.clear();
		reloadLog();
	}
	
	private class ReloadLogJob extends Job {

		public ReloadLogJob() {
			super(org.jboss.ide.eclipse.as.ui.Messages.ServerLogView_ImportingLogTaskName);
		}

		protected IStatus run(IProgressMonitor monitor) {
			List<AbstractEntry> entries = new ArrayList<AbstractEntry>();
			LogReader.parseLogFile(fInputFile, entries, fMemento);
			if( !monitor.isCanceled()) {
				elements.addAll(entries);
				Display.getDefault().asyncExec(new Runnable(){
						public void run(){
							asyncRefresh(false);
							updateButtons();
						}
					});
			}
			return Status.OK_STATUS;
		}
		
	}
	
	private ReloadLogJob reloadJob = null;
	protected void reloadLog() {
		if( reloadJob != null && reloadJob.getResult() == null ) {
			reloadJob.cancel();
		}
		
		reloadJob = new ReloadLogJob();
		reloadJob.schedule();
	}

	private void updateButtons() {
		fDeleteLogAction.setEnabled(exists(fInputFile));
		fOpenLogAction.setEnabled(exists(fInputFile));
		fExportLogAction.setEnabled(exists(fInputFile));
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
					setContentDescription(selected == null ? org.jboss.ide.eclipse.as.ui.Messages.ServerLogView_NoLogSelected : selected.getName());
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

	protected boolean exists(File f) {
		return f != null && f.exists();
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
		public int hashCode() {
			return val;
		}
	}
	
	public boolean shouldSort() {
		return true;
	}
	
	public AbstractEntry[] getElements() {
		return elements.toArray(new AbstractEntry[elements.size()]);
	}

	
	
	/* Stolen from log view and can be changed but in a rush */
	private Action createReadLogAction() {
		Action action = new Action(Messages.LogView_readLog_restore) {
			public void run() {
				reloadLog();
			}
		};
		action.setToolTipText(Messages.LogView_readLog_restore_tooltip);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_READ_LOG));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_READ_LOG_DISABLED));
		return action;
	}
	
	private Action createClearAction() {
		Action action = new Action(Messages.LogView_clear) {
			public void run() {
				handleClear();
			}
		};
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_CLEAR));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_CLEAR_DISABLED));
		action.setToolTipText(Messages.LogView_clear_tooltip);
		action.setText(Messages.LogView_clear);
		return action;
	}
	
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
	
	private Action createReLogErrorAction() {
		Action action = new Action("") {
			public void run() {
				handleLogErrorAction();
			}
		};
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_ERROR_ST_OBJ));
		return action;
	}
	
	private void handleLogErrorAction() {
		boolean currentVal = ServerLogger.shouldDoubleLogErrors();
		ServerLogger.setDoubleLogErrors(!currentVal);
		updateReLogActionText();
	}
	
	private void updateReLogActionText() {
		String newVal = !ServerLogger.shouldDoubleLogErrors() ?
				org.jboss.ide.eclipse.as.ui.Messages.LogAction_DoNotLogErrorsToErrorLog :
				org.jboss.ide.eclipse.as.ui.Messages.LogAction_AlsoLogErrorsToErrorLog;
		fReLogErrorsAction.setText(newVal);
		fReLogErrorsAction.setToolTipText(newVal);
	}
	
	private Action createExportLogAction() {
		Action action = new Action(Messages.LogView_export) {
			public void run() {
				handleExport();
			}
		};
		action.setToolTipText(Messages.LogView_export_tooltip);
		action.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_EXPORT));
		action.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.DESC_EXPORT_DISABLED));
		action.setEnabled(fInputFile != null && fInputFile.exists());
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
								"dummy.txt"); //$NON-NLS-1$
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
	
	private void handleExport() {
		FileDialog dialog = new FileDialog(getViewSite().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] {"*.log"}); //$NON-NLS-1$
		if (fDirectory != null)
			dialog.setFilterPath(fDirectory);
		String path = dialog.open();
		if (path != null) {
			if (path.indexOf('.') == -1 && !path.endsWith(".log")) //$NON-NLS-1$
				path += ".log"; //$NON-NLS-1$
			File outputFile = new Path(path).toFile();
			fDirectory = outputFile.getParent();
			if (outputFile.exists()) {
				String message = NLS.bind(Messages.LogView_confirmOverwrite_message, outputFile.toString());
				if (!MessageDialog.openQuestion(getViewSite().getShell(),Messages.LogView_exportLog, message))
					return;
			}

			Reader in = null;
			Writer out = null;
			try {
				out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"); //$NON-NLS-1$
				in = new InputStreamReader(new FileInputStream(fInputFile), "UTF-8"); //$NON-NLS-1$
				copy(in, out);
			} catch (IOException ex) {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				} catch (IOException e1) { // do nothing
				}
			}
		}
	}
	
	private void copy(Reader input, Writer output) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(input);
			writer = new BufferedWriter(output);
			String line;
			while (reader.ready() && ((line = reader.readLine()) != null)) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) { // do nothing
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (IOException e1) {
				// do nothing
			}
		}
	}

	private void setComparator(byte sortType) {
		if (sortType == DATE) {
			fComparator = new Comparator() {
				public int compare(Object e1, Object e2) {
					long date1 = 0;
					long date2 = 0;
					if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
						date1 = ((LogEntry) e1).getDate().getTime();
						date2 = ((LogEntry) e2).getDate().getTime();
					} else if ((e1 instanceof LogSession) && (e2 instanceof LogSession)) {
						date1 = ((LogSession) e1).getDate() == null ? 0 : ((LogSession) e1).getDate().getTime();
						date2 = ((LogSession) e2).getDate() == null ? 0 : ((LogSession) e2).getDate().getTime();
					}
					if (date1 == date2) {
						int result = elements.indexOf(e2) - elements.indexOf(e1);
						if (DATE_ORDER == DESCENDING)
							result *= DESCENDING;
						return result;
					}
					if (DATE_ORDER == DESCENDING)
						return date1 > date2 ? DESCENDING : ASCENDING;
					return date1 < date2 ? DESCENDING : ASCENDING;
				}
			};
		} else {
			fComparator = new Comparator() {
				public int compare(Object e1, Object e2) {
					if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
						LogEntry entry1 = (LogEntry) e1;
						LogEntry entry2 = (LogEntry) e2;
						return Policy.getComparator().compare(entry1.getMessage(), entry2.getMessage()) * MESSAGE_ORDER;
					}
					return 0;
				}
			};
		}
	}
	
	private ViewerComparator getViewerComparator(byte sortType) {
		if (sortType == MESSAGE) {
			return new ViewerComparator() {
				public int compare(Viewer viewer, Object e1, Object e2) {
					if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
						LogEntry entry1 = (LogEntry) e1;
						LogEntry entry2 = (LogEntry) e2;
						return getComparator().compare(entry1.getMessage(), entry2.getMessage()) * MESSAGE_ORDER;
					}
					return 0;
				}
			};
		} else {
			return new ViewerComparator() {
				public int compare(Viewer viewer, Object e1, Object e2) {
					long date1 = 0;
					long date2 = 0;
					if ((e1 instanceof LogEntry) && (e2 instanceof LogEntry)) {
						date1 = ((LogEntry) e1).getDate().getTime();
						date2 = ((LogEntry) e2).getDate().getTime();
					} else if ((e1 instanceof LogSession) && (e2 instanceof LogSession)) {
						date1 = ((LogSession) e1).getDate() == null ? 0 : ((LogSession) e1).getDate().getTime();
						date2 = ((LogSession) e2).getDate() == null ? 0 : ((LogSession) e2).getDate().getTime();
					}

					if (date1 == date2) {
						int result = elements.indexOf(e2) - elements.indexOf(e1);
						if (DATE_ORDER == DESCENDING)
							result *= DESCENDING;
						return result;
					}
					if (DATE_ORDER == DESCENDING)
						return date1 > date2 ? DESCENDING : ASCENDING;
					return date1 < date2 ? DESCENDING : ASCENDING;
				}
			};
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if( selection != null && selection instanceof IStructuredSelection ) {
			IStructuredSelection sel2 = (IStructuredSelection)selection;
			if( sel2.size() > 0 ) {
				Object o = sel2.getFirstElement();
				if( o instanceof IServer ) {
					setServer((IServer)o);
				}
			}
		}
	}
	
}
