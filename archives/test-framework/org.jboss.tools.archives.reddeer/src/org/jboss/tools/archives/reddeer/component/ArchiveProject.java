/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.archives.reddeer.component;

import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.jboss.tools.archives.reddeer.archives.ui.NewJarDialog;

/**
 * Archive Project accessible via Project Archives view
 * 
 * @author jjankovi
 *
 */
public class ArchiveProject {

	private TreeItem archiveProject;
	protected static final Logger log = Logger.getLogger(ArchiveProject.class);

	public ArchiveProject(TreeItem archiveProject) {
		this.archiveProject = archiveProject;
	}
	
	public String getName() {
		return archiveProject.getText();
	}
	
	public NewJarDialog newJarArchive() {
		selectArchive();
		new ContextMenuItem("New Archive", "JAR").select();
		return new NewJarDialog();
	}
	
	public void buildProjectFull() {
		selectArchive();
		new ContextMenuItem("Build Project (Full)").select();
		new WaitWhile(new JobIsRunning());
	}
	
	public Archive getArchive(String archiveName) {
		return new Archive(archiveProject, archiveProject.getItem(archiveName));
	}
	
	public boolean hasArchive(String archiveName){
		try{
			archiveProject.getItem(archiveName);
		} catch (RedDeerException e) {
			return false;
		}
		return true;
	}
	
	public void selectArchive(){
		archiveProject.select();
	}
	
}
