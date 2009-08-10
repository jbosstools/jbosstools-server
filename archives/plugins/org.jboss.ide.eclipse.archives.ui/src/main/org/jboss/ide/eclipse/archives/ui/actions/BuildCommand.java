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
package org.jboss.ide.eclipse.archives.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;

public class BuildCommand extends AbstractHandler implements IHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		String val;
		try {
			val = manager.getDynamicVariable("project_name").getValue(null); //$NON-NLS-1$
			if( val != null ) {
				final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(val);
				if( p.exists() && p.isOpen() ) {
					Job j = new Job(ArchivesUIMessages.BuildArchivesNode) {
						protected IStatus run(IProgressMonitor monitor) {
							return new ArchiveBuildDelegate().fullProjectBuild(p.getLocation(), monitor);
						}
					};
					j.schedule();
				}
			}
		} catch (CoreException e) {
		} 
		return null;
	}
}
