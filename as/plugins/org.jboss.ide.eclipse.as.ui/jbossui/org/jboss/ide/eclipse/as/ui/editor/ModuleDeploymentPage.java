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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.IServerEditorPartInput;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class ModuleDeploymentPage extends ServerEditorPart {
	protected ServerResourceCommandManager commandManager;
	protected ArrayList<IModule> possibleModules;
	protected DeploymentPreferences preferences;
	protected ServerAttributeHelper helper; 
	protected DeploymentModuleOptionCompositeAssistant tab;
	
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
		if (input instanceof IServerEditorPartInput) {
			IServerEditorPartInput sepi = (IServerEditorPartInput) input;
			server = sepi.getServer();
			commandManager = ((ServerEditorPartInput) sepi).getServerCommandManager();
			readOnly = sepi.isServerReadOnly();
		}
		helper = new ServerAttributeHelper(server.getOriginal(), server);

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
		allContent.setText("Deployment");
		allContent.getBody().setLayout(new FormLayout());
		return allContent;
	}
	
	private void addDeploymentLocationControls(Composite parent, Control top) {
		tab = new DeploymentModuleOptionCompositeAssistant();
		tab.setDeploymentPage(this);
		tab.setDeploymentPrefs(preferences);
		Composite defaultComposite = tab.createDefaultComposite(parent);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		if( top == null )
			fd.top = new FormAttachment(0, 5);
		else
			fd.top = new FormAttachment(top, 5);
		fd.right = new FormAttachment(100, -5);
		defaultComposite.setLayoutData(fd);
		
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
		commandManager.execute(new ChangePropertyCommand(p,key,val,cmdName));
	}
	
	private class ChangePropertyCommand extends ServerCommand {
		private DeploymentModulePrefs p;
		private String key;
		private String oldVal;
		private String newVal;
		public ChangePropertyCommand(DeploymentModulePrefs p, String key, String val, String commandName) {
			super(ModuleDeploymentPage.this.server, commandName);
			this.p = p;
			this.key = key;
			this.newVal = val;
			this.oldVal = p.getProperty(key);
		}
		public void execute() {
			p.setProperty(key, newVal);
			savePreferencesToWorkingCopy();
		}
		public void undo() {
			p.setProperty(key, oldVal);
			savePreferencesToWorkingCopy();
		}
	}
	
	public void savePreferencesToWorkingCopy() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DeploymentPreferenceLoader.savePreferences(bos, preferences);
		String asXML = new String(bos.toByteArray());
		getHelper().setAttribute(DeploymentPreferenceLoader.DEPLOYMENT_PREFERENCES_KEY, asXML);
	}

	public String makeGlobal(String path) {
		return makeGlobal(path, server.getRuntime());
	}
	
	public static String makeGlobal(String path, IRuntime runtime) {
		IJBossServerRuntime rt = getRuntime(runtime);
		if( rt != null )
			return ServerUtil.makeGlobal(rt, new Path(path)).toString();
		return path;
	}
	
	public String makeRelative(String path) {
		return makeRelative(path, server.getRuntime());
	}
	
	public static String makeRelative(String path, IRuntime runtime) {
		IJBossServerRuntime rt = getRuntime(runtime);
		if (rt == null)
			return path;
		return ServerUtil.makeRelative(rt, new Path(path)).toString();
	}

	private IJBossServerRuntime getRuntime() {
		IRuntime r = server.getRuntime();
		return getRuntime(r);
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
//		try {
//			DeploymentPreferenceLoader.savePreferences(server.getOriginal(), preferences);
//		} catch( IOException ioe ) {
//			// TODO eh?
//		}
	}
}
