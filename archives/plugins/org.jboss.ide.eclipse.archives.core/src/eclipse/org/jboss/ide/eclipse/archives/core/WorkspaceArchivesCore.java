package org.jboss.ide.eclipse.archives.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.build.ModelChangeListenerWithRefresh;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.IExtensionManager;
import org.jboss.ide.eclipse.archives.core.model.IPreferenceManager;
import org.jboss.ide.eclipse.archives.core.model.IArchivesVFS;
import org.jboss.ide.eclipse.archives.core.model.other.internal.ArchivesWorkspaceLogger;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspaceExtensionManager;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspacePreferenceManager;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspaceVFS;
import org.jboss.ide.eclipse.archives.core.project.ProjectUtils;

public class WorkspaceArchivesCore extends ArchivesCore {
	static {
		NLS.initializeMessages("org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages", ArchivesCoreMessages.class); //$NON-NLS-1$
	}

	public WorkspaceArchivesCore () {
		super(WORKSPACE);
		ArchivesCore.setInstance(this);
		ArchivesModel.instance().addModelListener(new ModelChangeListenerWithRefresh());
	}

	protected IExtensionManager createExtensionManager() {
		return new WorkspaceExtensionManager();
	}

	protected IPreferenceManager createPreferenceManager() {
		return new WorkspacePreferenceManager();
	}

	protected IArchivesVFS createVFS() {
		return new WorkspaceVFS();
	}

	public void preRegisterProject(IPath project) {
		ProjectUtils.addProjectNature(project);
	}

	protected IArchivesLogger createLogger() {
		return new ArchivesWorkspaceLogger();
	}

	protected String bind2(String message, Object[] bindings) {
		return NLS.bind(message, bindings);
	}

}
