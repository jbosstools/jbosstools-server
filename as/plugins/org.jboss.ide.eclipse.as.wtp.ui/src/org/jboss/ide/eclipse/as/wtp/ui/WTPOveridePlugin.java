/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.ide.eclipse.as.core.server.UserPrompter;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.ServerHotCodeReplaceListener;
import org.jboss.ide.eclipse.as.wtp.ui.prompt.ServerAlreadyStartedPrompter;
import org.jboss.ide.eclipse.as.wtp.ui.prompt.ServerHotCodeReplacePrompter;
import org.jboss.ide.eclipse.as.wtp.ui.prompt.ZombieProcessPrompter;
import org.jboss.tools.foundation.ui.plugin.BaseUISharedImages;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class WTPOveridePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.wtp.ui"; //$NON-NLS-1$
	
	// The shared instance
	private static WTPOveridePlugin plugin;
	private BaseUISharedImages sharedImages = null;

	/**
	 * The constructor
	 */
	public WTPOveridePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		UserPrompter.getDefaultPrompter().addPromptHandler(UserPrompter.EVENT_CODE_SERVER_ALREADY_STARTED, new ServerAlreadyStartedPrompter());
		UserPrompter.getDefaultPrompter().addPromptHandler(UserPrompter.EVENT_CODE_PROCESS_UNTERMINATED, new ZombieProcessPrompter());
		ServerHotCodeReplacePrompter hcrPrompt = new ServerHotCodeReplacePrompter();
		UserPrompter.getDefaultPrompter().addPromptHandler(ServerHotCodeReplaceListener.EVENT_HCR_FAIL, hcrPrompt);
		UserPrompter.getDefaultPrompter().addPromptHandler(ServerHotCodeReplaceListener.EVENT_HCR_OBSOLETE, hcrPrompt);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static WTPOveridePlugin getInstance() {
		return plugin;
	}

	public static void log(Exception e) {
		log(e.getMessage(), e);
	}
	
	public static void log(String message, Exception e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		getInstance().getLog().log(status);
	}
	public static IStatus createStatus(int severity, int aCode,
			String aMessage, Throwable exception) {
		return new Status(severity, PLUGIN_ID, aCode,
				aMessage != null ? aMessage : "No message.", exception); //$NON-NLS-1$
	}
	public static IStatus createErrorStatus(String message) {
		return createErrorStatus(message, null);
	}
	public static IStatus createWarningStatus(String message) {
		return createWarningStatus(message, null);
	}
	public static IStatus createErrorStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, -1, message, exception);
	}
	public static IStatus createWarningStatus(String message, Throwable exception) {
		return new Status(IStatus.WARNING, PLUGIN_ID, -1, message, exception);
	}
	public static IStatus createErrorStatus(int aCode, String aMessage,
			Throwable exception) {
		return createStatus(IStatus.ERROR, aCode, aMessage, exception);
	}

	public static IStatus createStatus(int severity, String message, Throwable exception) {
		return new Status(severity, PLUGIN_ID, message, exception);
	}

	public static IStatus createStatus(int severity, String message) {
		return createStatus(severity, message, null);
	}
	public static void logError(Throwable exception) {
		Platform.getLog(Platform.getBundle(PLUGIN_ID)).log( createStatus(IStatus.ERROR, exception.getMessage(), exception));
	}

	public static void logError(CoreException exception) {
		Platform.getLog(Platform.getBundle(PLUGIN_ID)).log( exception.getStatus() );
	}

	public BaseUISharedImages getSharedImages() {
		if( sharedImages == null ) {
			sharedImages = createSharedImages();
			
		}
		return sharedImages;
	}

	public static final String JMX_IMG = "icons/jmeth_obj.gif";
	
	public static final String STDOUT_IMG = "icons/stdout.gif";
	
	protected BaseUISharedImages createSharedImages() {
		return new ASWTPSharedImages(getBundle());
	}
	
	private static class ASWTPSharedImages extends BaseUISharedImages {
		public ASWTPSharedImages(Bundle pluginBundle) {
			super(pluginBundle);
			addImage(JMX_IMG, JMX_IMG);
			addImage(STDOUT_IMG, STDOUT_IMG);
		}
	}
}
