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
package org.jboss.tools.archives.reddeer.archives.ui;

import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.jboss.tools.archives.reddeer.archives.ui.NewJarDialog;
import org.jboss.tools.archives.reddeer.component.Archive;
/**
 * Simulates Project Archives explorer which is maintaned in
 * Project Explorer view
 * 
 * @author jjankovi
 *
 */
public class ProjectArchivesExplorer {

	private ProjectExplorer projectExplorer = new ProjectExplorer();
	private TreeItem explorer = null;
	private TreeItem projectItem = null;
	
	private static final String PROJECT_ARCHIVES_NODE = "Project Archives";
	
	
	public ProjectArchivesExplorer(String project) {
		openExplorer(project);
		explorer.expand();
	}
	
	private void openExplorer(String project) {
		projectExplorer.open();
		projectExplorer.getProject(project).select();
		projectItem = new DefaultTreeItem(project);
		explorer = projectItem.getItem(PROJECT_ARCHIVES_NODE);
	}
	
	public NewJarDialog newJarArchive() {
		explorer.select();
		new ContextMenuItem("New Archive", "JAR").select();
		return new NewJarDialog();
		
	}
	
	public void buildProjectFull() {
		explorer.select();
		new ContextMenuItem("Build Project (Full)").select();
		new WaitWhile(new JobIsRunning());
	}
	
	public Archive getArchive(String archiveName) {
		return new Archive(projectItem, explorer.getItem(archiveName));
	}
	
}
