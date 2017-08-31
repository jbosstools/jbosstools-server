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

import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.condition.TreeContainsItem;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.swt.keyboard.KeyboardFactory;
import org.eclipse.swt.SWT;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.jboss.tools.archives.reddeer.archives.jdt.integration.LibFilesetDialog;
import org.jboss.tools.archives.reddeer.archives.ui.ArchivePublishDialog;
import org.jboss.tools.archives.reddeer.archives.ui.EditArchiveDialog;
import org.jboss.tools.archives.reddeer.archives.ui.FilesetDialog;
import org.jboss.tools.archives.reddeer.archives.ui.NewFolderDialog;
import org.jboss.tools.archives.reddeer.archives.ui.NewJarDialog;
import org.jboss.tools.archives.reddeer.archives.ui.ProjectArchivesExplorer;

/**
 * Archive retrieved from Project Archives view/explorer
 * 
 * @author jjankovi
 *
 */
public class Archive {

	private TreeItem archive;
	private TreeItem archiveProject;
	
	public Archive(TreeItem archiveProject, TreeItem archive) {
		this.archive = archive;
		this.archiveProject = archiveProject;
	}
	
	public String getName() {
		return archive.getText();
	}
	
	public NewJarDialog newJarArchive() {
		archive.select();
		new ContextMenuItem("New Archive", "JAR").select();
		return new NewJarDialog();
		
	}
	
	public NewFolderDialog newFolder() {
		archive.select();
		new ContextMenuItem("New Folder").select();
		return new NewFolderDialog();
	}
	
	public FilesetDialog newFileset() {
		archive.select();
		new ContextMenuItem("New Fileset").select();
		return new FilesetDialog();
	}
	
	public LibFilesetDialog newUserLibraryFileset() {
		archive.select();
		new ContextMenuItem("New User Library Fileset").select();
		return new LibFilesetDialog();
	}
	
	public void buildArchiveFull() {
		archive.select();
		new ContextMenuItem("Build Archive (Full)").select();
		new WaitWhile(new JobIsRunning());
	}
	
	public EditArchiveDialog editArchive() {
		archive.select();
		new ContextMenuItem("Edit Archive").select();
		return new EditArchiveDialog();
	}
	
	public void deleteArchive(boolean withContextMenuItem) {
		archive.select();
		if (withContextMenuItem) {
			new ContextMenuItem("Delete Archive").select();
		} else {
			KeyboardFactory.getKeyboard().invokeKeyCombination(SWT.DEL);
		}
		Shell dShell = new DefaultShell("Delete selected nodes?");
		new PushButton(dShell, "Yes").click();
		new WaitWhile(new ShellIsAvailable(dShell));
		new WaitWhile(new JobIsRunning());
	}
	
	public ArchivePublishDialog publishToServer() {
		archive.select();
		new ContextMenuItem("Publish To Server").select();
		if (new DefaultShell().getText().equals("Archive Publish Settings")) {
			return new ArchivePublishDialog();
		}
		new WaitWhile(new JobIsRunning());
		return null;
	}
	
	public ArchivePublishDialog editPublishSettings() {
		archive.select();
		new ContextMenuItem("Edit publish settings...").select();
		return new ArchivePublishDialog();
	}
	
	public Archive getArchive(String archiveName) {
		new WaitUntil(new TreeContainsItem(archive.getParent(), archiveProject.getText(), archive.getText()));
		return new Archive(archiveProject, archive.getItem(archiveName));
	}
	
	public Folder getFolder(String folderName, boolean explorer) {
		waitForArchiveItem(folderName, explorer);
		return new Folder(archive.getItem(folderName));
	}
	
	public Fileset getFileset(String filesetName, boolean explorer) {
		waitForArchiveItem(filesetName, explorer);
		return new Fileset(archive.getItem(filesetName));
	}
	
	public UserLibraryFileset getUserLibraryFileset(String userLibraryFilesetName, boolean explorer) {
		waitForArchiveItem(userLibraryFilesetName, explorer);
		return new UserLibraryFileset(archive.getItem(userLibraryFilesetName));
	}
	
	private void waitForArchiveItem(String item, boolean explorer){
		if(!explorer){
			new WaitUntil(new TreeContainsItem(archive.getParent(), archiveProject.getText(), archive.getText(), item));
		} else {
			new ProjectArchivesExplorer(archiveProject.getText());
			new WaitUntil(new TreeContainsItem(new DefaultTree(), archiveProject.getText(), "Project Archives", archive.getText(), item));
		}
	}
}
