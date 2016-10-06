/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.server.launch;

import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.IUserPrompter;
import org.jboss.ide.eclipse.as.core.server.UserPrompter;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;

/**
 * A standard hotcode replace listener for servers which will use the userprompt
 * api to present options to the user on how to handle this.
 * 
 * This class is usable only for servers that are part of the ControllableServer
 * heirarchy
 */
public class ServerHotCodeReplaceListener implements IJavaHotCodeReplaceListener {

	/**
	 * Prompt code that will pass 3 arguments: code, server, and exception (or
	 * null) The result of this prompt must be an integer indicating a suggested
	 * action to take
	 */
	public static final int EVENT_HCR_FAIL = 1013;
	/**
	 * Prompt code that will pass 3 arguments: code, server, and exception (or
	 * null) The result of this prompt must be an integer indicating a suggested
	 * action to take
	 */
	public static final int EVENT_HCR_OBSOLETE = 1014;

	/**
	 * The setting on whether or not to override hotcode replace
	 */
	public static final String PROPERTY_HOTCODE_REPLACE_OVERRIDE = "org.jboss.ide.eclipse.as.wtp.core.server.launch.OverrideHotCodeReplace";

	/**
	 * The key representing your new behavior on hotcode failures
	 */
	public static final String PROPERTY_HOTCODE_BEHAVIOR = "org.jboss.ide.eclipse.as.wtp.core.server.launch.hotCodeReplace";

	public static final int PROMPT = 0;
	public static final int RESTART_MODULE = 1;
	public static final int RESTART_SERVER = 2;
	public static final int CONTINUE = 3;
	public static final int TERMINATE = 4;

	private IServer server;
	private ILaunch launch;
	public ServerHotCodeReplaceListener(IServer server, ILaunch launch) {
		this.server = server;
		this.launch = launch;
	}

	protected IServer getServer() {
		return server;
	}
	
	protected ILaunch getLaunch() {
		return launch;
	}
	
	public void hotCodeReplaceSucceeded(IJavaDebugTarget arg0) {
		// ignore
	}

	public void hotCodeReplaceFailed(IJavaDebugTarget target, DebugException exception) {
		hotCodeError(EVENT_HCR_FAIL, target, exception);
	}

	public void obsoleteMethods(IJavaDebugTarget target) {
		hotCodeError(EVENT_HCR_OBSOLETE, target, null);
	}

	protected void hotCodeError(int code, IJavaDebugTarget target, Exception exception) {
		int behavior = server.getAttribute(PROPERTY_HOTCODE_BEHAVIOR, PROMPT);
		if (behavior == PROMPT) {
			Object result = getPrompter().promptUser(code, server, exception);
			if (result instanceof Integer) {
				behavior = ((Integer) result).intValue();
			}
		}
		if (behavior == RESTART_MODULE) {
			restartModules(target);
		} else if (behavior == RESTART_SERVER) {
			restartServer();
		} else if (behavior == TERMINATE) {
			server.stop(true);
		}
		// Continue means ignore
	}

	protected void restartModules(final IJavaDebugTarget target) {

		final IModule[] modules = server.getModules();
		IServer.IOperationListener listener = new IServer.IOperationListener() {

			public void done(IStatus result) {
				postPublish(target, modules);
			}
		};
		server.publish(IServer.PUBLISH_FULL, Collections.singletonList(modules), null, listener);
	}

	/**
	 * Subclasses can override
	 * @param target 
	 * @param server
	 * @param modules
	 */
	protected void postPublish(IJavaDebugTarget target, IModule[] modules) {
	}
	
	/**
	 * Currently unused, but used by subclasses
	 * @param modules
	 */
	protected void waitModulesStarted(IModule[] modules) {
		IControllableServerBehavior controllableBehaviour = (IControllableServerBehavior) server
				.loadAdapter(IControllableServerBehavior.class, new NullProgressMonitor());
		if (controllableBehaviour != null) {
			IServerModuleStateVerifier verifier;
			try {
				verifier = (IServerModuleStateVerifier) controllableBehaviour
						.getController(IControllableServerBehavior.SYSTEM_MODULES);
				if (verifier != null) {
					// we can verify the remote state, so go do it, so
					// go wait for the module to be deployed
					
					// TODO:  'modules' is an array of top-level modules, not a single module tree. 
					// Do we need to perform a for-loop here?
					verifier.waitModuleStarted(server, modules, 20000);
				}
			} catch (CoreException e) {
				ASWTPToolsPlugin.pluginLog().logError("Error waiting for modules to start after publish", e);
			}
		}
	}
	
	protected void restartServer() {
		server.restart(server.getMode(), new IServer.IOperationListener() {
			public void done(IStatus result) {
				// Ignore
			}
		});
	}

	/**
	 * Clients can override this method for custom prompt behavior
	 * 
	 * @return
	 */
	public IUserPrompter getPrompter() {
		return UserPrompter.getDefaultPrompter();
	}
}
