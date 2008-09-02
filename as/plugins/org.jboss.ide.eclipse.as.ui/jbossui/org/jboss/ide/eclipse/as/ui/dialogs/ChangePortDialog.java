package org.jboss.ide.eclipse.as.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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

public class ChangePortDialog extends TitleAreaDialog {

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
	private Group group;
	private Label nameLabel; private Text nameValueLabel;
	private Label xpathLabel; private Text xpathValueLabel;
	private Label attributeLabel; private Text attributeValueLabel;
	private Label locationLabel; private Text locationValueLabel;
	private Label countLabel; private Text countValueLabel;
	private Label valueLabel; private Text valueValueLabel;

	
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
		
		Label defaultLabel = new Label(main, SWT.NONE);
		d = new FormData();
		d.right = new FormAttachment(100,-5);
		d.left = new FormAttachment(listWidget,5);
		d.bottom = new FormAttachment(100,-5);
		defaultLabel.setLayoutData(d);
		
		group = new Group(main, SWT.DEFAULT);
		d = new FormData();
		d.right = new FormAttachment(100,-5);
		d.left = new FormAttachment(listWidget,5);
		d.bottom = new FormAttachment(defaultLabel,-5);
		d.top = new FormAttachment(0,5);
		group.setLayoutData(d);

		GridLayout gl = new GridLayout(2, false);
		gl.marginRight = 5;
		group.setLayout(gl);
		GridData common = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		nameLabel = new Label(group, SWT.NONE);
		nameValueLabel = new Text(group, SWT.DEFAULT | SWT.H_SCROLL);
		xpathLabel = new Label(group, SWT.NONE);
		xpathValueLabel = new Text(group, SWT.DEFAULT | SWT.H_SCROLL);
		attributeLabel = new Label(group, SWT.NONE);
		attributeValueLabel = new Text(group, SWT.DEFAULT | SWT.H_SCROLL);
		countLabel = new Label(group, SWT.NONE);
		countValueLabel = new Text(group, SWT.DEFAULT | SWT.H_SCROLL);
		valueLabel = new Label(group, SWT.NONE);
		valueValueLabel = new Text(group, SWT.DEFAULT | SWT.H_SCROLL);
		locationLabel = new Label(group, SWT.NONE);
		locationValueLabel = new Text(group, SWT.DEFAULT | SWT.H_SCROLL);
		
		nameValueLabel.setLayoutData(common);
		xpathValueLabel.setLayoutData(common);
		attributeValueLabel.setLayoutData(common);
		countValueLabel.setLayoutData(common);
		valueValueLabel.setLayoutData(common);
		locationValueLabel.setLayoutData(common);
		
		nameLabel.setText(Messages.EditorCPD_Name);
		xpathLabel.setText(Messages.EditorCPD_XPath);
		attributeLabel.setText(Messages.EditorCPD_Attribute);
		countLabel.setText(Messages.EditorCPD_Count);
		valueLabel.setText(Messages.EditorCPD_Value);
		locationLabel.setText(Messages.EditorCPD_Location);
		String defaultLabelText;
		if( info.defaultValue != null ) 
			defaultLabelText = NLS.bind(Messages.EditorCPD_Default, info.port, info.defaultValue);
		else
			defaultLabelText = NLS.bind(Messages.EditorCPD_NoDefault, info.port);
		defaultLabel.setText(defaultLabelText);
		group.setText(Messages.EditorCPD_SelectionDetails);
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
			if( stuff[i] == info.currentXPath)
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
	}
	
	protected synchronized void selectionChanged() {
		if( !queriesLoaded ) { 
			try { 
				final String[] items = listWidget.getItems();
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				dialog.run(true, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Loading...", items.length);
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
			nameValueLabel.setText(safeString(currentQuery.getName()));
			xpathValueLabel.setText(safeString(currentQuery.getXpathPattern()));
			attributeValueLabel.setText(safeString(currentQuery.getAttribute()));
			locationValueLabel.setText(safeString(currentQuery.getBaseDir()));
			countValueLabel.setText(countMatches(currentQuery));
			valueValueLabel.setText(safeString(currentQuery.getFirstResult()));
		} else {
			nameValueLabel.setText(safeString(null));
			xpathValueLabel.setText(safeString(null));
			attributeValueLabel.setText(safeString(null));
			locationValueLabel.setText(safeString(null));
			countValueLabel.setText(safeString(null));
			valueValueLabel.setText(safeString(null));
		}
	}
	
	private String countMatches(XPathQuery query) {
		if( query == null ) 
			return new Integer(-1).toString();
		
		int count = 0;
		XPathFileResult[] fResults = query.getResults();
		for( int i = 0; i < fResults.length; i++ ) {
			count += fResults[i].getChildren().length;
		}
		return new Integer(count).toString();
	}
	private String safeString(String s) {
		return s == null ? "" : s;
	}
	
	public String getSelection() {
		return selected;
	}
}
