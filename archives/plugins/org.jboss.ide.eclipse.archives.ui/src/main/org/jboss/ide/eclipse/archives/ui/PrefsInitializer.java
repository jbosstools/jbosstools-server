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
package org.jboss.ide.eclipse.archives.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;

public class PrefsInitializer extends AbstractPreferenceInitializer {


	// preference keys
	public static final String PREF_SHOW_PACKAGE_OUTPUT_PATH = "showPackageOutputPath"; //$NON-NLS-1$
	public static final String PREF_SHOW_FULL_FILESET_ROOT_DIR = "showFullFilesetRootDir";//$NON-NLS-1$
	public static final String PREF_SHOW_PROJECT_ROOT = "showProjectRoot";//$NON-NLS-1$
	public static final String PREF_SHOW_ALL_PROJECTS = "showAllProjects";//$NON-NLS-1$
	public static final String PREF_SHOW_BUILD_ERROR_DIALOG = "showBuildErrorDialog";//$NON-NLS-1$
	public static final ArrayList<IArchivesPreferenceListener> listeners = new ArrayList<IArchivesPreferenceListener>();

	public static interface IArchivesPreferenceListener {
		public void preferenceChanged(String key, boolean val);
	}

	public void initializeDefaultPreferences() {
		IEclipsePreferences prefs = new DefaultScope().getNode(PackagesUIPlugin.PLUGIN_ID);
		prefs.putBoolean(PREF_SHOW_FULL_FILESET_ROOT_DIR, true);
		prefs.putBoolean(PREF_SHOW_PACKAGE_OUTPUT_PATH, true);
		prefs.putBoolean(PREF_SHOW_PROJECT_ROOT, true);
		prefs.putBoolean(PREF_SHOW_ALL_PROJECTS, false);
		prefs.putBoolean(PREF_SHOW_BUILD_ERROR_DIALOG, true);
		try {
			prefs.flush();
		} catch (org.osgi.service.prefs.BackingStoreException e) {
			e.printStackTrace();
		} // swallow
	}

	public static void setBoolean(String key, boolean val) {
		setBoolean(key, val, null);
	}

	public static void setBoolean(String key, boolean val, IAdaptable adaptable) {
		QualifiedName name = new QualifiedName(PackagesUIPlugin.PLUGIN_ID, key);
		if( adaptable != null ) {
			IResource project = (IResource)adaptable.getAdapter(IResource.class);
			try {
				if( project != null && project.getPersistentProperty(name) != null) {
					project.setPersistentProperty(name, new Boolean(val).toString());
					return;
				}
			} catch(CoreException ce) {}
		}
		IEclipsePreferences prefs = new InstanceScope().getNode(PackagesUIPlugin.PLUGIN_ID);
		prefs.putBoolean(key, val);
		try {
			prefs.flush();
		} catch (org.osgi.service.prefs.BackingStoreException e) { } // swallow
		fireChanged(key, val);
	}

	protected static void fireChanged(String key, boolean val) {
		Iterator<IArchivesPreferenceListener> i = listeners.iterator();
		while(i.hasNext()) {
			i.next().preferenceChanged(key, val);
		}
	}

	public static void addListener(IArchivesPreferenceListener listener) {
		if( !listeners.contains(listener))
			listeners.add(listener);
	}

	public static void removeListener(IArchivesPreferenceListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Get the global pref value for this key
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(String key) {
		return getBoolean(key, null, true);
	}

	/**
	 *
	 * @param key  the preference to be gotten
	 * @param adaptable  the project / resource where the pref might be stored
	 * @param effective  whether or not to get the raw pref value or the effective value
	 * 					(based on whether project specific prefs are turned on)
	 * @return
	 */
	public static boolean getBoolean(String key, IAdaptable adaptable, boolean effective) {
		QualifiedName name = new QualifiedName(PackagesUIPlugin.PLUGIN_ID, key);
		if( adaptable != null ) {
			IResource project = (IResource)adaptable.getAdapter(IResource.class);
			boolean specific = ArchivesCore.getInstance().getPreferenceManager().areProjectSpecificPrefsEnabled(project.getLocation());
			//if( adaptable != null && WorkspacePreferenceManager.areProjectSpecificPrefsEnabled(project.getLocation())) {
			if( specific ) {
				try {
					if( project != null && project.getPersistentProperty(name) != null) {
						return Boolean.parseBoolean(project.getPersistentProperty(name));
					}
				} catch(CoreException ce) {}
			}
		}
		boolean defaultVal = new DefaultScope().getNode(PackagesUIPlugin.PLUGIN_ID).getBoolean(key, false);
		return new InstanceScope().getNode(PackagesUIPlugin.PLUGIN_ID).getBoolean(key, defaultVal);
	}
}
