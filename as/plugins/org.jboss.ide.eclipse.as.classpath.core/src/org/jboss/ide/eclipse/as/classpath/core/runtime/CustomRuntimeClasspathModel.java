/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.filesets.Fileset;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetUtil;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.internal.Messages;
import org.jboss.ide.eclipse.as.classpath.core.runtime.internal.DefaultClasspathJarLocator;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.XMLMemento;

public class CustomRuntimeClasspathModel {
	protected static final IPath DEFAULT_CLASSPATH_FS_ROOT = JBossServerCorePlugin.getGlobalSettingsLocation().append("filesets").append("runtimeClasspaths"); //$NON-NLS-1$ //$NON-NLS-2$

	private static CustomRuntimeClasspathModel instance;
	public static CustomRuntimeClasspathModel getInstance() {
		if( instance == null )
			instance = new CustomRuntimeClasspathModel();
		return instance;
	}
	 
	/**
	 * @since 2.5
	 */
	public IRuntimePathProvider[] getEntries(IRuntimeType type) {
		IRuntimePathProvider[] sets = loadFilesets(type);
		if( sets == null ) {
			return getDefaultEntries(type);
		}
		return sets;
	}
	
	/**
	 * @since 2.5
	 */
	public IRuntimePathProvider[] getDefaultEntries(IRuntimeType type) {
		return new DefaultClasspathJarLocator().getDefaultPathProviders(type);
	}
	
	public IPath[] getDefaultPaths(IRuntime rt) {
		return getAllEntries(rt, getDefaultEntries(rt.getRuntimeType()));
	}

	/**
	 * @since 2.5
	 */
	public IPath[] getAllEntries(IRuntime runtime, IRuntimePathProvider[] sets) {
		return DefaultClasspathJarLocator.getAllEntries(runtime, sets);
	}
	
	/*
	 * Persistance of the model
	 */
	
	private static IRuntimePathProvider[] loadFilesets(IRuntimeType rt) {
		IPath fileToRead = DEFAULT_CLASSPATH_FS_ROOT.append(rt.getId());
		Fileset[] sets = loadFilesets(fileToRead.toFile(), null);
		if( sets != null ) {
			RuntimePathProviderFileset[] newSets = new RuntimePathProviderFileset[sets.length];
			for( int i = 0; i < sets.length; i++ ) {
				newSets[i] = new RuntimePathProviderFileset(sets[i]);
			}
			return newSets;
		}
		return null;
	}
	
	/**
	 * Return a list of filesets, or null if none are found
	 * @param file
	 * @param server
	 * @deprecated This method should be private. 
	 * @return
	 */
	public static Fileset[] loadFilesets(File file, IServer server) {
		try {
			if( file != null && file.exists())
				return FilesetUtil.loadFilesets(new FileInputStream(file), server);
		} catch( FileNotFoundException fnfe) {
			return null;
		}
		return null;
	}

	/**
	 * @since 2.5
	 */
	public static void saveFilesets(IRuntimeType runtime, IRuntimePathProvider[] sets) {
		if( !DEFAULT_CLASSPATH_FS_ROOT.toFile().exists()) {
			DEFAULT_CLASSPATH_FS_ROOT.toFile().mkdirs();
		}
		IPath fileToWrite = DEFAULT_CLASSPATH_FS_ROOT.append(runtime.getId());
		XMLMemento memento = XMLMemento.createWriteRoot("classpathProviders"); //$NON-NLS-1$
		for( int i = 0; i < sets.length; i++ ) {
			if( sets[i] instanceof Fileset) {
				Fileset fs = (Fileset)sets[i];
				XMLMemento child = (XMLMemento)memento.createChild("fileset");//$NON-NLS-1$
				child.putString("name", fs.getName());//$NON-NLS-1$
				child.putString("folder", fs.getRawFolder());//$NON-NLS-1$
				child.putString("includes", fs.getIncludesPattern());//$NON-NLS-1$
				child.putString("excludes", fs.getExcludesPattern());//$NON-NLS-1$	
			} else {
				// TODO
			}
		}
		try {
			memento.save(new FileOutputStream(fileToWrite.toFile()));
		} catch( IOException ioe) {
			IStatus status = new Status(IStatus.ERROR, ClasspathCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.CouldNotSaveDefaultClasspathEntries, runtime.getId()), ioe);
			ClasspathCorePlugin.getDefault().getLog().log(status);
		}
	}
}
