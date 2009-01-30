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
package org.jboss.ide.eclipse.archives.core.project;

import java.util.ArrayList;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * The nature associated with archives, specifically
 * the builder is added or removed based on this nature.
 *
 * @author Rob Stryker (rob.stryker@redhat.com)
 *
 */
public class ArchivesNature implements IProjectNature {

	public static final String NATURE_ID = "org.jboss.ide.eclipse.archives.core.archivesNature"; //$NON-NLS-1$

	private IProject project;

	public void configure() throws CoreException {
		addProjectBuilder(project, ArchivesBuilder.BUILDER_ID);
	}

	public void deconfigure() throws CoreException {
		removeProjectBuilder(project, ArchivesBuilder.BUILDER_ID);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public static boolean addProjectBuilder(IProject project, String builderId)
	{
		try
		{
			IProjectDescription desc = project.getDescription();
			ICommand[] commands = desc.getBuildSpec();

			//add builders to project
			ICommand builderCommand = desc.newCommand();
			builderCommand.setBuilderName(builderId);

			ICommand[] newCommands = new ICommand[commands.length + 1];
			System.arraycopy(commands, 0, newCommands, 0, commands.length);
			newCommands[newCommands.length - 1] = builderCommand;

			desc.setBuildSpec(newCommands);

			project.setDescription(desc, new NullProgressMonitor());
	      }
	      catch (CoreException e)
	      {
	         e.printStackTrace();
	         return false;
	      }

	      return true;
	   }

	   /**
	    * Removes a specific builder from the end of a given project's builder list.
	    * @param project
	    * @param builderId
	    * @return Whether or not the builder was successfully removed
	    */
	   public static boolean removeProjectBuilder(IProject project, String builderId) {
	      try {
	         IProjectDescription desc = project.getDescription();
	         ICommand[] commands = desc.getBuildSpec();
	         ArrayList newCommands = new ArrayList();

	         for (int i = 0; i < commands.length; i++) {
	            if (!commands[i].getBuilderName().equals(builderId)) {
	               newCommands.add(commands[i]);
	            }
	         }

	         desc.setBuildSpec((ICommand[]) newCommands.toArray(new ICommand[newCommands.size()]));

	         project.setDescription(desc, new NullProgressMonitor());
	      } catch (CoreException e) {
	         e.printStackTrace();
	         return false;
	      }

	      return true;
	   }


}
