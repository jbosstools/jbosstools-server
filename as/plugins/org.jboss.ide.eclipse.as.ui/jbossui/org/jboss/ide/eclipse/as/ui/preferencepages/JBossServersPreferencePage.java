/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.preferencepages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.attributes.IServerPollingAttributes;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.Messages;


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
	
	private Button abortOnTimeout, ignoreOnTimeout;
	
	private HashMap workingCoppies;	
	
		
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
				return ServerConverter.getAllJBossServers();
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
		timeoutGroup.setText(Messages.PreferencePageServerTimeouts);
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
		startTimeoutLabel.setText(Messages.PreferencePageStartTimeouts);
		
		
		FormData stopTD = new FormData();
		stopTD.left = new FormAttachment(0,5);
		stopTD.top = new FormAttachment(startSpinner,4);
		stopTimeoutLabel.setLayoutData(stopTD);
		stopTimeoutLabel.setText(Messages.PreferencePageStopTimeouts);
		
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
		stopSpinner.setIncrement(1);
		startSpinner.setIncrement(1);
		
		Label uponTimeoutLabel = new Label(timeoutGroup, SWT.NONE);
		abortOnTimeout = new Button(timeoutGroup, SWT.RADIO);
		ignoreOnTimeout = new Button(timeoutGroup, SWT.RADIO);
		
		FormData utl = new FormData();
		utl.left = new FormAttachment(0,5);
		utl.right = new FormAttachment(100, -5);
		utl.top = new FormAttachment(stopSpinner,5);
		uponTimeoutLabel.setLayoutData(utl);

		FormData b1D = new FormData();
		b1D.left = new FormAttachment(0,15);
		b1D.right = new FormAttachment(100, -5);
		b1D.top = new FormAttachment(uponTimeoutLabel,5);
		abortOnTimeout.setLayoutData(b1D);
		
		FormData b2D = new FormData();
		b2D.left = new FormAttachment(0,15);
		b2D.right = new FormAttachment(100, -5);
		b2D.top = new FormAttachment(abortOnTimeout,5);
		ignoreOnTimeout.setLayoutData(b2D);
		
		uponTimeoutLabel.setText(Messages.PreferencePageUponTimeout);
		abortOnTimeout.setText(Messages.PreferencePageUponTimeoutAbort);
		ignoreOnTimeout.setText(Messages.PreferencePageUponTimeoutIgnore);

		
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
				getSelectedWC().setAttribute(IServerPollingAttributes.START_TIMEOUT, startSpinner.getSelection() * 1000);
			} 
		});
		stopSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getSelectedWC().setAttribute(IServerPollingAttributes.STOP_TIMEOUT, stopSpinner.getSelection() * 1000);
			} 
		});
		
		abortOnTimeout.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if( getSelectedWC() != null ) 
					getSelectedWC().setAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, IServerPollingAttributes.TIMEOUT_ABORT);
			} 
		});
		ignoreOnTimeout.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if( getSelectedWC() != null ) 
					getSelectedWC().setAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, IServerPollingAttributes.TIMEOUT_IGNORE);
			} 
		});
				
	}
	
	private void serverSelected(JBossServer server) {
		currentServer = server;
		ServerAttributeHelper wcHelper = getWCHelper(server);
		
		/* Handle spinners */
		startSpinner.setMaximum(((ServerType)server.getServer().getServerType()).getStartTimeout() / 1000);
		stopSpinner.setMaximum(((ServerType)server.getServer().getServerType()).getStopTimeout() / 1000);
		startSpinner.setSelection(getStartTimeout(wcHelper));
		stopSpinner.setSelection(getStopTimeout(wcHelper));
		
		boolean currentVal = wcHelper.getAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, IServerPollingAttributes.TIMEOUT_IGNORE);
		if( currentVal == IServerPollingAttributes.TIMEOUT_ABORT) {
			abortOnTimeout.setSelection(true);
			ignoreOnTimeout.setSelection(false);
		} else {
			abortOnTimeout.setSelection(false);
			ignoreOnTimeout.setSelection(true);
		}
	}
	
	public int getStartTimeout(ServerAttributeHelper helper) {
		int prop = helper.getAttribute(IServerPollingAttributes.START_TIMEOUT, -1);
		int max = ((ServerType)helper.getServer().getServerType()).getStartTimeout();
		
		if( prop <= 0 || prop > max ) return max / 1000;
		return prop / 1000;
	}
	public int getStopTimeout(ServerAttributeHelper helper) {
		int prop = helper.getAttribute(IServerPollingAttributes.STOP_TIMEOUT, -1);
		int max = ((ServerType)helper.getServer().getServerType()).getStopTimeout();
		
		if( prop <= 0 || prop > max ) return max / 1000;
		return prop / 1000;
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
    			ServerAttributeHelper o2 = (ServerAttributeHelper)o;
    			if( o2.isDirty() ) {
    				try {
    					o2.save(true, new NullProgressMonitor());
    				} catch( CoreException ce) {
    					ce.printStackTrace();
    				}
    			}
    		}
    	}
	}
	
    public boolean performCancel() {
    	return super.performCancel();
    }
}
