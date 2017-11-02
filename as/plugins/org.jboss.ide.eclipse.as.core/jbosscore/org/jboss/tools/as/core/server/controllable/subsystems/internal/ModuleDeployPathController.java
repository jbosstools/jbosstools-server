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

import org.eclipse.core.runtime.CoreException;
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
import org.jboss.tools.as.core.internal.modules.DeploymentPreferences;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferencesLoader;
import org.jboss.tools.as.core.internal.modules.ModuleDeploymentPrefsUtil;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

/**
 * This controller is in charge of manipulating the deployment preferences model
 * which is stored in the server object. 
 * 
 * It requires several environment variables to work, specifically, knowing 
 * the root directory for which modules' paths should be calculated against. 
 * These can either be set directly in the environment, or, 
 * an IDeploymentOptionsController may be in the environment for use. 
 * 
 * As a last resort, this will ask the behavior for a IDeploymentOptionsController
 * to use. 
 */
public class ModuleDeployPathController extends AbstractSubsystemController
		implements IModuleDeployPathController {
	
	// An optional controller which may be resolved to calculate default deploy folder etc. 
	private IDeploymentOptionsController options;
	
	public ModuleDeployPathController() {
		super();
	}
	
	private IDeploymentOptionsController getDeploymentOptions() {
		if( options == null ) {
			options = (IDeploymentOptionsController)getEnvironment().get(ENV_DEPLOYMENT_OPTIONS_CONTROLLER);
			if( options == null ) {
				try {
					options = (IDeploymentOptionsController)findDependencyFromBehavior(IDeploymentOptionsController.SYSTEM_ID);
				} catch(CoreException ce) {
					JBossServerCorePlugin.log(ce.getStatus());
				}
			}
		}
		return options;
	}
	
	protected String getDefaultDeployFolder() {
		String ret = (String)getEnvironment().get(ENV_DEFAULT_DEPLOY_FOLDER);
		if( ret == null ) {
			IDeploymentOptionsController c = getDeploymentOptions();
			if( c != null )
				return c.getDeploymentsRootFolder(true);
		}
		return ret;
	}

	protected String getDefaultTmpDeployFolder() {
		String ret = (String)getEnvironment().get(ENV_DEFAULT_TMP_DEPLOY_FOLDER);
		if( ret == null ) {
			IDeploymentOptionsController c = getDeploymentOptions();
			if( c != null )
				return c.getDeploymentsTemporaryFolder(true);
		}
		return ret;
	}

	protected char getTargetSystemSeparator() {
		Character c = (Character)getEnvironment().get(ENV_TARGET_OS_SEPARATOR);
		if( c == null ) {
			IDeploymentOptionsController cont =getDeploymentOptions();
			if( cont == null )
				return java.io.File.separatorChar;
			return cont.getPathSeparatorCharacter();
		}
		return c.charValue();
	}
	
	@Override
	public String getOutputName(IModule[] module) {
		// Get the full path of where this module would be deployed to, and just take the last segment
		// IF the array is of size 1, it will pull from prefs.
		// If the array is larger, it will pull from the relative path of the child to its parent module
		IServerAttributes server = getServerOrWC();
		IPath p = createModuleDeploymentPrefsUtil().getModuleNestedDeployPath(module, "/", server); //$NON-NLS-1$
		return p.lastSegment();
	}

	protected ModuleDeploymentPrefsUtil createModuleDeploymentPrefsUtil() {
		return new ModuleDeploymentPrefsUtil();
	}
	
	@Override
	public IPath getDeployDirectory(IModule[] module) {
		return getDeployDirectory(module, isBinaryModule(module));
	}
	
	@Override
	public IPath getDeployDirectory(IModule[] module, boolean binary) {
		IServerAttributes server = getServerOrWC();
		IPath fullPath = createModuleDeploymentPrefsUtil().getModuleTreeDestinationFullPath(module, server, getDefaultDeployFolder(), getTargetSystemSeparator());
		if( !binary ) {
			return fullPath;
		} else {
			return fullPath.removeLastSegments(1);
		}
	}
	
	protected boolean isBinaryModule(IModule[] module) {
		return ServerModelUtilities.isBinaryModule(module);		
	}

	@Override
	public IPath getTemporaryDeployDirectory(IModule[] module) {
		IServerAttributes server = getServerOrWC();
		IPath fullPath = createModuleDeploymentPrefsUtil().getModuleTempDeployPath(module, server, getDefaultTmpDeployFolder(), getTargetSystemSeparator());
		return fullPath;
	}

	private void verifyWorkingCopy() throws IllegalStateException {
		IServerWorkingCopy wc = getWorkingCopy();
		if( wc == null )
			throw new IllegalStateException("This controller requires a server working-copy."); //$NON-NLS-1$
	}

	@Override
	public void setOutputName(IModule module, String name) throws IllegalStateException {
		setModuleDeploymentPreference(module, IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME, name);
	}

	@Override
	public void setDeployDirectory(IModule module, String directory) throws IllegalStateException {
		setModuleDeploymentPreference(module, IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, directory);
	}

	@Override
	public void setTemporaryDeployDirectory(IModule module, String directory) throws IllegalStateException {
		setModuleDeploymentPreference(module, IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC, directory);
	}
	
	protected void setModuleDeploymentPreference(IModule module, String key, String val) {
		verifyWorkingCopy();
		DeploymentPreferences prefs = DeploymentPreferencesLoader.loadPreferencesFromServer(getServerOrWC());
		prefs.setModulePreferenceValue(module, key, val);
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
		if(missingProperty(ENV_DEPLOYMENT_OPTIONS_CONTROLLER)) {
			if( missingProperty(ENV_DEFAULT_DEPLOY_FOLDER))
				return invalid();
			if( missingProperty(ENV_DEFAULT_TMP_DEPLOY_FOLDER))
				return invalid();
		}
		return Status.OK_STATUS;
	}
	private boolean missingProperty(String key) {
		return getEnvironment() == null || getEnvironment().get(key) == null;
	}
	private IStatus invalid() {
		return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The controller is invalid and is missing required properties"); //$NON-NLS-1$
	}
	
	
}
