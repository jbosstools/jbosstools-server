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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleRestartBehaviorController;

/**
 * This class stores and retrieves preferences for determining whether
 * a set of resources or deltas match a restart regex pattern.
 * 
 * If instantiated with a IServerWorkingCopy in the environment,
 * it will get and set the values from the working copy. 
 * 
 * If instantiated with only an IServer,  
 * setters may fail with an IllegalStateException, depending on the implementation.
 * 
 * The setters of this controller are not part of the interface
 */
public class StandardModuleRestartBehaviorController extends
		AbstractSubsystemController implements IModuleRestartBehaviorController {
	/**
	 * The property key used for storage of the pattern inside the server adapter
	 */
	public static final String PROPERTY_RESTART_FILE_PATTERN = "org.jboss.tools.as.restartFilePattern"; //$NON-NLS-1$

	/**
	 * The property key used for storage of the pattern inside the server adapter
	 */
	public static final String PROPERTY_USE_DEFAULT_RESTART_PATTERN = "org.jboss.tools.as.useDefaultRestartFilePattern"; //$NON-NLS-1$

	/**
	 * A default restart pattern
	 */
	public static final String RESTART_DEFAULT_FILE_PATTERN = "\\.jar$"; //$NON-NLS-1$
	
	/**
	 * The compiled default restart pattern to avoid multiple compilations
	 */
	protected static Pattern defaultRestartPattern = Pattern.compile(getDefaultModuleRestartPattern(),
			Pattern.CASE_INSENSITIVE);
	
	private static String getDefaultModuleRestartPattern() {
		return RESTART_DEFAULT_FILE_PATTERN;
	}
	/*
	 * The deployment options gives us access to things like
	 * where the deployment root dir for a server should be,
	 * or whether the server prefers zipped settings
	 */
	private IDeploymentOptionsController deploymentOptions;
	
	/*
	 * The deploy path controller helps us to discover
	 * a module's root deployment directory
	 */
	private IModuleDeployPathController deployPathController;
	
	private Pattern restartFilePattern;
	
	private boolean moduleRequiresRestart(IModule[] module) {
		// Just validate the module name itself
		// Find dependency will throw a CoreException if an object is not found, rather than return null
		try {
			IDeploymentOptionsController opts = getDeploymentOptions();
			IModuleDeployPathController depPath = getDeployPathController();
			IPath dest = new RemotePath(depPath.getDeployDirectory(module).toOSString(), 
					opts.getPathSeparatorCharacter());
			return testPath(dest);
		} catch(CoreException ce) {
			JBossServerCorePlugin.getDefault().log(ce);
		}
		return false;
	}
	
	@Override
	public boolean moduleRequiresRestart(IModule[] module,
			IModuleResource[] resourcesToTest) {
		loadPattern();
		// Check if the module name itself counts as something that must be force-restarted
		if( moduleRequiresRestart(module)) {
			return true;
		}
		boolean matches = false;
		for( int i = 0; i < resourcesToTest.length && !matches; i++) {
			matches |= testResource(resourcesToTest[i]);
		}
		return matches;
	}
	
	/**
	 * Get the system for deployment options such as zipped or not
	 * We must pass in a custom environment here. 
	 */
	protected IDeploymentOptionsController getDeploymentOptions() throws CoreException {
		if( deploymentOptions == null ) {
			deploymentOptions = (IDeploymentOptionsController)findDependencyFromBehavior(IDeploymentOptionsController.SYSTEM_ID);
		}
		return deploymentOptions;
	}
	
	/**
	 * get the system for deploy path for a given module
	 */
	protected IModuleDeployPathController getDeployPathController() throws CoreException {
		if( deployPathController == null ) {
			Map<String, Object> env = getEnvironment() == null ? new HashMap<String, Object>() : new HashMap<String, Object>(getEnvironment());
			env.put(IModuleDeployPathController.ENV_DEPLOYMENT_OPTIONS_CONTROLLER, getDeploymentOptions());
			deployPathController = (IModuleDeployPathController)findDependency(IModuleDeployPathController.SYSTEM_ID, getServer().getServerType().getId(), env);
		}
		return deployPathController;
	}

	private boolean testResource(IModuleResource r) {
		boolean matches = false;
		if( r instanceof IModuleFolder) {
			IModuleResource[] children = ((IModuleFolder)r).members();
			for( int i = 0; i < children.length && !matches; i++ ) {
				matches |= testResource(children[i]);
			}
		} else {
			IPath modRelativePath = r.getModuleRelativePath().append(r.getName());
			matches |= restartFilePattern.matcher(modRelativePath.toString()).find();
		}
		return matches;
	}
	
	private boolean testPath(IPath path) {
		return restartFilePattern.matcher(path.toString()).find();
	}

	@Override
	public boolean moduleRequiresRestart(IModule[] module,
			IModuleResourceDelta[] deltaToTest) {
		if( deltaToTest == null )
			return false;
		
		loadPattern();
		boolean matches = false;
		for( int i = 0; i < deltaToTest.length && !matches; i++ ) {
			matches |= testDelta(deltaToTest[i]);
		}
		return matches;
	}
	
	private boolean testDelta(IModuleResourceDelta delta) {
		boolean matches = false;
		IModuleResource r = delta.getModuleResource();
		IPath modRelativePath = r.getModuleRelativePath().append(r.getName());
		String modRelativePathString = modRelativePath.toString();
		matches |= restartFilePattern.matcher(modRelativePathString).find();
		IModuleResourceDelta[] children = delta.getAffectedChildren();
		if( children != null ) {
			for( int i = 0; i < children.length && !matches; i++ ) {
				matches |= testDelta(children[i]);
			}
		}
		return matches;
	}

	

	private void loadPattern(){
		if( restartFilePattern == null ) {
			String useDef = getServerOrWC().getAttribute(PROPERTY_USE_DEFAULT_RESTART_PATTERN, Boolean.TRUE.toString());
			if( !new Boolean(useDef).booleanValue()) {
				String currentPattern = getModuleRestartPattern();
				if( currentPattern != null ) {
					try {
						restartFilePattern = Pattern.compile(currentPattern, Pattern.CASE_INSENSITIVE);
						return;
					} catch(PatternSyntaxException pse) {
						JBossServerCorePlugin.log("Could not set restart file pattern to: " + currentPattern, pse); //$NON-NLS-1$
					}
				}
			}
			restartFilePattern = defaultRestartPattern;
		}
	}
	
	/*
	 * Non-interface methods for use by UI below
	 */
	
	public void setModuleRestartPattern(String pattern) {
		getWorkingCopy().setAttribute(PROPERTY_RESTART_FILE_PATTERN, pattern);
	}
	
	public String getModuleRestartPattern() {
		return getServerOrWC().getAttribute(PROPERTY_RESTART_FILE_PATTERN, (String)null);
	}
}
