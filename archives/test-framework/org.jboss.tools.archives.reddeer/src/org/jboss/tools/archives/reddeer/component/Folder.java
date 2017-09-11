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

import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.tools.archives.reddeer.archives.jdt.integration.LibFilesetDialog;
import org.jboss.tools.archives.reddeer.archives.ui.FilesetDialog;
import org.jboss.tools.archives.reddeer.archives.ui.NewFolderDialog;
import org.jboss.tools.archives.reddeer.archives.ui.NewJarDialog;

/**
 * Folder retrieved from Project Archives view/explorer
 * 
 * @author jjankovi
 *
 */
public class Folder {
	
	private TreeItem folder;
	
	public String getName() {
		return folder.getText();
	}
	
	public Folder(TreeItem folder) {
		this.folder = folder;
	}
	
	public NewJarDialog newJarArchive() {
		folder.select();
		new ContextMenuItem("New Archive", "JAR").select();
		return new NewJarDialog();
	}
	
	public NewFolderDialog newFolder() {
		folder.select();
		new ContextMenuItem("New Folder").select();
		return new NewFolderDialog();
	}
	
	public FilesetDialog newFileset() {
		folder.select();
		new ContextMenuItem("New Fileset").select();
		return new FilesetDialog();
	}
	
	public LibFilesetDialog newUserLibraryFileset() {
		folder.select();
		new ContextMenuItem("New User Library Fileset").select();
		return new LibFilesetDialog();
	}
	
	public NewFolderDialog editFolder() {
		folder.select();
		new ContextMenuItem("Edit Folder").select();
		return new NewFolderDialog();
	}
	
	public void deleteFolder(boolean withContextMenuItem) {
		folder.select();
		new ContextMenuItem("Delete Folder").select();
		try {
			Shell s = new DefaultShell("Delete selected nodes?");
			new PushButton(s, "Yes").click();
			new WaitWhile(new ShellIsAvailable(s));
		} catch (RedDeerException e) {
			//do nothing here
		}
		new WaitWhile(new JobIsRunning());
	}
	
	public Folder getFolder(String folderName) {
		return new Folder(folder.getItem(folderName));
	}
	
	public Fileset getFileset(String filesetName) {
		return new Fileset(folder.getItem(filesetName));
	}
	
	public UserLibraryFileset getUserLibraryFileset(String userLibraryFilesetName) {
		return new UserLibraryFileset(folder.getItem(userLibraryFilesetName));
	}
	
}
