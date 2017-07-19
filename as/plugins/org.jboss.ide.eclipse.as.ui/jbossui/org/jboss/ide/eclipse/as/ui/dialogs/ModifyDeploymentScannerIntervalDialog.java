/******************************************************************************* 
* Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.AS7DeploymentScannerUtility;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.osgi.service.prefs.BackingStoreException;

public class ModifyDeploymentScannerIntervalDialog extends TitleAreaDialog {
	
	public static final String AS7_IGNORE_ZERO_INTERVAL_SCANNER_SETTING = "AS7_IGNORE_ZERO_INTERVAL_SCANNER_SETTING"; //$NON-NLS-1$
	
	public static class DeploymentScannerUIServerStartedListener extends UnitedServerListener {
		public void serverChanged(ServerEvent event) {
			IServer s = event.getServer();
			JBossExtendedProperties props = (JBossExtendedProperties)
					s.loadAdapter(JBossExtendedProperties.class, null);
			if( props != null && props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT) {
				if( serverSwitchesToState(event, IServer.STATE_STARTED)) {
					// Don't do any potentially long-running tasks here. 
					launchJob(s);
				}
			}
		}
		
		private void launchJob(final IServer server) {
			new Job(Messages.DeploymentScannerCheckJobTitle) {
				protected IStatus run(IProgressMonitor monitor) {
					IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
					boolean ignore = prefs.getBoolean(AS7_IGNORE_ZERO_INTERVAL_SCANNER_SETTING, false);
					if( !ignore ) {
						final AS7DeploymentScannerUtility.Scanner[] scanners =
								new AS7DeploymentScannerUtility().getDeploymentScannersBlocking(server, true);
						if( scanners != null && hasScannersAtZero(server,scanners)) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									// NOW launch the dialog
									launchDialog(server, scanners);
								}
							});
						}
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
		
		private void launchDialog(final IServer server, AS7DeploymentScannerUtility.Scanner[] scanners) {
			ModifyDeploymentScannerIntervalDialog d = 
					new ModifyDeploymentScannerIntervalDialog(
							server, scanners,
							Display.getDefault().getActiveShell());
			int ret = d.open();
			
			final AS7DeploymentScannerUtility.Scanner[] changedArray = d.getChangedScanners();
			boolean neverAskAgainSetting = d.getAskAgainSelection();
			
			if( neverAskAgainSetting ) {
				IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
				prefs.putBoolean(AS7_IGNORE_ZERO_INTERVAL_SCANNER_SETTING, true);
				try {
					prefs.flush();
				} catch(BackingStoreException e) {
				}
			}
			
			if( ret == Window.OK && changedArray.length > 0 ) {
				new Job(Messages.DeploymentScannerUpdateJobTitle) {
					protected IStatus run(IProgressMonitor monitor) {
						return updateServersScanners(server, changedArray);
					}
				}.schedule();
			}
		}
		
		private IStatus updateServersScanners(IServer server, AS7DeploymentScannerUtility.Scanner[] changed) {
			AS7DeploymentScannerUtility util = new AS7DeploymentScannerUtility();
			for( int i = 0; i < changed.length; i++ ) {
				util.updateDeploymentScannerInterval(server, changed[i].getName(), changed[i].getInterval());
			}
			return Status.OK_STATUS;
		}
		
		private boolean hasScannersAtZero(IServer server, AS7DeploymentScannerUtility.Scanner[] scanners ) {
			// check if any have 0
			for( int i = 0; i < scanners.length; i++ ) {
				if( scanners[i].getInterval() <= 0 )
					return true;
			}
			return false;
		}
	}
	
	private IServer server;
	private AS7DeploymentScannerUtility.Scanner[] scanners;
	private Label extraInfo;
	private TableViewer tv;
	private boolean askAgainSelected = false;
    private String[] headings = new String[]{
    		Messages.DeploymentScannerColumnName, 
    		Messages.DeploymentScannerColumnPath, 
    		Messages.DeploymentScannerColumnInterval
    };
    private ArrayList<AS7DeploymentScannerUtility.Scanner> changed = new ArrayList<AS7DeploymentScannerUtility.Scanner>();
	public ModifyDeploymentScannerIntervalDialog(
			IServer server, AS7DeploymentScannerUtility.Scanner[] scanners, Shell parentShell) {
		super(parentShell);
		this.scanners = scanners;
		this.server = server;
	}
		
	public AS7DeploymentScannerUtility.Scanner[] getChangedScanners() {
		return (AS7DeploymentScannerUtility.Scanner[]) changed.toArray(new AS7DeploymentScannerUtility.Scanner[changed.size()]);
	}
	
	public boolean getAskAgainSelection() {
		return askAgainSelected;
	}
	
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		setMessage(NLS.bind(Messages.DeploymentScannerDialogWarning, server.getName()), IMessageProvider.WARNING);
		setTitle(Messages.DeploymentScannerDialogTitle);
		getShell().setText(Messages.DeploymentScannerDialogTitle);
		getShell().setSize(500,400);
		return c;
	}

	protected boolean isResizable() {
	    return true;
	}

	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite((Composite)super.createDialogArea(parent), SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(1,false));
		extraInfo = new Label(main, SWT.WRAP);
		extraInfo.setText(Messages.DeploymentScannerDialogWarningExplained);
		
		
	    // Add the TableViewer
	    tv = new TableViewer(main, SWT.FULL_SELECTION);
	    tv.setContentProvider(new ScannerContentProvider());
	    tv.setLabelProvider(new ScannerLabelProvider());

	    // Set up the table
	    Table table = tv.getTable();
	    table.setLayoutData(new GridData(GridData.FILL_BOTH));

	    TableColumn tc1 = new TableColumn(table, SWT.CENTER);
	    TableColumn tc2 = new TableColumn(table, SWT.CENTER);
	    TableColumn tc3 = new TableColumn(table, SWT.CENTER);

	    for (int i = 0, n = table.getColumnCount(); i < n; i++) {
	    	table.getColumn(i).setText(headings[i]);
	    	table.getColumn(i).pack();
	    }
	    tc1.setWidth(150);
	    tc2.setWidth(180);
	    tc3.setWidth(70);
	    
	    table.setHeaderVisible(true);
	    table.setLinesVisible(true);

	    // Create the cell editors
	    CellEditor[] editors = new CellEditor[4];
	    editors[0] = null;
	    editors[1] = null;
	    editors[2] = new TextCellEditor(table);

	    // Set the editors, cell modifier, and column properties
	    tv.setColumnProperties(headings);
	    tv.setCellModifier(new ScannerCellModifier());
	    tv.setCellEditors(editors);

	    tv.setInput(scanners);
	    
	    final Button askAgain = new Button(main, SWT.CHECK);
	    askAgain.setText("Don't ask this again."); //$NON-NLS-1$
	    askAgain.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				askAgainSelected = askAgain.getSelection();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				askAgainSelected = askAgain.getSelection();
			}
		});
		return main;
	}
	
	private class ScannerContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			return scanners;
		}
	}
	
	private class ScannerLabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if( columnIndex == 0 )
				return ((AS7DeploymentScannerUtility.Scanner)element).getName();
			if( columnIndex == 1 )
				return ((AS7DeploymentScannerUtility.Scanner)element).getAddress();
			if( columnIndex == 2 )
				return "" + ((AS7DeploymentScannerUtility.Scanner)element).getInterval(); //$NON-NLS-1$
			return null;
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
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	private class ScannerCellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			return property.equals(headings[2]);
		}
		public Object getValue(Object element, String property) {
			int i = ((AS7DeploymentScannerUtility.Scanner)element).getInterval();
			return new Integer(i).toString();
		}
		public void modify(Object element, String property, Object value) {
			String element2 = ((TableItem)element).getText();
			AS7DeploymentScannerUtility.Scanner e = null;
			for( int i = 0; i < scanners.length; i++ ) {
				if( scanners[i].getName().equals(element2)) {
					e = scanners[i];
					break;
				}
			}
			if( !value.toString().equals(e.getInterval())) {
				Integer i = null;
				try {
					i = Integer.parseInt(value.toString());
				} catch(NumberFormatException nfe) {
					// Ignore
				}
				if( i != null ) {
					e.setInterval(i);
					if( !changed.contains(e))
						changed.add(e);
					tv.refresh();
				}
			}
		}
	}
}
