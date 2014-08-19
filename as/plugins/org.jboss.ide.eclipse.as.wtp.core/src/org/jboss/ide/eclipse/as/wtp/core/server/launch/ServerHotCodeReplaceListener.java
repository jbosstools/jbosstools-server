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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IUserPrompter;
import org.jboss.ide.eclipse.as.core.server.UserPrompter;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.FullPublishJobScheduler;

/**
 * A standard hotcode replace listener for servers which will use the userprompt
 * api to present options to the user on how to handle this. 
 * 
 * This class is usable only for servers that are part of the ControllableServer heirarchy
 */
public class ServerHotCodeReplaceListener implements IJavaHotCodeReplaceListener {

	/**
	 * Prompt code that will pass 3 arguments: code, server, and exception (or null)
	 * The result of this prompt must be an integer indicating a suggested action to take
	 */
	public static final int EVENT_HCR_FAIL = 1013;
	/**
	 * Prompt code that will pass 3 arguments: code, server, and exception (or null)
	 * The result of this prompt must be an integer indicating a suggested action to take
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
	public ServerHotCodeReplaceListener(IServer server, ILaunch launch) {
		this.server = server;
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
		if( behavior == PROMPT ) { 
			Object result = getPrompter().promptUser(code, server, exception);
			if( result instanceof Integer) {
				behavior = ((Integer)result).intValue();
			}
		}
		if( behavior == RESTART_MODULE) {
			restartModules();
		} else if( behavior == RESTART_SERVER) {
			restartServer();
		} else if( behavior == TERMINATE ) {
			server.stop(true);
		}
		// Continue means ignore
	}
	
	
	protected void restartModules() {
		new FullPublishJobScheduler(server, server.getModules()).schedule();
	}
	
	protected void restartServer() {
		server.restart(server.getMode(), new IServer.IOperationListener(){
			public void done(IStatus result) {
				// Ignore
			}});
	}
	
	/**
	 * Clients can override this method for custom prompt behavior
	 * @return
	 */
	public IUserPrompter getPrompter() {
		return UserPrompter.getDefaultPrompter();
	}
}
