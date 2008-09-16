/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.archives.core.ant;

import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;

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
				if (property.endsWith(".dir")) {
					System.setProperty(property, getProject().getProperty(property));
				}
			}
			
			// needed so the correct XML binding / TrueZIP jars are loaded
			ClassLoader myCL = getClass().getClassLoader();
			Thread.currentThread().setContextClassLoader(myCL);
			if( ArchivesModel.instance().canReregister(projectPath)) {
				ArchivesModel.instance().registerProject(projectPath, monitor);
				new ArchiveBuildDelegate().fullProjectBuild(projectPath);
			} else {
				getCore().getLogger().log(IArchivesLogger.MSG_ERR, "Project \"" + projectPath + "\" does not exist or has no .packages file. Skipping.", null);
			}
		} catch(RuntimeException e ) {
			getCore().getLogger().log(IArchivesLogger.MSG_ERR, "A runtime error has occurred during build.", e);
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