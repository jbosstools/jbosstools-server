/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.InternalRuntimeClasspathModel;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.RuntimeClasspathModelIO;
import org.jboss.ide.eclipse.as.classpath.core.runtime.internal.DefaultClasspathModelLoader;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

public class CustomRuntimeClasspathModel {
	/**
	 * A location to store these default settings.
	 * Use of the term 'filesets' is legacy but must be maintained. 
	 * 
	 * Each runtime in {metadata}/org.jboss.ide.eclipse.as.server.core/filesets/runtimeClasspaths 
	 * gets its own file.  This allows the format to change (if required) for newer runtime types,
	 * if such a thing is needed. 
	 * 
	 * 
	 */
	protected static final IPath DEFAULT_CLASSPATH_FS_ROOT = JBossServerCorePlugin.getGlobalSettingsLocation().append("filesets").append("runtimeClasspaths"); //$NON-NLS-1$ //$NON-NLS-2$

	private static CustomRuntimeClasspathModel instance;
	public static CustomRuntimeClasspathModel getInstance() {
		if( instance == null )
			instance = new CustomRuntimeClasspathModel();
		return instance;
	}
	 
	private HashMap<String, InternalRuntimeClasspathModel> modelMap;
	private CustomRuntimeClasspathModel() {
		modelMap = new HashMap<String, InternalRuntimeClasspathModel>();
	}
	
	/**
	 * @since 3.0
	 */
	public IRuntimePathProvider[] getEntries(IRuntimeType type) {
		if( type == null ) {
			return new IRuntimePathProvider[0];
		}
		String id = type.getId();
		if( modelMap.get(id) == null ) {
			// Has not been read yet
			InternalRuntimeClasspathModel model = new RuntimeClasspathModelIO().readModel(type);
			if( model != null ) {
				modelMap.put(id,  model);
			} else {
				// Create a default one somehow
			}
		}
		InternalRuntimeClasspathModel model = modelMap.get(id);
		if( model != null ) {
			return model.getStandardProviders();
		}
		return getDefaultEntries(type);
	}
	
	/**
	 * Used by UI for restore-defaults... ... 
	 * UI needs to be updated to be able to handle a full model, 
	 * not just a single set of entries. 
	 * 
	 * @since 3.0
	 */
	public IRuntimePathProvider[] getDefaultEntries(IRuntimeType type) {
		InternalRuntimeClasspathModel model = new DefaultClasspathModelLoader().getDefaultRuntimeClasspathModel(type);
		return model.getStandardProviders();
	}
	
	public static void savePathProviders(IRuntimeType runtime, IRuntimePathProvider[] sets) {
		InternalRuntimeClasspathModel m = new InternalRuntimeClasspathModel();
		m.addProviders(sets);
		new RuntimeClasspathModelIO().saveModel(runtime, m);
	}
}
