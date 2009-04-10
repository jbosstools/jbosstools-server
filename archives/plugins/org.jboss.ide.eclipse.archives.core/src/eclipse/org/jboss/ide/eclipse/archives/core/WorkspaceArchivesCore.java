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
package org.jboss.ide.eclipse.archives.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.archives.core.build.ModelChangeListenerWithRefresh;
import org.jboss.ide.eclipse.archives.core.build.PostBuildRefresher;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.IArchivesVFS;
import org.jboss.ide.eclipse.archives.core.model.IExtensionManager;
import org.jboss.ide.eclipse.archives.core.model.IPreferenceManager;
import org.jboss.ide.eclipse.archives.core.model.other.internal.ArchivesWorkspaceLogger;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspaceExtensionManager;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspaceNodeFactory;
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
		ArchivesModel.instance().addBuildListener(new PostBuildRefresher());
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
	
	protected IArchiveNodeFactory createNodeFactory() {
		return new WorkspaceNodeFactory();
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
