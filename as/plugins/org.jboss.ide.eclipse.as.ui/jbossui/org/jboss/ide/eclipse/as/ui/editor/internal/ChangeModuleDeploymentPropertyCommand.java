/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor.internal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.server.IServerWorkingCopyProvider;
import org.jboss.tools.as.core.internal.modules.DeploymentModulePrefs;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferences;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferencesLoader;

/**
 * This class represents a command to modify and persist 
 * one or more changed deployment preferences for modules. 
 */
public class ChangeModuleDeploymentPropertyCommand extends ServerCommand {
	private IServerWorkingCopyProvider provider;
	private DeploymentModulePrefs modPrefs;
	private DeploymentPreferences prefs;
	private String[] keys;
	private String[] oldVals;
	private String[] newVals;
	private Viewer toRefresh;
	
	public ChangeModuleDeploymentPropertyCommand(IServerWorkingCopyProvider provider,
			DeploymentPreferences prefs,
			DeploymentModulePrefs modPrefs, String[] keys, 
			String[] vals, String commandName) {
		this(provider, prefs, modPrefs, keys, vals, commandName, null);
	}
	
	public ChangeModuleDeploymentPropertyCommand(IServerWorkingCopyProvider provider,
			DeploymentPreferences prefs,
			DeploymentModulePrefs modPrefs, String[] keys, 
			String[] vals, String commandName, Viewer v) {
		super(provider.getServer(), commandName);
		this.provider = provider;
		this.prefs = prefs;
		this.modPrefs = modPrefs;
		this.keys = keys;
		this.newVals = vals;
		this.oldVals = new String[newVals.length];
		for( int i = 0; i < newVals.length; i++ ) {
			oldVals[i] = modPrefs.getProperty(keys[i]);
		}
		this.toRefresh = v;
	}
	
	public void execute() {
		for( int i = 0; i < keys.length; i++ ) {
			modPrefs.setProperty(keys[i], newVals[i]);
		}
		DeploymentPreferencesLoader.savePreferencesToServerWorkingCopy(provider.getServer(), prefs);
	}
	
	public void undo() {
		for( int i = 0; i < keys.length; i++) {
			modPrefs.setProperty(keys[i], oldVals[i]);
		}
		DeploymentPreferencesLoader.savePreferencesToServerWorkingCopy(provider.getServer(), prefs);
		if( toRefresh != null )
			toRefresh.refresh();
	}
}