/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentTypeUIUtil;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentTypeUIUtil.ICompletable;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;
import org.jboss.ide.eclipse.as.ui.wizards.ServerProfileWizardFragment;

public class RSEWizardFragment extends WizardFragment {
	private IWizardHandle handle;
	private Composite main;
	
	public RSEWizardFragment() {
	}


	protected void initWizardHandle() {
		// make modifications to parent
		handle.setTitle("Remote System Integration");
		handle.setDescription("Please set the properties required for connecting to a remote system.");
		String imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_LOGO;
		handle.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(imageKey));
	}

	public boolean hasComposite() {
		return true;
	}

	public boolean isComplete() {
		return main != null && !main.isDisposed() && super.isComplete();
	}
	
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		// do nothing
	}
	
	private static class DelegatingCallback implements IServerModeUICallback {
		protected IServerModeUICallback callback;
		public DelegatingCallback(IServerModeUICallback cb) {
			this.callback = cb;
		}
		public IServerWorkingCopy getServer() {
			return callback.getServer();
		}
		public IRuntime getRuntime() {
			return callback.getRuntime();
		}
		public void execute(IUndoableOperation operation) {
			callback.execute(operation);
		}
		public void executeLongRunning(Job j) {
			callback.executeLongRunning(j);
		}
		public void setErrorMessage(String msg) {
			callback.setErrorMessage(msg);
		}
		public Object getAttribute(String key) {
			return callback.getAttribute(key);
		}
		public int getCallbackType() {
			return callback.getCallbackType();
		}
		public void setComplete(boolean complete) {
			callback.setComplete(complete);
		}
	};
	
	public Composite createComposite(Composite parent, final IWizardHandle handle) {
		this.handle = handle;
		initWizardHandle();
		
		// Problem is this one was created only to work with the 1st page.  >=[ 
		final IServerModeUICallback callbackDelegate = (IServerModeUICallback)getTaskModel().getObject(ServerProfileWizardFragment.WORKING_COPY_CALLBACK);
		IServerModeUICallback callback = new DelegatingCallback(callbackDelegate){
			public void setComplete(boolean complete) {
				RSEWizardFragment.this.setComplete(complete);
				handle.update();
			}
			public void setErrorMessage(String msg) {
				handle.setMessage(msg, IMessageProvider.ERROR);
				setComplete(msg != null);
			}
		};
		
		
		main = new Composite(parent, SWT.NONE);
		main.setLayout(new FillLayout());
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		main.setLayoutData(gd);
		
		RSEDeploymentPreferenceComposite composite = null;

		IServerWorkingCopy cServer = (IServerWorkingCopy)getTaskModel().getObject(TaskModel.TASK_SERVER);
		IJBossServer jbs = cServer.getOriginal() == null ? 
				ServerConverter.getJBossServer(cServer) :
					ServerConverter.getJBossServer(cServer.getOriginal());
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(cServer);
		
		if( jbs == null || sep == null)
			composite = new DeployOnlyRSEPrefComposite(main, SWT.NONE, callback);
		else if( sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY){
			composite = new JBossRSEDeploymentPrefComposite(main, SWT.NONE, callback);
		} else if( sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS){
			composite = new JBoss7RSEDeploymentPrefComposite(main, SWT.NONE, callback);
		}
		// NEW_SERVER_ADAPTER potential location for new server details
		
		main.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				main = null;
			}
		});
		return main;
	}
	

	
}
