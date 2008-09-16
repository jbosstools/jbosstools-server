package org.jboss.ide.eclipse.archives.core.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.build.ModelChangeListener;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IActionType;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.IArchivesVFS;
import org.jboss.ide.eclipse.archives.core.model.IExtensionManager;
import org.jboss.ide.eclipse.archives.core.model.IPreferenceManager;

/**
 * A core API entry point for ant.
 * @author rob stryker (rob.stryker@redhat.com)
 *
 */
public class AntArchivesCore extends ArchivesCore {
	private Project currentProject;
	private Task currentTask;
	public AntArchivesCore () {
		super(STANDALONE);
		ArchivesModel.instance().addModelListener(new ModelChangeListener());
	}
	
	public void setProject(Project p) {
		currentProject = p;
	}
	
	public Project getProject() {
		return currentProject;
	}
	
	public void setTask(Task t) {
		currentTask= t;
	}
	
	public Task getTask() {
		return currentTask;
	}
	protected IExtensionManager createExtensionManager() {
		return new AntExtensionManager();
	}

	protected IPreferenceManager createPreferenceManager() {
		return new AntPreferences();
	}

	protected IArchivesVFS createVFS() {
		return new AntVFS();
	}

	public void preRegisterProject(IPath project) {
		// do nothing
	}

	protected IArchivesLogger createLogger() {
		return new AntLogger();
	}
	
	protected class AntExtensionManager implements IExtensionManager {
		public IActionType getActionType(String id) {
			return null;
		}

		public IArchiveType getArchiveType(String id) {
			final String id2 = id;
			return new IArchiveType() {
				public IArchive createDefaultConfiguration(String projectName,
						IProgressMonitor monitor) {
					return null;
				}
				public IArchive fillDefaultConfiguration(String projectName,
						IArchive topLevel, IProgressMonitor monitor) {
					return null;
				}
				public String getId() {
					return id2;
				}
				public String getLabel() {
					return id2;
				} 
			};
		}

		public IActionType[] getActionTypes() {
			return new IActionType[]{};
		}

		public IArchiveType[] getArchiveTypes() {
			return new IArchiveType[]{};
		}
	}
	
	protected class AntPreferences implements IPreferenceManager {

		public boolean areProjectSpecificPrefsEnabled(IPath path) {
			return false;
		}

		public boolean isBuilderEnabled(IPath path) {
			return true;
		}

		public void setBuilderEnabled(IPath path, boolean val) {
			// not implemented
		}

		public void setProjectSpecificPrefsEnabled(IPath path, boolean val) {
			// not implemented
		}
	}
	protected class AntLogger implements IArchivesLogger {
		public void log(int severity, String message, Throwable throwable) {
			currentProject.log(message, throwable, severity);
			if( throwable != null && severity == IArchivesLogger.MSG_ERR)
				throwable.printStackTrace();
		}
	}
}
