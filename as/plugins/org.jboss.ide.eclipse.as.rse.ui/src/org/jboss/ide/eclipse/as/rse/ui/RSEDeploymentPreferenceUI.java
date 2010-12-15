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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvent;
import org.eclipse.rse.core.events.ISystemModelChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.rse.core.RSEPublishMethod;
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
		private Button rseBrowse, rseTest;
		private ModifyListener comboMListener;
		public RSEDeploymentPreferenceComposite(Composite parent, int style, IServerModeUICallback callback) {
			super(parent, style);
			this.callback = callback;
			setLayout(new FormLayout());
			Composite child = new Composite(this, SWT.None);
			child.setLayoutData(UIUtil.createFormData2(0, 0, null, 0, 0, 5, 100, 0));
			child.setLayout(new GridLayout());
			String current = discoverCurrentHost(callback);
			combo = new CustomSystemHostCombo(child, SWT.NULL, current, "files"); //$NON-NLS-1$
			comboMListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					rseHostChanged();
				}
			};
			combo.getCombo().addModifyListener(comboMListener);
			
			IServerWorkingCopy cServer = callback.getServer();
			JBossServer jbs = cServer.getOriginal() == null ? 
					ServerConverter.getJBossServer(cServer) :
						ServerConverter.getJBossServer(cServer.getOriginal());
			if( jbs != null ) {
			
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
						getRuntime() == null ? "" : getRuntime().getRuntime().getLocation().toString()));
				rseServerHome.addModifyListener(new ModifyListener(){
					public void modifyText(ModifyEvent e) {
						serverHomeChanged();
					}});
				
				Label serverConfigLabel = new Label(this, SWT.NONE);
				serverConfigLabel.setText("Remote Server Configuration: ");
				rseServerConfig= new Text(this, SWT.SINGLE | SWT.BORDER);
				serverConfigLabel.setLayoutData(UIUtil.createFormData2(rseServerHome, 7, null, 0, 0, 10, null, 0));
				rseServerConfig.setText(callback.getServer().getAttribute(RSEUtils.RSE_SERVER_CONFIG, 
						getRuntime() == null ? "" : getRuntime().getJBossConfiguration()));
				rseServerConfig.addModifyListener(new ModifyListener(){
					public void modifyText(ModifyEvent e) {
						serverConfigChanged();
					}});
				callback.getServer().addPropertyChangeListener(this);
				
				rseTest = new Button(this, SWT.NONE);
				rseTest.setText("Test...");
				rseTest.setLayoutData(UIUtil.createFormData2(rseServerHome, 5, null, 0, null, 0, 100, -5));
				rseServerConfig.setLayoutData(UIUtil.createFormData2(rseServerHome, 5, null, 0, serverConfigLabel, 5, rseTest, -5));
				rseTest.addSelectionListener(new SelectionListener(){
					public void widgetSelected(SelectionEvent e) {
						testPressed();
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		}
		
		private void testPressed(){
			rseTest.setEnabled(false);
			   IWorkbench wb = PlatformUI.getWorkbench();
			   IProgressService ps = wb.getProgressService();
			   final IStatus[] s = new IStatus[1];
			   Throwable e = null;
			   final String home = rseServerHome.getText();
			   final String config = rseServerConfig.getText();
			   try {
				   ps.busyCursorWhile(new IRunnableWithProgress() {
				      public void run(IProgressMonitor pm) {
				    	  s[0] = testPressed(home, config, pm);
				      }
				   });
			   } catch(InvocationTargetException ite) {
				   e = ite;
			   } catch(InterruptedException ie) {
				   e = ie;
			   }
			   if( s[0] == null && e != null ) {
				   s[0] = new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, e.getMessage(), e);
			   }
			   rseTest.setEnabled(true);
			   showMessageDialog("Test of Remote System", s[0]);
		}
		
		private void showMessageDialog(String title, IStatus s) {
			if( s.isOK() ) 
				s = new Status(IStatus.INFO, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
						"The remote server is properly configured.");
			ErrorDialog d = new ErrorDialog(rseServerHome.getShell(), title, null, s, IStatus.INFO | IStatus.ERROR);
			d.open();
		}
		
		private IStatus testPressed(String home, String config, IProgressMonitor pm) {
			pm.beginTask("Validating Remote Configuration", 1200);
			IHost host = combo.getHost();
			if( host == null ) {
				pm.done(); 
				return getTestFailStatus("Host is empty!");
			}
			pm.worked(100);
			
			IFileServiceSubSystem fileSubSystem = RSEPublishMethod.findFileTransferSubSystem(host);
			if( fileSubSystem == null ) {
				pm.done(); 
				return getTestFailStatus("No File Sub System found on " + host.getName());
			}
			pm.worked(100);

			if(!fileSubSystem.isConnected()) {
			    try {
			    	fileSubSystem.connect(new NullProgressMonitor(), false);
			    } catch (Exception e) {
			    	pm.done(); 
			    	return getTestFailStatus("Error when connecting to file system: " + e.getLocalizedMessage()); 
			    }
			}
			pm.worked(300);

			IFileService service = fileSubSystem.getFileService();
			if( service == null ) {
				pm.done(); 
				return getTestFailStatus("No File Service for " + host.getName());
			}
			pm.worked(100);
			
			String root = home;
			IPath root2 = new Path(root);
			try {
				IHostFile file = service.getFile(root2.removeLastSegments(1).toOSString(), root2.lastSegment(), new NullProgressMonitor());
				if( file == null || !file.exists()) {
					pm.done(); 
					return getTestFailStatus("Server's Home folder " + root2 + " not found on " + service.getName() + " for " + host.getName());
				}
				pm.worked(300);
				
				root2 = root2.append(IConstants.SERVER).append(config);
				file = service.getFile(root2.removeLastSegments(1).toOSString(), root2.lastSegment(), new NullProgressMonitor());
				if( file == null || !file.exists()) {
					pm.done(); 
					return getTestFailStatus("Server's config folder " + root2 + " does not exist");
				}
				pm.worked(300);
			} catch(SystemMessageException sme) {
				pm.done();
				return getTestFailStatus("Error while checking remote folders: " + sme.getLocalizedMessage());
			}
			pm.done(); 
			return Status.OK_STATUS;
		}
		
		private IStatus getTestFailStatus(String string) {
			return new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, string);
		}
		
		private String discoverCurrentHost(IServerModeUICallback callback) {
			String current = callback.getServer().getAttribute(RSEUtils.RSE_SERVER_HOST, (String)null);
			if( current == null ) {
				String serverHost = callback.getServer().getHost().toLowerCase();
				IHost[] hosts = RSECorePlugin.getTheSystemRegistry().getHostsBySubSystemConfigurationCategory("files");
				String name, hostName;
				for( int i = 0; i < hosts.length; i++ ) {
					name = hosts[i].getName();
					hostName = hosts[i].getHostName();
					if( hostName.toLowerCase().equals(serverHost)) {
						callback.getServer().setAttribute(RSEUtils.RSE_SERVER_HOST, name);
						return hosts[i].getName();
					}
				}
			}
			return current;
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
						callback.getServer(), RSEUtils.RSE_SERVER_HOME_DIR, rseServerHome.getText(), 
						getRuntime() == null ? "" : getRuntime().getRuntime().getLocation().toString(),
						"Change RSE Server's Home Directory"));
			}
		}

		protected void serverConfigChanged() {
			if( !updatingFromModelChange ) {
				callback.execute(new ChangeServerPropertyCommand(
						callback.getServer(), RSEUtils.RSE_SERVER_CONFIG, rseServerConfig.getText(), 
						getRuntime() == null ? "" : getRuntime().getJBossConfiguration(),
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
