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
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.core.internal.modules.DeploymentModulePrefs;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferences;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferencesLoader;
import org.jboss.tools.as.core.internal.modules.ModuleDeploymentPrefsUtil;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

/**
 * This controller is in charge of manipulating the deployment preferences model
 * which is stored in the server object. 
 */
public class ModuleDeployPathController extends AbstractSubsystemController
		implements IModuleDeployPathController {
	public ModuleDeployPathController() {
		super();
	}
	protected String getDefaultDeployFolder() {
		String ret = (String)getEnvironment().get(ENV_DEFAULT_DEPLOY_FOLDER);
		if( ret == null ) {
			IDeploymentOptionsController c = (IDeploymentOptionsController)getEnvironment().get(ENV_DEPLOYMENT_OPTIONS_CONTROLLER);
			if( c != null )
				return c.getDeploymentsRootFolder(true);
		}
		return ret;
	}

	protected String getDefaultTmpDeployFolder() {
		String ret = (String)getEnvironment().get(ENV_DEFAULT_TMP_DEPLOY_FOLDER);
		if( ret == null ) {
			IDeploymentOptionsController c = (IDeploymentOptionsController)getEnvironment().get(ENV_DEPLOYMENT_OPTIONS_CONTROLLER);
			if( c != null )
				return c.getDeploymentsTemporaryFolder(true);
		}
		return ret;
	}

	protected char getTargetSystemSeparator() {
		Character c = (Character)getEnvironment().get(ENV_TARGET_OS_SEPARATOR);
		if( c == null )
			return java.io.File.separatorChar;
		return c.charValue();
	}
	
	@Override
	public String getOutputName(IModule[] module) {
		// Get the full path of where this module would be deployed to, and just take the last segment
		// IF the array is of size 1, it will pull from prefs.
		// If the array is larger, it will pull from the relative path of the child to its parent module
		IServerAttributes server = getServerOrWC();
		IPath p = new ModuleDeploymentPrefsUtil().getModuleNestedDeployPath(module, "/", server); //$NON-NLS-1$
		return p.lastSegment();
	}

	@Override
	public String getDeployDirectory(IModule[] module) {
		IServerAttributes server = getServerOrWC();
		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		IPath fullPath = new ModuleDeploymentPrefsUtil().getModuleTreeDestinationFullPath(module, server, getDefaultDeployFolder(), getTargetSystemSeparator());
		if( isBinaryObject )
			fullPath = fullPath == null ? null : fullPath.removeLastSegments(1);
		String ret = fullPath == null ? null : fullPath.toOSString();
		return ret;
	}

	@Override
	public String getTemporaryDeployDirectory(IModule[] module) {
		IServerAttributes server = getServerOrWC();
		IPath fullPath = new ModuleDeploymentPrefsUtil().getModuleTempDeployPath(module, server, getDefaultTmpDeployFolder(), getTargetSystemSeparator());
		String ret = fullPath == null ? null : fullPath.toOSString();
		return ret;
	}

	private void verifyWorkingCopy() throws IllegalStateException {
		IServerWorkingCopy wc = getWorkingCopy();
		if( wc == null )
			throw new IllegalStateException("This controller requires a server working-copy."); //$NON-NLS-1$
	}

	@Override
	public void setOutputName(IModule module, String name) throws IllegalStateException {
		verifyWorkingCopy();
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(getServerOrWC());
		DeploymentModulePrefs modPrefs = prefs.getOrCreatePreferences().getOrCreateModulePrefs(module);
		modPrefs.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME, name);		
		DeploymentPreferencesLoader.savePreferencesToServerWorkingCopy(getWorkingCopy(), prefs);
	}

	@Override
	public void setDeployDirectory(IModule module, String directory) throws IllegalStateException {
		verifyWorkingCopy();
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(getServerOrWC());
		DeploymentModulePrefs modPrefs = prefs.getOrCreatePreferences().getOrCreateModulePrefs(module);
		modPrefs.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, directory);
		DeploymentPreferencesLoader.savePreferencesToServerWorkingCopy(getWorkingCopy(), prefs);
	}

	@Override
	public void setTemporaryDeployDirectory(IModule module, String directory) throws IllegalStateException {
		verifyWorkingCopy();
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(getServerOrWC());
		DeploymentModulePrefs modPrefs = prefs.getOrCreatePreferences().getOrCreateModulePrefs(module);
		modPrefs.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC, directory);		
		DeploymentPreferencesLoader.savePreferencesToServerWorkingCopy(getWorkingCopy(), prefs);
	}

	@Override
	public String getDefaultSuffix(IModule module) {
		return ServerModelUtilities.getDefaultSuffixForModule(module);
		
	}
	
	public IStatus validate() {
		IStatus s = super.validate();
		if( !s.isOK())
			return s;
		if( missingProperty(ENV_DEFAULT_DEPLOY_FOLDER))
			return invalid();
		if( missingProperty(ENV_DEFAULT_TMP_DEPLOY_FOLDER))
			return invalid();
		return Status.OK_STATUS;
	}
	private boolean missingProperty(String key) {
		return getEnvironment() == null || getEnvironment().get(key) == null;
	}
	private IStatus invalid() {
		return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The controller is invalid and is missing required properties"); //$NON-NLS-1$
	}
	
	
}
