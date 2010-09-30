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
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.IServerEditorPartInput;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;

public class ServerModeSection extends ServerEditorSection {
	private IServerModeUICallback callback = null;
	private ServerResourceCommandManager commandManager;
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		if (input instanceof IServerEditorPartInput) {
			IServerEditorPartInput sepi = (IServerEditorPartInput) input;
			server = sepi.getServer();
			commandManager = ((ServerEditorPartInput) sepi).getServerCommandManager();
		}
	}
	
	public ServerResourceCommandManager getCommandManager() {
		return commandManager;
	}
	
	public IServerWorkingCopy getServer() {
		return server;
	}
		
	private IServerModeUICallback getUICallback() {
		if( callback == null ) {
			callback = new IServerModeUICallback(){
				public IServerWorkingCopy getServer() {
					return server;
				}
				public void execute(IUndoableOperation operation) {
					getCommandManager().execute(operation);
				}
				public IRuntime getRuntime() {
					return server.getRuntime();
				}
			};
		}
		return callback;
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit2 = new FormToolkit(parent.getDisplay());
		Section publishTypeSection = toolkit2.createSection(parent,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
						| ExpandableComposite.TITLE_BAR);
		publishTypeSection.setText("Server Behaviour");
		Control c = createPublishMethodComposite(publishTypeSection);
		publishTypeSection.setClient(c);
		publishTypeSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
	}

	private Composite createPublishMethodComposite(Composite parent) {
		return new ServerModeSectionComposite(parent, SWT.None, getUICallback());
	}
}
