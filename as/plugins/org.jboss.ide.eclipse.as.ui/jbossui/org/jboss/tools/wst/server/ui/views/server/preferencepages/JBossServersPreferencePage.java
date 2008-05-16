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
package org.jboss.tools.wst.server.ui.views.server.preferencepages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IServerPollingAttributes;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossServersPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	// for the main section
	private JBossServer currentServer;
	private Table serverTable;
	private TableViewer serverTableViewer;
	private HashMap<JBossServer, ServerAttributeHelper> workingCoppies;	
	private Group serverGroup, secondGroup;
	private PageBook book;
	private ServerPreferenceProvider[] groups;
	int pageColumn = 55;

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
		createSecondGroup(main);
		addListeners();
		return main;
	}
	
	
	protected void createServerViewer(Composite main) {
		
		serverGroup = new Group(main, SWT.NONE);
		FillLayout serverGroupLayout = new FillLayout();
		serverGroupLayout.marginHeight = 5;
		serverGroupLayout.marginWidth = 5;
		serverGroup.setLayout(serverGroupLayout);
		serverGroup.setText("Servers");
		
		workingCoppies = new HashMap<JBossServer, ServerAttributeHelper>();
		
		serverTable = new Table(serverGroup, SWT.BORDER);
		FormData lData = new FormData();
		lData.left = new FormAttachment(0,5);
		lData.right = new FormAttachment(100,-5);
		lData.top = new FormAttachment(0,5);
		lData.bottom = new FormAttachment(30,-5);
		serverGroup.setLayoutData(lData);
		
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

	protected void createSecondGroup(Composite main) {
		secondGroup = new Group(main, SWT.NONE);
		
		FormData lData = new FormData();
		lData.left = new FormAttachment(0,5);
		lData.right = new FormAttachment(100,-5);
		lData.top = new FormAttachment(serverGroup,5);
		lData.bottom = new FormAttachment(100,-5);
		secondGroup.setLayoutData(lData);

		
		
		secondGroup.setLayout(new FillLayout());
		book = new PageBook(secondGroup, SWT.NONE);
		groups = new ServerPreferenceProvider[] { 
				new TimeoutComposite(book)
		};
		book.showPage(groups[0]);
		secondGroup.setText(groups[0].getName());
	}
	
	
	
	private void addListeners() {
		serverTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)serverTableViewer.getSelection();
				serverSelected(sel.getFirstElement() == null ? null : (JBossServer)sel.getFirstElement());
			}
		});
	}

	private void serverSelected(JBossServer server) {
		currentServer = server;
		ServerAttributeHelper wcHelper = getWCHelper(server);
		for( int i = 0; i < groups.length; i++ ) {
			try {
				groups[i].serverSelected(server, wcHelper);
			} catch( Exception e ) {}
		}
	}

	
	private ServerAttributeHelper getWCHelper(JBossServer server) {
		if( workingCoppies.get(server) == null ) {
			ServerAttributeHelper ret = server.getAttributeHelper();
			workingCoppies.put(server, ret);
			return ret;
		}
		
		return workingCoppies.get(server);
	}
	
	private ServerAttributeHelper getSelectedWC() {
		return currentServer == null ? null : getWCHelper(currentServer);
	}
	
	public void init(IWorkbench workbench) {
	}
		
    public boolean performOk() {
    	super.performOk();
    	saveDirtyWorkingCoppies();
    	return true;
    }
    
    /* Saves the actual ServerWorkingCopy changes into the IServer it relates to. */
	private void saveDirtyWorkingCoppies() {
    	Collection<ServerAttributeHelper> c = workingCoppies.values();
    	Iterator<ServerAttributeHelper> i = c.iterator();
    	Object o;
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
	
    public static abstract class ServerPreferenceProvider extends Composite {
    	protected String name;
    	public ServerPreferenceProvider(Composite parent, int style, 
    					String name) {
    		super(parent, style);
    		this.name = name;
    	}
    	public String getName() { return name; }
    	public abstract void serverSelected(JBossServer server, ServerAttributeHelper helper);
    }
    
	public static class TimeoutComposite extends ServerPreferenceProvider {

		private Spinner stopSpinner, startSpinner;
		private Button abortOnTimeout, ignoreOnTimeout;
		private JBossServer server;
		private ServerAttributeHelper helper;
		private Composite durations, behavior, pollers;
		
		// polling
		private Combo startPollerCombo, stopPollerCombo;
		private String[] startupTypesStrings, shutdownTypesStrings;
		ServerStatePollerType[] startupTypes, shutdownTypes;

		public TimeoutComposite(Composite parent) {
			super(parent, SWT.NONE, Messages.PreferencePageServerTimeouts);
			
			findPossiblePollers();
			
			setLayout(new FormLayout());
			createTimeoutDurations();
			createTimeoutBehavior();
			createPollerChoices();
			
			durations.setLayoutData(createLayoutData(null));
			behavior.setLayoutData(createLayoutData(durations));
			pollers.setLayoutData(createLayoutData(behavior));
			addTimeoutListeners();
		}

		protected void findPossiblePollers() {
			startupTypes = ExtensionManager.getDefault().getStartupPollers();
			shutdownTypes = ExtensionManager.getDefault().getShutdownPollers();
			startupTypesStrings = new String[startupTypes.length];
			shutdownTypesStrings = new String[shutdownTypes.length];
			
			for( int i = 0; i < startupTypes.length; i++ ) {
				startupTypesStrings[i] = startupTypes[i].getName();
			}
			for( int i = 0; i < shutdownTypes.length; i++ ) {
				shutdownTypesStrings[i] = shutdownTypes[i].getName();
			}
		}
		
		private FormData createLayoutData(Composite top) {
			FormData data = new FormData();
			if( top == null ) 	data.top = new FormAttachment(0,5);
			else 				data.top = new FormAttachment(top, 5);
			data.left = new FormAttachment(0,5);
			data.right = new FormAttachment(100,-5);
			return data;
		}
		public void serverSelected(JBossServer server,
				ServerAttributeHelper helper) {
			this.server = server;
			this.helper = helper;
			timeoutServerSelected();
		}
		
		protected void createTimeoutDurations() {
			durations = new Composite(this, SWT.NONE);
			durations.setLayout(new FormLayout());
			
			// add two textboxes, two labels
			Label startTimeoutLabel, stopTimeoutLabel;
			
			startTimeoutLabel = new Label(durations, SWT.NONE);
			stopTimeoutLabel = new Label(durations, SWT.NONE);
			
			stopSpinner = new Spinner(durations, SWT.BORDER);
			startSpinner = new Spinner(durations, SWT.BORDER);
			
			FormData startTD = new FormData();
			startTD.left = new FormAttachment(0,5);
			startTD.top = new FormAttachment(0,7);
			startTimeoutLabel.setLayoutData(startTD);
			startTimeoutLabel.setText(Messages.PreferencePageStartTimeouts);
			
			FormData stopTD = new FormData();
			stopTD.left = new FormAttachment(0,5);
			stopTD.top = new FormAttachment(startSpinner,7);
			stopTimeoutLabel.setLayoutData(stopTD);
			stopTimeoutLabel.setText(Messages.PreferencePageStopTimeouts);
			
			durations.layout();
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
			stopSpinner.setEnabled(false);
			startSpinner.setEnabled(false);
			
		}
		
		protected void createTimeoutBehavior() {
			behavior = new Composite(this, SWT.NONE);
			behavior.setLayout(new FormLayout());
			
			Label uponTimeoutLabel = new Label(behavior, SWT.NONE);
			abortOnTimeout = new Button(behavior, SWT.RADIO);
			ignoreOnTimeout = new Button(behavior, SWT.RADIO);
			
			FormData utl = new FormData();
			utl.left = new FormAttachment(0,5);
			utl.right = new FormAttachment(100, -5);
			utl.top = new FormAttachment(0,5);
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
			abortOnTimeout.setEnabled(false);
			ignoreOnTimeout.setEnabled(false);
			
		}
		
		protected void createPollerChoices() {
			pollers = new Composite(this, SWT.NONE);
			pollers.setLayout(new GridLayout(2, false));
			
			// create widgets
			Label start, stop;
			start = new Label(pollers, SWT.NONE);
			startPollerCombo = new Combo(pollers, SWT.READ_ONLY);
			stop = new Label(pollers, SWT.NONE);
			stopPollerCombo = new Combo(pollers, SWT.READ_ONLY);
			
			start.setText("Startup Poller");
			stop.setText("Shutdown Poller");
			
			// set items
			startPollerCombo.setItems(startupTypesStrings);
			stopPollerCombo.setItems(shutdownTypesStrings);
			
			startPollerCombo.setEnabled(false);
			stopPollerCombo.setEnabled(false);
		}
		
		private void addTimeoutListeners() {
			startSpinner.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if( helper != null )
						helper.setAttribute(IServerPollingAttributes.START_TIMEOUT, startSpinner.getSelection() * 1000);
				} 
			});
			stopSpinner.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if( helper != null )
						helper.setAttribute(IServerPollingAttributes.STOP_TIMEOUT, stopSpinner.getSelection() * 1000);
				} 
			});
			
			abortOnTimeout.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					if( helper != null )
						helper.setAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, IServerPollingAttributes.TIMEOUT_ABORT);
				} 
			});
			ignoreOnTimeout.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					if( helper != null )
						helper.setAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, IServerPollingAttributes.TIMEOUT_IGNORE);
				} 
			});
			startPollerCombo.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					if( helper != null ) 
						helper.setAttribute(IServerPollingAttributes.STARTUP_POLLER_KEY, startupTypes[startPollerCombo.getSelectionIndex()].getId());
				} 
			});
			stopPollerCombo.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					if( helper != null ) 
						helper.setAttribute(IServerPollingAttributes.SHUTDOWN_POLLER_KEY, shutdownTypes[stopPollerCombo.getSelectionIndex()].getId());
				}
			});
		}
			
		private void timeoutServerSelected() {
			// Handle spinners 
			startSpinner.setMaximum(((ServerType)server.getServer().getServerType()).getStartTimeout() / 1000);
			stopSpinner.setMaximum(((ServerType)server.getServer().getServerType()).getStopTimeout() / 1000);
			startSpinner.setSelection(getStartTimeout(helper));
			stopSpinner.setSelection(getStopTimeout(helper));
			
			startSpinner.setEnabled(true);
			stopSpinner.setEnabled(true);
			abortOnTimeout.setEnabled(true);
			ignoreOnTimeout.setEnabled(true);
			
			boolean currentVal = helper.getAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, IServerPollingAttributes.TIMEOUT_IGNORE);
			if( currentVal == IServerPollingAttributes.TIMEOUT_ABORT) {
				abortOnTimeout.setSelection(true);
				ignoreOnTimeout.setSelection(false);
			} else {
				abortOnTimeout.setSelection(false);
				ignoreOnTimeout.setSelection(true);
			}
			
			// poller
			stopPollerCombo.setEnabled(true);
			startPollerCombo.setEnabled(true);
			String currentStartId = helper.getAttribute(IServerPollingAttributes.STARTUP_POLLER_KEY, IServerPollingAttributes.DEFAULT_STARTUP_POLLER);
			String currentStopId = helper.getAttribute(IServerPollingAttributes.SHUTDOWN_POLLER_KEY, IServerPollingAttributes.DEFAULT_SHUTDOWN_POLLER);
			startPollerCombo.select(startPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStartId).getName()));
			stopPollerCombo.select(stopPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStopId).getName()));
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
	}
}
