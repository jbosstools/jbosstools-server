package org.jboss.ide.eclipse.archives.webtools.filesets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.tools.jmx.core.IMemento;
import org.jboss.tools.jmx.core.util.XMLMemento;

public class FilesetUtil {
	protected static final String FILESET_FILE_NAME = "filesets.xml"; //$NON-NLS-1$
	protected static IPath DEFAULT_FS_ROOT = JBossServerCorePlugin.getGlobalSettingsLocation().append("filesets").append("default"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static String DEFAULT_FS_ALL_SERVERS = "org.jboss.ide.eclipse.archives.webtools.ui.allServers"; //$NON-NLS-1$
	public static Fileset[] loadFilesets(IServer server) {
		return loadFilesets(getFile(server), server);
	}
	
	public static Fileset[] loadFilesets(File file, IServer server) {
		if( file != null && file.exists()) {
			try {
				return loadFilesets(new FileInputStream(file), server);
			} catch( FileNotFoundException fnfe) {}
		}
		return new Fileset[]{};
	}
	
	public static Fileset[] loadFilesets(InputStream is, IServer server) {
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
			filesets[i].setServer(server);
		}
		return filesets == null ? new Fileset[] { } : filesets;
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
				// TODO LOG
			}
		}
	}

	
	public static File getFile(IServer server) {
		return JBossServerCorePlugin.getServerStateLocation(server)
			.append(FILESET_FILE_NAME).toFile();
	}

}
