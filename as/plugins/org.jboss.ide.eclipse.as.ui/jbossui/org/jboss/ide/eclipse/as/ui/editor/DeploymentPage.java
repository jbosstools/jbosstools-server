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
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.ui.editor.IServerEditorPartInput;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.jboss.ide.eclipse.as.core.server.IServerWorkingCopyProvider;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferences;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferencesLoader;

/**
 * This class is a module deployment page for server editors. It allows customizations 
 * of default deployment folders and per-module customization in names and locations
 * 
 * Clients may extend this on a *provisional* basis. Further changes may come. 
 * @since 3.0
 *
 */
public class DeploymentPage extends ServerEditorPart implements IServerWorkingCopyProvider {
	protected ServerResourceCommandManager commandManager;
	protected DeploymentPreferences preferences;
	protected ServerAttributeHelper helper; 
	private IServerListener listener;
	private IDeploymentPageUIController controller;
	
	private IDeploymentPageUIController getController() {
		if( controller == null ) {
			IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(getServer());
			if( beh != null ) {
				try {
					controller = (IDeploymentPageUIController)beh.getController(IDeploymentPageUIController.SYSTEM_ID);
				} catch(CoreException ce) {
					JBossServerUIPlugin.log(ce.getStatus());
				}
			}
		}
		return controller;
	}
	
	
	/* Cache a helper in charge of letting us set values on the most recent server working copy. */
	public ServerAttributeHelper getHelper() {
		if( helper == null ) {
			helper = new ServerAttributeHelper(getServer().getOriginal(), getServer());
		} else {
			String helperTS = helper.getWorkingCopy().getAttribute("timestamp", (String)null);
			String officialTS = getServer().getAttribute("timestamp", (String)null);
			if( !helperTS.equals(officialTS)) {
				helper = new ServerAttributeHelper(getServer().getOriginal(), getServer());
			}
		}
		return helper;
	}

	public FormToolkit getFormToolkit(Composite parent) {
		return getFormToolkit(parent.getDisplay());
	}
	
	public IServerWorkingCopy getServer() {
		return server;
	}
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		if (input instanceof IServerEditorPartInput) { // always true (?)
			IServerEditorPartInput sepi = (IServerEditorPartInput) input;
			server = sepi.getServer();
			commandManager = ((ServerEditorPartInput) sepi).getServerCommandManager();
			readOnly = sepi.isServerReadOnly();
			helper = new ServerAttributeHelper(server.getOriginal(), server);
			listener = new UnitedServerListener() {
				public void serverChanged(ServerEvent event) {
					fireServerChanged(event);
				}
			};
			server.getOriginal().addServerListener(listener);
			fireServerChanged(null);
		}
		getController().init(site, input, this);
	}
	
	public void dispose() {
		super.dispose();
		server.getOriginal().removeServerListener(listener);
		getController().dispose();
	}
	
	
	public void execute(IUndoableOperation command) {
		commandManager.execute(command);
	}
	
	public void setFocus() {
	}

	/**
	 * Clients are expected to override this method
	 * if they have specialized tasks to perform on a save event
	 */
	public void doSave(IProgressMonitor monitor) {
		getController().doSave(monitor);
	}
	private void fireServerChanged(ServerEvent event) {
		getController().serverChanged(event);
	}

	
	@Override
	public void createPartControl(Composite parent) {
		preferences = DeploymentPreferencesLoader.loadPreferencesFromServer(getServer());
		getController().createPartControl(parent);
	}
	
	public DeploymentPreferences getPreferences() {
		return preferences;
	}
	
	@Override
	public IStatus[] getSaveStatus() {
		return new IStatus[]{};
	}
}
