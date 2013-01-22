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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;

public class ServerModeSection extends ServerEditorSection {
	private IServerModeUICallback callback = null;
	private IEditorInput input;
	private ServerEditorPart editor;
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		this.input = input;
	}
	
	public IServerWorkingCopy getServer() {
		return server;
	}
		
	private IServerModeUICallback getUICallback() {
		if( callback == null ) {
			callback = DeploymentTypeUIUtil.getCallback(server, input, editor, this);
		}
		return callback;
	}
	
	@Override
	public void setServerEditorPart(ServerEditorPart editor) {
		super.setServerEditorPart(editor);
		this.editor = editor;
	}

	
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit2 = new FormToolkit(parent.getDisplay());
		Section publishTypeSection = toolkit2.createSection(parent,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
						| ExpandableComposite.TITLE_BAR);
		publishTypeSection.setText(Messages.ServerBehavior);
		Control c = createPublishMethodComposite(publishTypeSection);
		publishTypeSection.setClient(c);
		publishTypeSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
	}

	private Composite createPublishMethodComposite(Composite parent) {
		return new ServerModeSectionComposite(parent, SWT.None, getUICallback(), getManagedForm());
	}
}
