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
package org.jboss.ide.eclipse.archives.webtools.filesets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IMemento;
import org.jboss.ide.eclipse.as.core.util.XMLMemento;

public class FilesetUtil {
	protected static final String FILESET_FILE_NAME = "filesets.xml"; //$NON-NLS-1$
	protected static IPath DEFAULT_FS_ROOT = JBossServerCorePlugin.getGlobalSettingsLocation().append("filesets").append("default"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static String DEFAULT_FS_ALL_SERVERS = "org.jboss.ide.eclipse.archives.webtools.ui.allServers"; //$NON-NLS-1$
	public static Fileset[] loadFilesets(IServer server) {
		return loadFilesets(getFile(server), server);
	}
	
	/**
	 * Must return a list of filesets. NOT NULL
	 * @param file
	 * @param server
	 * @return
	 */
	public static Fileset[] loadFilesets(File file, IServer server) {
		if( file == null || !file.exists()) 
			return new Fileset[0];
		try {
			return loadFilesets(new FileInputStream(file), server);
		} catch( FileNotFoundException fnfe) {
			return new Fileset[0];
		}
	}
	
	/**
	 * May return null, or a list of filesets
	 * @param is
	 * @param server
	 * @return
	 */
	public static Fileset[] loadFilesets(InputStream is, IServer server) {
		Fileset[] sets = loadFilesets(is);
		if( sets != null ) {
			for( int i = 0; i < sets.length; i++ ) {
				sets[i].setServer(server);
			}
		}
		return sets;
	}
	
	/**
	 * may return null
	 * @param is
	 * @return
	 */
	public static Fileset[] loadFilesets(InputStream is) {
		Fileset[] filesets = null;
		XMLMemento memento = XMLMemento.createReadRoot(is);
		IMemento[] categoryMementos = memento.getChildren("fileset");//$NON-NLS-1$
		filesets = new Fileset[categoryMementos.length];
		String name, folder, includes, excludes;
		for( int i = 0; i < categoryMementos.length; i++ ) {
			name = categoryMementos[i].getString("name"); //$NON-NLS-1$
			folder = categoryMementos[i].getString("folder");//$NON-NLS-1$
			includes = categoryMementos[i].getString("includes");//$NON-NLS-1$
			excludes = categoryMementos[i].getString("excludes");//$NON-NLS-1$
			filesets[i] = new Fileset(name, folder, includes, excludes);
		}
		return filesets;
	}
	
	public static void saveFilesets(IServer server, Fileset[] sets) {
		saveFilesets(getFile(server), sets);
	}
	
	public static void saveFilesets(File file, Fileset[] sets) {
		if( file != null ) {
			file.getParentFile().mkdirs();
			XMLMemento memento = XMLMemento.createWriteRoot("filesets"); //$NON-NLS-1$
			for( int i = 0; i < sets.length; i++ ) {
				XMLMemento child = (XMLMemento)memento.createChild("fileset");//$NON-NLS-1$
				child.putString("name", sets[i].getName());//$NON-NLS-1$
				child.putString("folder", sets[i].getRawFolder());//$NON-NLS-1$
				child.putString("includes", sets[i].getIncludesPattern());//$NON-NLS-1$
				child.putString("excludes", sets[i].getExcludesPattern());//$NON-NLS-1$	
			}
			try {
				memento.save(new FileOutputStream(file));
			} catch( IOException ioe) {
				IntegrationPlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, ioe.getMessage(), ioe)
				);
			}
		}
	}

	
	public static File getFile(IServer server) {
		return JBossServerCorePlugin.getServerStateLocation(server)
			.append(FILESET_FILE_NAME).toFile();
	}

}
