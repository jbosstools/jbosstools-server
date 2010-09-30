/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvent;
import org.eclipse.rse.core.events.ISystemModelChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSection;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite.ChangeServerPropertyCommand;

public class RSEDeploymentPreferenceUI implements IDeploymentTypeUI {
	public RSEDeploymentPreferenceUI() {
		// Do nothing
	}

	@Override @Deprecated
	public void fillComposite(Composite parent, ServerModeSection modeSection) {
		return;
	}

	@Override 
	public void fillComposite(Composite parent, IServerModeUICallback callback) {
		parent.setLayout(new FillLayout());
		new RSEDeploymentPreferenceComposite(parent, SWT.NONE, callback);
	}
	
	public static class RSEDeploymentPreferenceComposite extends Composite implements PropertyChangeListener {
		private IServerModeUICallback callback;
		private CustomSystemHostCombo combo;
		private Text rseServerHome,rseServerConfig;
		private Button rseBrowse;
		private ModifyListener comboMListener;
		public RSEDeploymentPreferenceComposite(Composite parent, int style, IServerModeUICallback callback) {
			super(parent, style);
			this.callback = callback;
			setLayout(new FormLayout());
			Composite child = new Composite(this, SWT.None);
			child.setLayoutData(UIUtil.createFormData2(0, 0, null, 0, 0, 5, 100, 0));
			child.setLayout(new GridLayout());
			String current = callback.getServer().getAttribute(RSEUtils.RSE_SERVER_HOST, RSEUtils.RSE_SERVER_DEFAULT_HOST);
			combo = new CustomSystemHostCombo(child, SWT.NULL, current, "files"); //$NON-NLS-1$
			comboMListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					rseHostChanged();
				}
			};
			combo.getCombo().addModifyListener(comboMListener);
			
			Label serverHomeLabel = new Label(this, SWT.NONE);
			serverHomeLabel.setText("Remote Server Home: ");
			rseBrowse = new Button(this, SWT.DEFAULT);
			rseBrowse.setText("Browse...");
			rseBrowse.setLayoutData(UIUtil.createFormData2(child, 5, null, 0, null, 0, 100, -5));
			rseBrowse.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					browseClicked();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					browseClicked();
				}
			});
			rseServerHome = new Text(this, SWT.SINGLE | SWT.BORDER);
			serverHomeLabel.setLayoutData(UIUtil.createFormData2(child, 7, null, 0, 0, 10, null, 0));
			rseServerHome.setLayoutData(UIUtil.createFormData2(child, 5, null, 0, serverHomeLabel, 5, rseBrowse, -5));
			rseServerHome.setText(callback.getServer().getAttribute(RSEUtils.RSE_SERVER_HOME_DIR, 
					getRuntime().getRuntime().getLocation().toString()));
			rseServerHome.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					serverHomeChanged();
				}});
			
			Label serverConfigLabel = new Label(this, SWT.NONE);
			serverConfigLabel.setText("Remote Server Configuration: ");
			rseServerConfig= new Text(this, SWT.SINGLE | SWT.BORDER);
			serverConfigLabel.setLayoutData(UIUtil.createFormData2(rseServerHome, 7, null, 0, 0, 10, null, 0));
			rseServerConfig.setLayoutData(UIUtil.createFormData2(rseServerHome, 5, null, 0, serverConfigLabel, 5, 100, -5));
			rseServerConfig.setText(callback.getServer().getAttribute(RSEUtils.RSE_SERVER_CONFIG, 
					getRuntime().getJBossConfiguration()));
			rseServerConfig.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					serverConfigChanged();
				}});
			callback.getServer().addPropertyChangeListener(this);
		}
		
		@Override
		public void dispose () {
			super.dispose();
			callback.getServer().removePropertyChangeListener(this);
		}

		private boolean updatingFromModelChange = false;
		public void propertyChange(PropertyChangeEvent evt) {
			updatingFromModelChange = true;
			if( evt.getPropertyName().equals(RSEUtils.RSE_SERVER_HOME_DIR)) {
				rseServerHome.setText(evt.getNewValue().toString());
			} else if( evt.getPropertyName().equals(RSEUtils.RSE_SERVER_CONFIG)) {
				rseServerConfig.setText(evt.getNewValue().toString());
			} else if( evt.getPropertyName().equals(RSEUtils.RSE_SERVER_HOST)) {
				combo.setHostName(evt.getNewValue().toString());
			}
			updatingFromModelChange = false;
		}

		protected void browseClicked() {
			SystemRemoteFileDialog d = new SystemRemoteFileDialog(
					rseBrowse.getShell(), "Browse remote system", combo.getHost());
			if( d.open() == Dialog.OK) {
				Object o = d.getOutputObject();
				if( o instanceof IRemoteFile ) {
					String path = ((IRemoteFile)o).getAbsolutePath();
					rseServerHome.setText(path);
					serverHomeChanged();
				}
			}
		}
		
		protected IJBossServerRuntime getRuntime() {
			IRuntime rt = callback.getRuntime();
			if( rt == null ) return null;
			return (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, null);
		}
		
		protected void rseHostChanged() {
			if( !updatingFromModelChange ) {
				String hostName = combo.getHost() == null ? null : combo.getHost().getAliasName();
				String oldVal = callback.getServer().getAttribute(RSEUtils.RSE_SERVER_HOST, (String)null);
				if( !hostName.equals(oldVal) && !updatingFromModelChange) {
					callback.execute(new ChangeServerPropertyCommand(
							callback.getServer(), RSEUtils.RSE_SERVER_HOST, hostName, "localhost", 
							"Change RSE Host"));
					callback.execute(new ChangeServerPropertyCommand(
							callback.getServer(), "hostname", combo.getHost().getHostName(), 
							"Change Hostname"));
				}
			}
		}
		
		protected void serverHomeChanged() {
			if( !updatingFromModelChange) {
				callback.execute(new ChangeServerPropertyCommand(
						callback.getServer(), RSEUtils.RSE_SERVER_HOME_DIR, rseServerHome.getText(), getRuntime().getRuntime().getLocation().toString(),
						"Change RSE Server's Home Directory"));
			}
		}

		protected void serverConfigChanged() {
			if( !updatingFromModelChange ) {
				callback.execute(new ChangeServerPropertyCommand(
						callback.getServer(), RSEUtils.RSE_SERVER_CONFIG, rseServerConfig.getText(), getRuntime().getJBossConfiguration(),
						"Change RSE Server's Configuration"));
			}
		}

		public class CustomSystemHostCombo extends Composite implements ModifyListener, ISystemModelChangeListener {
			private String fileSubSystem;
			private Combo combo;
			private Button newHost;
			private IHost currentHost;
			private String currentHostName;
			private IHost[] hosts;
			private String[] hostsAsStrings;
			public CustomSystemHostCombo(Composite parent, int style, String initialHostName, String fileSubSystem) {
				super(parent, style);
				this.fileSubSystem = fileSubSystem;
				this.currentHostName = initialHostName;
				this.hosts = RSECorePlugin.getTheSystemRegistry().getHostsBySubSystemConfigurationCategory(fileSubSystem);
				this.currentHost = findHost(initialHostName);
				RSECorePlugin.getTheSystemRegistry().addSystemModelChangeListener(this);			
				
				// Where I belong in the parent
				GridData data = new GridData();
				// horizontal clues
				data.horizontalAlignment = GridData.FILL;
			    data.grabExcessHorizontalSpace = true;        
		        data.widthHint =  200;
				// vertical clues
				data.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING; //GridData.CENTER;
			    data.grabExcessVerticalSpace = false; // true;        
				this.setLayoutData(data);
	
				// What's inside me
				setLayout(new FormLayout());
				Label l = new Label(this, SWT.NONE);
				l.setText("Host");
				newHost = new Button(this, SWT.DEFAULT);
				newHost.setText("New Host...");
				newHost.setLayoutData(UIUtil.createFormData2(0, 0, null, 0, null, 0, 100, -5));
				newHost.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						newHostClicked();
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
				
				combo = new Combo(this, SWT.DEFAULT | SWT.READ_ONLY);
				l.setLayoutData(UIUtil.createFormData2(0, 5, null, 0, 0, 0, null, 0));
				combo.setLayoutData(UIUtil.createFormData2(0, 0, null, 0, l, 5, newHost, -5));
				refreshConnections();
				combo.addModifyListener(this);
			}
			
			protected void newHostClicked() {
				RSEMainNewConnectionWizard newConnWizard = new RSEMainNewConnectionWizard();
				WizardDialog d = new WizardDialog(getShell(), newConnWizard);
				d.open();
			}
			
			public IHost findHost(String name) {
				for( int i = 0; i < hosts.length; i++ ) {
					if( hosts[i].getAliasName().equals(name))
						return hosts[i];
				}
				return null;
			}

			public Combo getCombo() {
				return combo;
			}
			
			public IHost getHost() {
				return currentHost;
			}
			
			public String getHostName() {
				return currentHostName;
			}
			
			public void setHostName(String name) {
				this.currentHostName = name;
				this.currentHost = findHost(currentHostName);
				if( currentHost == null )
					combo.clearSelection();
				else {
					String[] items = combo.getItems();
					for( int i = 0; i < items.length; i++ ) {
						if( items[i].equals(currentHost.getAliasName())) {
							combo.select(i);
							return;
						}
					}
				}
			}
			
			public void refreshConnections() {
				hosts = RSECorePlugin.getTheSystemRegistry().getHostsBySubSystemConfigurationCategory(fileSubSystem);
				hostsAsStrings = new String[hosts.length];
				int currentHostIndex = -1;
				for( int i = 0; i < hosts.length; i++ ) {
					hostsAsStrings[i] = hosts[i].getAliasName();
					if( currentHostIndex == -1 && currentHostName != null 
							&& hostsAsStrings[i].equals(currentHostName)) {
						currentHostIndex = i;
					}
				}
				
				// refill the combo thingie
				combo.setItems(hostsAsStrings);
				if( currentHostIndex != -1 ) // set the current host
					combo.select(currentHostIndex);
				else
					combo.clearSelection();
			}
	
			@Override
			public void modifyText(ModifyEvent e) {
				int index = combo.getSelectionIndex();
				if( index != -1 ) {
					String s = combo.getItem(index);
					for( int i = 0; i < hosts.length; i++ ) {
						if( hosts[i].getAliasName().equals(s)) {
							currentHost = hosts[i];
							currentHostName = currentHost.getAliasName();
							return;
						}
					}
				}
			}
			public void systemModelResourceChanged(ISystemModelChangeEvent event) {
				if( combo.isDisposed())
					return;
				Display.getDefault().asyncExec(new Runnable(){
					public void run() {
						combo.removeModifyListener(comboMListener);
						refreshConnections();
						combo.addModifyListener(comboMListener);
					}
				});
			}
			@Override
			public void dispose () {
				super.dispose();
				RSECorePlugin.getTheSystemRegistry().removeSystemModelChangeListener(this);			
			}
		}
	}

}
