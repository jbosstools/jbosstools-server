/*
 * JBoss, a division of Red Hat
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
package org.jboss.ide.eclipse.archives.core.model.other.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.IPreferenceManager;
import org.osgi.service.prefs.BackingStoreException;


/**
 * Sets default preferences for the plugin.
 * By default, the builder is enabled for all projects with archives.
 *
 * @author rstryker
 *
 */
public class WorkspacePreferenceManager extends AbstractPreferenceInitializer implements IPreferenceManager {
	public static final String AUTOMATIC_BUILDER_ENABLED = "org.jboss.ide.eclipse.archives.core.automaticBuilderEnabled";
	public static final String PROJECT_SPECIFIC_PREFS = "org.jboss.ide.eclipse.archives.core.projectSpecificPreferencesEnabled";

	private static IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	private static IResource getResource(IPath path) {
		if( path != null ) {
			IProject[] projects = workspaceRoot.getProjects();
			if( projects != null ) {
				for( int i = 0; i < projects.length; i++ ) {
					if( projects[i].getLocation().equals(path))
						return projects[i];
				}
			}
		}
		return null;
	}

	public boolean isBuilderEnabled(IPath path) {
		QualifiedName name = new QualifiedName(ArchivesCorePlugin.PLUGIN_ID, AUTOMATIC_BUILDER_ENABLED);
		IResource res = getResource(path);
		if( res != null && areProjectSpecificPrefsEnabled(res)) {
			try {
				if( res.getPersistentProperty(name) != null) {
					return Boolean.parseBoolean(res.getPersistentProperty(name));
				}
			} catch( CoreException ce ) {}
		}
		return new InstanceScope().getNode(ArchivesCorePlugin.PLUGIN_ID).getBoolean(AUTOMATIC_BUILDER_ENABLED, true);
	}

	public void setBuilderEnabled(IPath path, boolean value) {
		QualifiedName name = new QualifiedName(ArchivesCorePlugin.PLUGIN_ID, AUTOMATIC_BUILDER_ENABLED);
		IResource resource = getResource(path);
		// if the resource is null or the resource has no preference val, use global val
		try {
			if( resource != null && resource.getPersistentProperty(name) != null) {
				resource.setPersistentProperty(name, new Boolean(value).toString());
				return;
			}
		} catch( CoreException ce ) {}
		IEclipsePreferences prefs = new InstanceScope().getNode(ArchivesCorePlugin.PLUGIN_ID);
		prefs.putBoolean(AUTOMATIC_BUILDER_ENABLED, value);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			ArchivesCore.getInstance().getLogger().log(IStatus.ERROR, e.getMessage(), e);
		}
	}

	public void initializeDefaultPreferences() {
		IEclipsePreferences prefs = new DefaultScope().getNode(ArchivesCorePlugin.PLUGIN_ID);
		prefs.putBoolean(AUTOMATIC_BUILDER_ENABLED, true);
		try {
			prefs.flush();
		} catch (BackingStoreException e1) {
			ArchivesCore.getInstance().getLogger().log(IStatus.ERROR, e1.getMessage(), e1);
		}
	}

	public boolean areProjectSpecificPrefsEnabled(IPath path) {
		return areProjectSpecificPrefsEnabled(getResource(path));
	}
	public boolean areProjectSpecificPrefsEnabled(IResource resource) {
		QualifiedName name = new QualifiedName(ArchivesCorePlugin.PLUGIN_ID, PROJECT_SPECIFIC_PREFS);
		try {
			if( resource != null && resource.getPersistentProperty(name) != null) {
				return Boolean.parseBoolean(resource.getPersistentProperty(name));
			}
		} catch( CoreException ce ) {}
		return false;
	}

	public void setProjectSpecificPrefsEnabled(IPath path, boolean value) {
		QualifiedName name = new QualifiedName(ArchivesCorePlugin.PLUGIN_ID, PROJECT_SPECIFIC_PREFS);
		IResource resource = getResource(path);
		try {
			if( resource != null) {
				resource.setPersistentProperty(name, new Boolean(value).toString());
				return;
			}
		} catch( CoreException ce ) {}
	}
}
