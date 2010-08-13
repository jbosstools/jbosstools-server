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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.IServerEditorPartInput;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class ServerModeSection extends ServerEditorSection {
	private ArrayList<DeployUIAdditions> deployAdditions;
	private ScrolledPageBook preferencePageBook;
	private Combo deployTypeCombo;
	private Section publishTypeSection;
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
		
	public void createSection(Composite parent) {
		super.createSection(parent);
		createPublishMethodSection(parent);
	}

	
	private class DeployUIAdditions {
		private IJBossServerPublishMethodType publishType;
		private IDeploymentTypeUI ui;
		private boolean registered = false;
		public DeployUIAdditions(IJBossServerPublishMethodType type,IDeploymentTypeUI ui) {
			this.publishType = type;
			this.ui = ui;
		}
		public boolean isRegistered() {
			return registered;
		}
		public IJBossServerPublishMethodType getPublishType() {
			return publishType;
		}
		public void createComposite(Composite parent) {
			// UI can be null
			if( ui != null ) {
				ui.fillComposite(parent, ServerModeSection.this);
				registered = true;
			}
		}
	}

	private void loadDeployTypeData() {
		deployAdditions = new ArrayList<DeployUIAdditions>();
		IJBossServerPublishMethodType[] publishMethodTypes = ExtensionManager.getDefault().findPossiblePublishMethods(server.getOriginal());
		for( int i = 0; i < publishMethodTypes.length; i++) {
			IDeploymentTypeUI ui = EditorExtensionManager.getDefault().getPublishPreferenceUI(publishMethodTypes[i].getId());
			deployAdditions.add(new DeployUIAdditions(publishMethodTypes[i], ui));
		}
	}

	private Control createPublishMethodSection(Composite parent) {
		loadDeployTypeData();

		FormToolkit toolkit2 = new FormToolkit(parent.getDisplay());
		publishTypeSection = toolkit2.createSection(parent,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
						| ExpandableComposite.TITLE_BAR);
		publishTypeSection.setText("Server Behaviour");
		Composite c = toolkit2.createComposite(publishTypeSection);
		publishTypeSection.setClient(c);
		publishTypeSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		c.setLayout(new FormLayout());
		deployTypeCombo = new Combo(c, SWT.DEFAULT);
		FormData fd = UIUtil.createFormData2(0, 5, null, 0, 0, 5, 50, -5);
		deployTypeCombo.setLayoutData(fd);
		

	    preferencePageBook = toolkit2.createPageBook(c, SWT.FLAT|SWT.TOP);
	    preferencePageBook.setLayoutData(UIUtil.createFormData2(
	    		deployTypeCombo, 5, 0, 150, 0, 5, 100, -5));

	    // fill widgets
	    String[] nameList = new String[deployAdditions.size()];
	    for( int i = 0; i < nameList.length; i++ ) {
	    	nameList[i] = deployAdditions.get(i).getPublishType().getName();
	    }
	    deployTypeCombo.setItems(nameList);
		DeployableServerBehavior ds = ServerConverter.getDeployableServerBehavior(server.getOriginal());
		if( ds != null ) {
			String current = ds.createPublishMethod().getPublishMethodType().getName();
			int index = deployTypeCombo.indexOf(current);
			if( index != -1 ) 
				deployTypeCombo.select(index);
		}
	    deployTypeCombo.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				deployTypeChanged(true);
			}});
	    deployTypeChanged(false);
		return publishTypeSection;
	}
	
	private void deployTypeChanged(boolean fireEvent) {
		int index = deployTypeCombo.getSelectionIndex();
		if( index != -1 ) {
			DeployUIAdditions ui = deployAdditions.get(index);
			if( !ui.isRegistered()) {
				Composite newRoot = preferencePageBook.createPage(ui);
				ui.createComposite(newRoot);
			}
			preferencePageBook.showPage(ui);
			if( fireEvent ) {
				commandManager.execute(new ChangeServerPropertyCommand(
						server, IDeployableServer.SERVER_MODE, 
						ui.getPublishType().getId(), "Change server mode"));
			}
		} else {
			// null selection
		}
	}

	
	public static class ChangeServerPropertyCommand extends ServerCommand {
		private IServerWorkingCopy server;
		private String key;
		private String oldVal;
		private String newVal;
		public ChangeServerPropertyCommand(IServerWorkingCopy server, String key, String val, String commandName) {
			super(server, commandName);
			this.server = server;
			this.key = key;
			this.newVal = val;
			this.oldVal = server.getAttribute(key, LocalPublishMethod.LOCAL_PUBLISH_METHOD);
		}
		public void execute() {
			server.setAttribute(key, newVal);
		}
		public void undo() {
			server.setAttribute(key, oldVal);
		}
	}

}
