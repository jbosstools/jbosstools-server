/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.archives.core;

import org.eclipse.core.runtime.IPath;
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

	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.archives.core";
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
}
