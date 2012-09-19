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

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.ui.editor.IServerEditorPartInput;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class ModuleDeploymentPage extends ServerEditorPart {
	protected ServerResourceCommandManager commandManager;
	protected ArrayList<IModule> possibleModules;
	protected DeploymentPreferences preferences;
	protected ServerAttributeHelper helper; 
	protected DeploymentModuleOptionCompositeAssistant tab;
	private IServerListener listener;
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

	public IModule[] getPossibleModules() {
		return (IModule[]) possibleModules.toArray(new IModule[possibleModules.size()]);
	}
	
	public FormToolkit getFormToolkit(Composite parent) {
		return getFormToolkit(parent.getDisplay());
	}
	
	public IServerWorkingCopy getServer() {
		return server;
	}
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		refreshPossibleModules();
		if (input instanceof IServerEditorPartInput) {
			IServerEditorPartInput sepi = (IServerEditorPartInput) input;
			server = sepi.getServer();
			commandManager = ((ServerEditorPartInput) sepi).getServerCommandManager();
			readOnly = sepi.isServerReadOnly();
			helper = new ServerAttributeHelper(server.getOriginal(), server);
			listener = new UnitedServerListener() {
				public void serverChanged(ServerEvent event) {
					setDeploymentTabEnablement(event.getModule());
				}
			};
			server.getOriginal().addServerListener(listener);
		}
	}
	
	protected void setDeploymentTabEnablement(IModule[] changed) {
		// This is a big hack due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=386718
		// IT seems getting the NEW module list from the event is not possible,
		// and figuring out if a module was added also does not seem to be possible
		new Thread() {
			public void run() {
				try {
					Thread.sleep(300);
				} catch(InterruptedException ie) {}
				
				IModule[] deployed = server.getOriginal().getModules();
				final boolean hasNoModules = deployed == null || deployed.length == 0;
				final boolean enabled =  hasNoModules && server.getOriginal().getServerPublishState() == IServer.PUBLISH_STATE_NONE;
				Display.getDefault().asyncExec(new Runnable() { 
					public void run() {
						tab.setEnabled(enabled);
					}
				});
			}
		}.start();
	}
	
	public void dispose() {
		super.dispose();
		server.getOriginal().removeServerListener(listener);
	}
	public void refreshPossibleModules() {
		ArrayList<IModule> possibleChildren = new ArrayList<IModule>();
		IModule[] modules2 = org.eclipse.wst.server.core.ServerUtil.getModules(server.getServerType().getRuntimeType().getModuleTypes());
		if (modules2 != null) {
			int size = modules2.length;
			for (int i = 0; i < size; i++) {
				IModule module = modules2[i];
				IStatus status = server.canModifyModules(new IModule[] { module }, null, null);
				if (status != null && status.getSeverity() != IStatus.ERROR)
					possibleChildren.add(module);
			}
		}
		this.possibleModules = possibleChildren;
	}

	public void createPartControl(Composite parent) {
		preferences = DeploymentPreferenceLoader.loadPreferencesFromServer(server.getOriginal());
		ScrolledForm innerContent = createPageStructure(parent);
		addDeploymentLocationControls(innerContent.getBody(), null);
		
		innerContent.reflow(true);
	}
	
	private ScrolledForm createPageStructure(Composite parent) {
		FormToolkit toolkit = getFormToolkit(parent);
		ScrolledForm allContent = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(allContent.getForm());
		allContent.setText(Messages.EditorDeployment);
		allContent.getBody().setLayout(new FormLayout());
		return allContent;
	}
	
	private void addDeploymentLocationControls(Composite parent, Control top) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Label l1 = toolkit.createLabel(parent, Messages.EditorDeploymentPageWarning); 
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = top == null ? new FormAttachment(0, 5) : new FormAttachment(top, 5); 
		fd.right = new FormAttachment(100, -5);
		l1.setLayoutData(fd);
		
		tab = new DeploymentModuleOptionCompositeAssistant();
		tab.setDeploymentPage(this);
		tab.setDeploymentPrefs(preferences);
		Composite defaultComposite = tab.createDefaultComposite(parent);
		defaultComposite.setLayoutData(UIUtil.createFormData2(l1, 5, null,0,0,5,100,-5));
		
		Composite viewComposite = tab.createViewerPortion(parent);
		fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(defaultComposite, 5);
		fd.right = new FormAttachment(100, -5);
		fd.bottom = new FormAttachment(100, -5);
		viewComposite.setLayoutData(fd);
	}

	public void execute(ServerCommand command) {
		commandManager.execute(command);
	}
	
	public void firePropertyChangeCommand(DeploymentModulePrefs p, String key, String val, String cmdName) {
		firePropertyChangeCommand(p, new String[]{key},new String[]{val},cmdName);
	}
	
	public void firePropertyChangeCommand(DeploymentModulePrefs p, String[] keys, String[] vals, String cmdName) {
		commandManager.execute(new ChangePropertyCommand(p, keys,vals,cmdName));
	}

	
	private class ChangePropertyCommand extends ServerCommand {
		private DeploymentModulePrefs p;
		private String[] keys;
		private String[] oldVals;
		private String[] newVals;
		public ChangePropertyCommand(DeploymentModulePrefs p, String key, String val, String commandName) {
			this(p, new String[]{key}, new String[]{val}, commandName);
		}
		
		public ChangePropertyCommand(DeploymentModulePrefs p, String[] keys, 
				String[] vals, String commandName) {
			super(ModuleDeploymentPage.this.server, commandName);
			this.p = p;
			this.keys = keys;
			this.newVals = vals;
			this.oldVals = new String[newVals.length];
			for( int i = 0; i < newVals.length; i++ ) {
				oldVals[i] = p.getProperty(keys[i]);
			}
		}
		
		public void execute() {
			for( int i = 0; i < keys.length; i++ ) {
				p.setProperty(keys[i], newVals[i]);
			}
			savePreferencesToWorkingCopy();
		}
		public void undo() {
			for( int i = 0; i < keys.length; i++) {
				p.setProperty(keys[i], oldVals[i]);
			}
			savePreferencesToWorkingCopy();
		}
	}
	
	public void savePreferencesToWorkingCopy() {
		DeploymentPreferenceLoader.savePreferencesToServerWorkingCopy(helper, preferences);
	}

	public String makeGlobal(String path) {
		return makeGlobal(path, server.getRuntime());
	}
	
	public static String makeGlobal(String path, IRuntime runtime) {
		return ServerUtil.makeGlobal(runtime, new Path(path)).toString();
	}
	
	public String makeRelative(String path) {
		return makeRelative(path, server.getRuntime());
	}
	
	public static String makeRelative(String path, IRuntime runtime) {
		return ServerUtil.makeRelative(runtime, new Path(path)).toString();
	}

	public static IJBossServerRuntime getRuntime(IRuntime r) {
		IJBossServerRuntime ajbsrt = null;
		if (r != null) {
			ajbsrt = (IJBossServerRuntime) r
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}
	
	public void setFocus() {
	}

	// Currently inactive!!! See bug 286699
	public void doSave(IProgressMonitor monitor) {
		tab.updateListeners();
	}
}
