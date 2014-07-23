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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
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
	
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		// do nothing
	}
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		initWizardHandle();
		
		IServerModeUICallback callback = (IServerModeUICallback)getTaskModel().getObject(ServerProfileWizardFragment.WORKING_COPY_CALLBACK);
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FillLayout());
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
		return main;
	}
	

	
}
