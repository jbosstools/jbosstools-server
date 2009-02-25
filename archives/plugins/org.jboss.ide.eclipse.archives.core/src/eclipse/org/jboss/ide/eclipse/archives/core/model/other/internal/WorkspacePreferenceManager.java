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
	public static final String AUTOMATIC_BUILDER_ENABLED = "org.jboss.ide.eclipse.archives.core.automaticBuilderEnabled"; //$NON-NLS-1$
	public static final String PROJECT_SPECIFIC_PREFS = "org.jboss.ide.eclipse.archives.core.projectSpecificPreferencesEnabled"; //$NON-NLS-1$

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

	
	public boolean shouldBuild(IPath path) {
		if( !ResourcesPlugin.getWorkspace().isAutoBuilding())
			return false;
		return isArchivesBuilderEnabled(path);
	}
	
	public boolean isArchivesBuilderEnabled(IPath path) {
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

	public void setArchivesBuilderEnabled(IPath path, boolean value) {
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
