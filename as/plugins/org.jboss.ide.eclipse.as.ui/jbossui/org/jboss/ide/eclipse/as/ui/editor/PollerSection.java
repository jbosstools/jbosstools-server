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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;

/**
 * 
 * @author rob Stryker (rob.stryker@redhat.com)
 *
 */
public class PollerSection extends ServerEditorSection implements PropertyChangeListener {
	private Combo startPollerCombo, stopPollerCombo;
	private Composite pollers;
	private String[] startupTypesStrings, shutdownTypesStrings;
	private IServerStatePollerType[] startupTypes, shutdownTypes;
	private ModifyListener startPollerListener;
	private ModifyListener stopPollerListener;
	protected ServerAttributeHelper helper; 
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
		server.addPropertyChangeListener(this);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		findPossiblePollers();
		createUI(parent);
		addListeners();
		String ignoreLaunch = server.getAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, (String)null);
		Boolean b = new Boolean(ignoreLaunch);
		startPollerCombo.setEnabled(!b.booleanValue());
		stopPollerCombo.setEnabled(!b.booleanValue());
	}
	
	protected void createUI(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.PollerSection_ServerPollingSection);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new FormLayout());
		
		pollers = toolkit.createComposite(composite);
		pollers.setLayout(new GridLayout(2, false));
		
		// create widgets
		Label start, stop;
		start = new Label(pollers, SWT.NONE);
		startPollerCombo = new Combo(pollers, SWT.READ_ONLY);
		stop = new Label(pollers, SWT.NONE);
		stopPollerCombo = new Combo(pollers, SWT.READ_ONLY);

		start.setText(Messages.EditorStartupPollerLabel);
		stop.setText(Messages.EditorShutdownPollerLabel);
		
		// set items
		startPollerCombo.setItems(startupTypesStrings);
		stopPollerCombo.setItems(shutdownTypesStrings);
		
		startPollerCombo.setEnabled(true);
		stopPollerCombo.setEnabled(true);
		String currentStartId = helper.getAttribute(IJBossServerConstants.STARTUP_POLLER_KEY, IJBossServerConstants.DEFAULT_STARTUP_POLLER);
		String currentStopId = helper.getAttribute(IJBossServerConstants.SHUTDOWN_POLLER_KEY, IJBossServerConstants.DEFAULT_SHUTDOWN_POLLER);
		int startIndex = startPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStartId).getName());
		int stopIndex = stopPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStopId).getName());
		
		if( startIndex >= 0 )
			startPollerCombo.select(startIndex);
		if( stopIndex >= 0 )
			stopPollerCombo.select(stopIndex);
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}
	
	protected void refreshUI() {
		startPollerCombo.removeModifyListener(startPollerListener);
		stopPollerCombo.removeModifyListener(stopPollerListener);
		
		findPossiblePollers();
		startPollerCombo.setItems(startupTypesStrings);
		stopPollerCombo.setItems(shutdownTypesStrings);
		String currentStartId = helper.getAttribute(IJBossServerConstants.STARTUP_POLLER_KEY, IJBossServerConstants.DEFAULT_STARTUP_POLLER);
		String currentStopId = helper.getAttribute(IJBossServerConstants.SHUTDOWN_POLLER_KEY, IJBossServerConstants.DEFAULT_SHUTDOWN_POLLER);
		int startIndex = startPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStartId).getName());
		int stopIndex = stopPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStopId).getName());
		if( startIndex >= 0 )
			startPollerCombo.select(startIndex);
		if( stopIndex >= 0 )
			stopPollerCombo.select(stopIndex);
		
		startPollerCombo.addModifyListener(startPollerListener);
		stopPollerCombo.addModifyListener(stopPollerListener);
	}
	
	protected void findPossiblePollers() {
		String currentMode = ServerProfileModel.getProfile(server);
		startupTypes = ExtensionManager.getDefault().getStartupPollers(server.getServerType(), currentMode);
		shutdownTypes = ExtensionManager.getDefault().getShutdownPollers(server.getServerType(), currentMode);
		startupTypesStrings = new String[startupTypes.length];
		shutdownTypesStrings = new String[shutdownTypes.length];
		
		for( int i = 0; i < startupTypes.length; i++ ) {
			startupTypesStrings[i] = startupTypes[i].getName();
		}
		for( int i = 0; i < shutdownTypes.length; i++ ) {
			shutdownTypesStrings[i] = shutdownTypes[i].getName();
		}
	}
	
	protected void addListeners() {
		startPollerListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if( startPollerCombo.getSelectionIndex() != -1 )
					execute(new SetStartupPollerCommand(server));
			}
		};
		stopPollerListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if( stopPollerCombo.getSelectionIndex() != -1 )
					execute(new SetStopPollerCommand(server));
			}
		};

		startPollerCombo.addModifyListener(startPollerListener);
		stopPollerCombo.addModifyListener(stopPollerListener);
	}

	public class SetStartupPollerCommand extends SetPollerCommand {
		public SetStartupPollerCommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangeStartPollerCommandName,  IJBossServerConstants.STARTUP_POLLER_KEY, 
					IJBossServerConstants.DEFAULT_STARTUP_POLLER,
					startupTypes, startPollerCombo, startPollerListener);
		}
	}
	
	public class SetStopPollerCommand extends SetPollerCommand {
		public SetStopPollerCommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangeStopPollerCommandName,  IJBossServerConstants.SHUTDOWN_POLLER_KEY, 
					IJBossServerConstants.DEFAULT_SHUTDOWN_POLLER,
					shutdownTypes, stopPollerCombo, stopPollerListener);
		}
	}
	
	
	public class SetPollerCommand extends ServerCommand {
		private String preChange;
		private String attributeKey;
		private String defaultValue;
		private IServerStatePollerType[] pollerArray;
		private Combo combo;
		private ModifyListener listener;
		public SetPollerCommand(IServerWorkingCopy server, String name, 
				String attributeKey, String defaultValue, IServerStatePollerType[] pollerArray, 
				Combo pollerCombo, ModifyListener listener) {
			super(server, name);
			this.attributeKey = attributeKey;
			this.defaultValue = defaultValue;
			this.pollerArray = pollerArray;
			this.combo = pollerCombo;
			this.listener = listener;
		}
		public void execute() {
			preChange = helper.getAttribute(attributeKey, defaultValue);
			helper.setAttribute(attributeKey, pollerArray[combo.getSelectionIndex()].getId());
		}
		
		public void undo() {
			helper.setAttribute(attributeKey, preChange);
			combo.removeModifyListener(listener);
			int ind = findIndex(preChange);
			if( ind == -1 )
				combo.clearSelection();
			else
				combo.select(ind);
			combo.addModifyListener(listener);
		}

		protected int findIndex(String id) {
			for( int i = 0; i < pollerArray.length; i++)
				if( pollerArray[i].getId().equals(id))
					return i;
			return -1;
		}
	}

	/**
	 * Disposes of the section.
	 */
	public void dispose() {
		server.removePropertyChangeListener(this);
	}

	// Pollers aren't launched if server is assumed externally controlled
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if( propertyName.equals(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS)) {
			Object val = evt.getNewValue();
			Boolean b = new Boolean((String)val);
			startPollerCombo.setEnabled(!b.booleanValue());
			stopPollerCombo.setEnabled(!b.booleanValue());
		}
		if( ServerProfileModel.isProfileKey(propertyName)) {
			refreshUI();
		}
	}

	public IStatus[] getSaveStatus() {
		IStatus s = Status.OK_STATUS;
		if( !server.getAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, false)) {
			if( startPollerCombo.getSelectionIndex() == -1 ) {
				s = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.EditorStartupPollerNotSet);
			}
			if( stopPollerCombo.getSelectionIndex() == -1 ) {
				s = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.EditorShutdownPollerNotSet);
			}
		}
		return new IStatus[] {s};
	}

}
