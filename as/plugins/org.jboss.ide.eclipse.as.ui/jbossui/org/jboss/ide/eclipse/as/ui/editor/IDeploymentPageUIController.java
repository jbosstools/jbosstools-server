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
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

public interface IDeploymentPageUIController extends ISubsystemController {
	
	public static final String SYSTEM_ID = "deploymentPage"; //$NON-NLS-1$
	
	
	/**
	 * Respond to changes in the server after a server is saved.
	 * This does not alert to changes in the working copy
	 * 
	 * @param event
	 */
	public void serverChanged(ServerEvent event);
	
	/**
	 * Save this page
	 * @param monitor
	 */
	public void doSave(IProgressMonitor monitor);
	
	/**
	 * Initialize the page with the editor input. 
	 * @param site
	 * @param input
	 * @param page
	 */
	public void init(IEditorSite site, IEditorInput input, DeploymentPage page);
	
	/**
	 * Create the widgets for the page
	 * @param parent
	 */
	public void createPartControl(Composite parent);
	
	/**
	 * Dispose
	 */
	public void dispose();

}
