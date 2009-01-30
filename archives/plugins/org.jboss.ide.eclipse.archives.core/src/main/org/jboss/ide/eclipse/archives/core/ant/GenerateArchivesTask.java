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
package org.jboss.ide.eclipse.archives.core.ant;

import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class GenerateArchivesTask extends Task {

	private String projectPath;

	public void init() throws BuildException {
		// Force standalone mode
		AntArchivesCore standalone = new AntArchivesCore();
		ArchivesCore.setInstance(standalone);
	}

	protected AntArchivesCore getCore() {
		return (AntArchivesCore)ArchivesCore.getInstance();
	}
	public void execute() throws BuildException {
		getCore().setProject(getProject());
		getCore().setTask(this);
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			IPath projectPath = new Path(this.projectPath);
			IProgressMonitor monitor = new NullProgressMonitor();

			for (Iterator iter = getProject().getProperties().keySet().iterator(); iter.hasNext(); ) {
				String property = (String) iter.next();
				if (property.endsWith(AntVFS.SUFFIX)) {
					System.setProperty(property, getProject().getProperty(property));
				}
			}

			// needed so the correct XML binding / TrueZIP jars are loaded
			ClassLoader myCL = getClass().getClassLoader();
			Thread.currentThread().setContextClassLoader(myCL);
			if( ArchivesModel.instance().canReregister(projectPath)) {
				ArchivesModel.instance().registerProject(projectPath, monitor);
				new ArchiveBuildDelegate().fullProjectBuild(projectPath, new NullProgressMonitor());
			} else {
				getCore().log(IStatus.ERROR,
						getCore().bind(ArchivesCoreMessages.ProjectCannotBeBuilt, projectPath.toString()), null);
			}
		} catch(RuntimeException e ) {
			getCore().getLogger().log(IStatus.ERROR, ArchivesCoreMessages.RuntimeErrorDuringBuild, e);
		}
		finally {
			Thread.currentThread().setContextClassLoader(original);
			getCore().setProject(null);
			getCore().setTask(null);
		}
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
}