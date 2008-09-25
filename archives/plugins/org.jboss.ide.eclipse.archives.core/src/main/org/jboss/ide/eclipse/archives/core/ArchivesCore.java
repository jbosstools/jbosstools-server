/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.IExtensionManager;
import org.jboss.ide.eclipse.archives.core.model.IPreferenceManager;
import org.jboss.ide.eclipse.archives.core.model.IArchivesVFS;

/**
 * The core entry point for Archives
 * @author rob.stryker@redhat.com
 *
 */
public abstract class ArchivesCore {

	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.archives.core"; //$NON-NLS-1$
	private static ArchivesCore instance;
	// Due to classloader restrictions we won't be able to lazy load, but that should be ok as long
	// as we keep the construction of ArchivesCore subclasses to a minimum
	public static ArchivesCore getInstance() {
		return instance;
	}

	public static void setInstance(ArchivesCore instance) {
		ArchivesCore.instance = instance;
	}


	public static final int STANDALONE = 0;
	public static final int WORKSPACE = 1;

	private int runType;
	private IArchivesVFS vfs;
	private IExtensionManager extensionManager;
	private IPreferenceManager preferenceManager;
	private IArchivesLogger logger;

	public ArchivesCore(int runType) {
		this.runType = runType;
		vfs = createVFS();
		extensionManager = createExtensionManager();
		preferenceManager = createPreferenceManager();
		logger = createLogger();
	}

	protected abstract IArchivesVFS createVFS();
	protected abstract IExtensionManager createExtensionManager();
	protected abstract IPreferenceManager createPreferenceManager();
	protected abstract IArchivesLogger createLogger();

	public int getRunType() {
		return runType;
	}
	public IArchivesVFS getVFS() {
		return vfs;
	}
	public IExtensionManager getExtensionManager() {
		return extensionManager;
	}
	public IPreferenceManager getPreferenceManager() {
		return preferenceManager;
	}
	public IArchivesLogger getLogger() {
		return logger;
	}

	public abstract void preRegisterProject(IPath project);

	protected abstract String bind2(String message, Object[] bindings);



	// Static convenience methods
	public static String bind(String message, Object[] bindings) {
		return ArchivesCore.getInstance().bind2(message, bindings);
	}
	public static String bind(String message, String binding1) {
		return ArchivesCore.getInstance().bind2(message, new Object[]{binding1});
	}
	public static String bind(String message, String binding1, String binding2) {
		return ArchivesCore.getInstance().bind2(message, new Object[]{binding1, binding2});
	}

	public static void log(IStatus status) {
		ArchivesCore.getInstance().getLogger().log(status);
	}
	public static void log(int severity, String message,Throwable throwable) {
		ArchivesCore.getInstance().getLogger().log(severity, message, throwable);
	}

}
