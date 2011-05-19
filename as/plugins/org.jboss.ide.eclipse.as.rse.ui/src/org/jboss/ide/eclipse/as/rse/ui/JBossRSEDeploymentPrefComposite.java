/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.rse.core.RSEPublishMethod;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.rse.ui.RSEDeploymentPreferenceUI.RSEDeploymentPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.editor.ServerModeSectionComposite.ChangeServerPropertyCommand;

public class JBossRSEDeploymentPrefComposite extends
		RSEDeploymentPreferenceComposite {

	private Text rseServerHome,rseServerConfig;
	private Button rseBrowse, rseTest;

	public JBossRSEDeploymentPrefComposite(Composite parent, int style, IServerModeUICallback callback) {
		super(parent, style, callback);
	}

	protected void createRSEWidgets(Composite child) {
		handleJBossServer(child);
	}
	
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
		   IStatus s2 = s[0];
			if( s2.isOK() ) 
				s2 = new Status(IStatus.INFO, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, 
						RSEUIMessages.REMOTE_SERVER_TEST_SUCCESS);
		   showMessageDialog(RSEUIMessages.REMOTE_SERVER_TEST, s2, rseServerHome.getShell());
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

	protected void browseClicked2() {
		String browseVal = browseClicked3(rseServerHome.getShell());
		if (browseVal != null) {
			rseServerHome.setText(browseVal);
			serverHomeChanged();
		}
	}
	protected void propertyChangeBody(PropertyChangeEvent evt) {
		super.propertyChangeBody(evt);
		if( evt.getPropertyName().equals(RSEUtils.RSE_SERVER_HOME_DIR)) {
			updateTextIfChanges(rseServerHome, evt.getNewValue().toString());
		} else if( evt.getPropertyName().equals(RSEUtils.RSE_SERVER_CONFIG)) {
			updateTextIfChanges(rseServerConfig, evt.getNewValue().toString());
		} 
	}
	protected void serverConfigChanged() {
		if( !isUpdatingFromModelChange() ) {
			callback.execute(new ChangeServerPropertyCommand(
					callback.getServer(), RSEUtils.RSE_SERVER_CONFIG, rseServerConfig.getText(), 
					getRuntime() == null ? "" : getRuntime().getJBossConfiguration(),
							RSEUIMessages.CHANGE_REMOTE_SERVER_CONFIG));
		}
	}
	protected void serverHomeChanged() {
		if( !isUpdatingFromModelChange()) {
			String safeString = callback.getRuntime() != null ? callback.getRuntime().getLocation() != null ? 
					callback.getRuntime().getLocation().toString() : "" : "";
			callback.execute(new ChangeServerPropertyCommand(
					callback.getServer(), RSEUtils.RSE_SERVER_HOME_DIR, rseServerHome.getText(), 
					safeString, RSEUIMessages.CHANGE_REMOTE_SERVER_HOME));
		}
	}

}
