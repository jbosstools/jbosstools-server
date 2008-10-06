package org.jboss.ide.eclipse.archives.webtools;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String ExceptionCannotScanDirectory;
	public static String ExceptionUnexpectedException;
	public static String ExceptionCannotDeployFile;
	public static String ErrorDuringPublish;
	public static String ArchivePublishSettings;
	public static String SelectServerWizard;
	public static String SelectServerWizardDescription;
	public static String SelectServerWizardTitle;
	public static String AlwaysPublishToTheseServers;
	public static String AutoDeployAfterBuild;
	public static String DeleteFiles;
	public static String DeleteFilesMessage;

	/* Filesets addition */
	public static String FilesetsCreateFilter;
	public static String FilesetsDeleteFilter;
	public static String FilesetsEditFilter;
	public static String FilesetsDeleteFile;
	public static String FilesetsEditFile;
	public static String FilesetsNewFileset;
	public static String FilesetsNewName;
	public static String FilesetsNewRootDir;
	public static String FilesetsNewBrowse;
	public static String FilesetsNewIncludes;
	public static String FilesetsNewExcludes;
	public static String FilesetsNewPreview;
	public static String FilesetsCannotOpenFile;
	public static String FilesetsDialogTitle;
	public static String FilesetsDialogMessage;


	public static String NewEar;
	public static String EarPreview;
	public static String EarDescription;
	public static String NewWar;
	public static String WarPreview;
	public static String WarDescription;
	public static String NewEjbJar;
	public static String Preview;
	public static String BuildArchive;
	public static String EjbJarPreview;
	public static String EjbJarDescription;


	static {
		initializeMessages("org.jboss.ide.eclipse.archives.webtools.Messages", Messages.class); //$NON-NLS-1$
	}
}
