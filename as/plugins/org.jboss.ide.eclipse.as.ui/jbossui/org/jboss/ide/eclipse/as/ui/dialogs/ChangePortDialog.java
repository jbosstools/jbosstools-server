package org.jboss.ide.eclipse.as.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathDialog;

public class ChangePortDialog extends TitleAreaDialog {
	private static final int RESTORE_DEFAULT = 35;
	public static class ChangePortDialogInfo {
		public String port;
		public String defaultValue;
		public String shellTitle;
		public String description;
		public IServer server;
		public String currentXPath;
	}

	private ChangePortDialogInfo info;
	private String selected;
	private XPathQuery currentQuery;
	private boolean queriesLoaded = false;
	private List listWidget;
	private Label currentValue;
	private Text currentValueText;
	private Button editXPathButton;

	public ChangePortDialog(Shell parentShell, ChangePortDialogInfo info) {
		super(parentShell);
		this.info = info;
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());
		createUI(main);
		fillWidgets();
		addListeners();
		setTitle(info.port);
		setMessage(info.description != null ? info.description
				: Messages.EditorCPD_DefaultDescription);
		getShell().setText(info.shellTitle != null ? info.shellTitle
				: Messages.EditorCPD_DefaultShellTitle);
		selectionChanged();
		return c;
	}

	protected void createUI(Composite main) {
		listWidget = new List(main, SWT.DEFAULT);
		FormData d = new FormData();
		d.top = new FormAttachment(0,5);
		d.left = new FormAttachment(0,5);
		d.bottom = new FormAttachment(100,-5);
		d.right = new FormAttachment(0,150);
		listWidget.setLayoutData(d);

		currentValue = new Label(main, SWT.NONE);
		d = new FormData();
		d.left = new FormAttachment(listWidget, 5);
		d.top = new FormAttachment(0,7);
		currentValue.setLayoutData(d);
		currentValue.setText(Messages.EditorCPD_Value);

		editXPathButton = new Button(main, SWT.NONE);
		d = new FormData();
		d.right = new FormAttachment(100, -5);
		d.top = new FormAttachment(currentValue, 5);
		editXPathButton.setLayoutData(d);
		editXPathButton.setText(Messages.DescriptorXPathEditXPath);

		currentValueText = new Text(main, SWT.DEFAULT);
		d = new FormData();
		d.left = new FormAttachment(listWidget, 5);
		d.right = new FormAttachment(editXPathButton,-5);
		d.top = new FormAttachment(currentValue,5);
		currentValueText.setLayoutData(d);
		currentValueText.setEnabled(false);
	}

	protected void fillWidgets() {
		ArrayList<String> list = new ArrayList<String>();
		XPathCategory[] categories = XPathModel.getDefault().getCategories(info.server);
		for( int i = 0; i < categories.length; i++ ) {
			XPathQuery[] queries = categories[i].getQueries();
			for( int j = 0; j < queries.length; j++ ) {
				list.add(categories[i].getName() + IPath.SEPARATOR + queries[j].getName());
			}
		}
		String[] stuff = (String[]) list.toArray(new String[list.size()]);
		listWidget.setItems(stuff);
		for( int i = 0; i < stuff.length; i++ )
			if( stuff[i].equals(info.currentXPath))
				listWidget.select(i);
	}

	protected void addListeners() {
		listWidget.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				selectionChanged();
			} });

		editXPathButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				editPressed();
			} });

	}

	protected void editPressed() {
		if( currentQuery != null ) {
			XPathCategory category = currentQuery.getCategory();

			XPathDialog d = new XPathDialog(Display.getCurrent().getActiveShell(),
					info.server, currentQuery);
			if( d.open() == Window.OK ) {
				currentQuery.setAttribute(d.getAttribute());
				currentQuery.setXpathPattern(d.getXpath());
				currentQuery.setName(d.getName());
				category.save();
				fillWidgets();
			}
		}
	}

	protected synchronized void selectionChanged() {
		if( !queriesLoaded ) {
			try {
				final String[] items = listWidget.getItems();
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				dialog.run(true, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.ChangePortDialog_LoadingTaskName, items.length);
						for( int i = 0; i < items.length; i++ ) {
							countMatches(currentQuery);
							monitor.worked(1);
						}
						monitor.done();
						queriesLoaded = true;
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								selectionChanged2();
							}
						});
					} });
			} catch( InvocationTargetException ite) {
				ite.printStackTrace();
			} catch(InterruptedException ie ) {
				ie.printStackTrace();
			}
		} else {
			selectionChanged2();
		}
	}

	private void selectionChanged2() {
		currentQuery = null;
		int s = listWidget.getSelectionIndex();
		if( s != -1 ) {
			selected = listWidget.getItem(s);
			currentQuery = XPathModel.getDefault().getQuery(info.server, new Path(selected));
		}
		if(currentQuery != null) {
			editXPathButton.setEnabled(true);
			currentValueText.setText(currentQuery.getFirstResult());
		} else {
		}
	}

	private String countMatches(XPathQuery query) {
		if( query == null )
			return String.valueOf(-1);

		int count = 0;
		XPathFileResult[] fResults = query.getResults();
		for( int i = 0; i < fResults.length; i++ ) {
			count += fResults[i].getChildren().length;
		}
		return String.valueOf(count);
	}
	
	private String safeString(String s) {
		return s == null ? "" : s; //$NON-NLS-1$
	}

	public String getSelection() {
		return selected;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, RESTORE_DEFAULT, Messages.EditorCPD_RestoreDefault, false);
	}
	
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if( RESTORE_DEFAULT == buttonId) {
			String[] items = listWidget.getItems();
			for( int i = 0; i < items.length; i++ )
				if( items[i].equals(info.defaultValue)) {
					listWidget.select(i);
					return;
				}
		}
	}
}
