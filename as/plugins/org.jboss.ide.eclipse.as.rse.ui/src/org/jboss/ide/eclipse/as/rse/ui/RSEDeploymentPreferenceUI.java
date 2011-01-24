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
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.rse.core.RSEPublishMethod;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.ui.Messages;
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
				handleJBossServer(child);
			} else {
				handleDeployOnlyServer(child);
			}
		}
		private IDeployableServer getServer() {
			return (IDeployableServer) callback.getServer().loadAdapter(
					IDeployableServer.class, new NullProgressMonitor());
		}

		private Text deployText, tempDeployText;
		private Button deployButton, tempDeployButton;
		private ModifyListener deployListener, tempDeployListener;

		private void handleJBossServer(Composite composite) {
			Label serverHomeLabel = new Label(this, SWT.NONE);
			serverHomeLabel.setText("Remote Server Home: ");
			rseBrowse = new Button(this, SWT.NONE);
			rseBrowse.setText("Browse...");
			rseBrowse.setLayoutData(UIUtil.createFormData2(composite, 5, null,
					0, null, 0, 100, -5));
			rseBrowse.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					browseClicked2();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					browseClicked2();
				}
			});
			rseServerHome = new Text(this, SWT.SINGLE | SWT.BORDER);
			serverHomeLabel.setLayoutData(UIUtil.createFormData2(composite, 7,
					null, 0, 0, 10, null, 0));
			rseServerHome.setLayoutData(UIUtil.createFormData2(composite, 5,
					null, 0, serverHomeLabel, 5, rseBrowse, -5));
			rseServerHome.setText(callback.getServer().getAttribute(
					RSEUtils.RSE_SERVER_HOME_DIR, RSEUIMessages.UNSET_REMOTE_SERVER_HOME));
			rseServerHome.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					serverHomeChanged();
				}
			});

			Label serverConfigLabel = new Label(this, SWT.NONE);
			serverConfigLabel.setText(RSEUIMessages.REMOTE_SERVER_CONFIG);
			rseServerConfig = new Text(this, SWT.SINGLE | SWT.BORDER);
			serverConfigLabel.setLayoutData(UIUtil.createFormData2(
					rseServerHome, 7, null, 0, 0, 10, null, 0));
			rseServerConfig.setText(callback.getServer().getAttribute(
					RSEUtils.RSE_SERVER_CONFIG,
					getRuntime() == null ? "" : getRuntime()
							.getJBossConfiguration()));
			rseServerConfig.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					serverConfigChanged();
				}
			});
			callback.getServer().addPropertyChangeListener(this);

			rseTest = new Button(this, SWT.NONE);
			rseTest.setText(RSEUIMessages.TEST);
			rseTest.setLayoutData(UIUtil.createFormData2(rseServerHome, 5,
					null, 0, null, 0, 100, -5));
			rseServerConfig.setLayoutData(UIUtil.createFormData2(rseServerHome,
					5, null, 0, serverConfigLabel, 5, rseTest, -5));
			rseTest.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					testPressed();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}



		private void handleDeployOnlyServer(Composite composite) {
			Label label = new Label(this, SWT.NONE);
			label.setText(Messages.swf_DeployDirectory);
			deployText = new Text(this, SWT.BORDER);
			deployText.setText(getServer().getDeployFolder());
			deployListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					callback.execute(new SetDeployDirCommand());
				}
			};
			deployText.addModifyListener(deployListener);

			deployButton = new Button(this, SWT.PUSH);
			deployButton.setText(Messages.browse);
			label.setLayoutData(UIUtil.createFormData2(composite, 7, null, 0, 0, 10, null, 0));
			deployButton.setLayoutData(UIUtil.createFormData2(composite, 5, null, 0, null, 0, 100, -5));
			deployText.setLayoutData(UIUtil.createFormData2(composite, 5, null, 0, label, 5, deployButton, -5));

			deployButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
				}
			});

			Label tempDeployLabel = new Label(this, SWT.NONE);
			tempDeployLabel.setText(Messages.swf_TempDeployDirectory);
			tempDeployText = new Text(this, SWT.BORDER);
			tempDeployText.setText(getServer().getTempDeployFolder());
			tempDeployListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					callback.execute(new SetTempDeployDirCommand());
				}
			};
			tempDeployText.addModifyListener(tempDeployListener);

			tempDeployButton = new Button(this, SWT.PUSH);
			tempDeployButton.setText(Messages.browse);
			
			tempDeployLabel.setLayoutData(UIUtil.createFormData2(deployText, 7, null, 0, 0, 10, null, 0));
			tempDeployButton.setLayoutData(UIUtil.createFormData2(deployText, 5, null, 0, null, 0, 100, -5));
			tempDeployText.setLayoutData(UIUtil.createFormData2(deployText, 5, null, 0, tempDeployLabel, 5, deployButton, -5));

			tempDeployButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
				}
			});
		}
		
		private void updateDeployOnlyWidgets() {
			String newDir = callback.getServer().getAttribute(IDeployableServer.DEPLOY_DIRECTORY, "");
			String newTemp = callback.getServer().getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, "");
			deployText.removeModifyListener(deployListener);
			deployText.setText(newDir);
			deployText.addModifyListener(deployListener);
			tempDeployText.removeModifyListener(tempDeployListener);
			tempDeployText.setText(newTemp);
			tempDeployText.addModifyListener(tempDeployListener);
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
			   showMessageDialog(RSEUIMessages.REMOTE_SERVER_TEST, s[0]);
		}
		
		private void showMessageDialog(String title, IStatus s) {
			if( s.isOK() ) 
				s = new Status(IStatus.INFO, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
						RSEUIMessages.REMOTE_SERVER_TEST_SUCCESS);
			ErrorDialog d = new ErrorDialog(rseServerHome.getShell(), title, null, s, IStatus.INFO | IStatus.ERROR);
			d.open();
		}
		
		private IStatus testPressed(String home, String config, IProgressMonitor pm) {
			pm.beginTask(RSEUIMessages.VALIDATING_REMOTE_CONFIG, 1200);
			IHost host = combo.getHost();
			if( host == null ) {
				pm.done(); 
				return getTestFailStatus(RSEUIMessages.EMPTY_HOST);
			}
			pm.worked(100);
			
			IFileServiceSubSystem fileSubSystem = RSEPublishMethod.findFileTransferSubSystem(host);
			if( fileSubSystem == null ) {
				pm.done(); 
				return getTestFailStatus(NLS.bind(RSEUIMessages.FILE_SUBSYSTEM_NOT_FOUND, host.getName()));
			}
			pm.worked(100);

			if(!fileSubSystem.isConnected()) {
			    try {
			    	fileSubSystem.connect(new NullProgressMonitor(), false);
			    } catch (Exception e) {
			    	pm.done(); 
			    	return getTestFailStatus(NLS.bind(RSEUIMessages.REMOTE_FILESYSTEM_CONNECT_FAILED, e.getLocalizedMessage())); 
			    }
			}
			pm.worked(300);

			IFileService service = fileSubSystem.getFileService();
			if( service == null ) {
				pm.done(); 
				return getTestFailStatus(NLS.bind(RSEUIMessages.FILESERVICE_NOT_FOUND, host.getName()));
			}
			pm.worked(100);
			
			String root = home;
			IPath root2 = new Path(root);
			try {
				IHostFile file = service.getFile(root2.removeLastSegments(1).toPortableString(), root2.lastSegment(), new NullProgressMonitor());
				if( file == null || !file.exists()) {
					pm.done(); 
					return getTestFailStatus(NLS.bind(RSEUIMessages.REMOTE_HOME_NOT_FOUND, 
							new Object[]{root2, service.getName(), host.getName()}));
					
				}
				pm.worked(300);
				
				root2 = root2.append(IConstants.SERVER).append(config);
				file = service.getFile(root2.removeLastSegments(1).toPortableString(), root2.lastSegment(), new NullProgressMonitor());
				if( file == null || !file.exists()) {
					pm.done(); 
					return getTestFailStatus(NLS.bind(RSEUIMessages.REMOTE_CONFIG_NOT_FOUND, root2));
				}
				pm.worked(300);
			} catch(SystemMessageException sme) {
				pm.done();
				return getTestFailStatus(RSEUIMessages.ERROR_CHECKING_REMOTE_SYSTEM + sme.getLocalizedMessage());
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
				updateTextIfChanges(rseServerHome, evt.getNewValue().toString());
			} else if( evt.getPropertyName().equals(RSEUtils.RSE_SERVER_CONFIG)) {
				updateTextIfChanges(rseServerConfig, evt.getNewValue().toString());
			} else if( evt.getPropertyName().equals(RSEUtils.RSE_SERVER_HOST)) {
				combo.setHostName(evt.getNewValue().toString());
			}		
			updatingFromModelChange = false;
		}
		
		private void updateTextIfChanges(Text control, String newValue) {
			if(!control.getText().equals(newValue)) {
				control.setText(newValue);
			}
		}

		protected void browseClicked2() {
			String browseVal = browseClicked3();
			if (browseVal != null) {
				rseServerHome.setText(browseVal);
				serverHomeChanged();
			}
		}

		protected String browseClicked3() {
			SystemRemoteFileDialog d = new SystemRemoteFileDialog(
					rseBrowse.getShell(), RSEUIMessages.BROWSE_REMOTE_SYSTEM, combo.getHost());
			if( d.open() == Dialog.OK) {
				Object o = d.getOutputObject();
				if( o instanceof IRemoteFile ) {
					String path = ((IRemoteFile)o).getAbsolutePath();
					return path;
				}
			}
			return null;
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
							RSEUIMessages.CHANGE_RSE_HOST));
					callback.execute(new ChangeServerPropertyCommand(
							callback.getServer(), "hostname", combo.getHost().getHostName(), 
							RSEUIMessages.CHANGE_HOSTNAME));
				}
			}
		}
		
		protected void serverHomeChanged() {
			if( !updatingFromModelChange) {
				callback.execute(new ChangeServerPropertyCommand(
						callback.getServer(), RSEUtils.RSE_SERVER_HOME_DIR, rseServerHome.getText(), 
						getRuntime() == null ? "" : getRuntime().getRuntime().getLocation().toString(),
						RSEUIMessages.CHANGE_REMOTE_SERVER_HOME));
			}
		}

		protected void serverConfigChanged() {
			if( !updatingFromModelChange ) {
				callback.execute(new ChangeServerPropertyCommand(
						callback.getServer(), RSEUtils.RSE_SERVER_CONFIG, rseServerConfig.getText(), 
						getRuntime() == null ? "" : getRuntime().getJBossConfiguration(),
								RSEUIMessages.CHANGE_REMOTE_SERVER_CONFIG));
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
				newHost = new Button(this, SWT.NONE);
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

		public class SetDeployDirCommand extends ServerCommand {
			private String oldDir;
			private String newDir;
			private Text text;
			private ModifyListener listener;

			public SetDeployDirCommand() {
				super(callback.getServer(), Messages.EditorSetDeployLabel);
				this.text = deployText;
				this.newDir = deployText.getText();
				this.listener = deployListener;
				this.oldDir = callback.getServer().getAttribute(
						IDeployableServer.DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
			}

			public void execute() {
				callback.getServer().setAttribute(
						IDeployableServer.DEPLOY_DIRECTORY, newDir);
				updateDeployOnlyWidgets();
			}

			public void undo() {
				callback.getServer().setAttribute(
						IDeployableServer.DEPLOY_DIRECTORY, oldDir);
				updateDeployOnlyWidgets();
			}
		}

		public class SetTempDeployDirCommand extends ServerCommand {
			private String oldDir;
			private String newDir;
			private Text text;
			private ModifyListener listener;

			public SetTempDeployDirCommand() {
				super(callback.getServer(), Messages.EditorSetTempDeployLabel);
				text = tempDeployText;
				newDir = tempDeployText.getText();
				listener = tempDeployListener;
				oldDir = callback.getServer().getAttribute(
						IDeployableServer.TEMP_DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
			}

			public void execute() {
				callback.getServer().setAttribute(
						IDeployableServer.TEMP_DEPLOY_DIRECTORY, newDir);
			}

			public void undo() {
				callback.getServer().setAttribute(
						IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldDir);
			}
		}
	}
}
