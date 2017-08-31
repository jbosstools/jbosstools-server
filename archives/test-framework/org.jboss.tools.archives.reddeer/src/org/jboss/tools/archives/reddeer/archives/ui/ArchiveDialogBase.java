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

import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.common.wait.WaitWhile;

/**
 * Base dialog class for EditArchiveDialog and NewJarDialog
 * 
 * @author jjankovi
 *
 */
public abstract class ArchiveDialogBase extends DefaultShell {

	public ArchiveDialogBase(String dialogTitle) {
		super(dialogTitle);
	}
	
	public ArchiveDialogBase setArchiveName(String archiveName) {
		String newArchiveName = archiveName.contains(".jar")?
				archiveName:archiveName + ".jar";
		new LabeledText(this, "Archive name:").setText(newArchiveName);
		return this;
	}
	
	public ArchiveDialogBase setDestination(String location) {
		new LabeledText(this, "Destination:").setText("");
		new LabeledText(this, "Destination:").setText(location);
		return this;
	}
	
	public ArchiveDialogBase setFileSystemRelative() {
		new RadioButton(this, "Filesystem Relative").click();
		return this;
	}
	
	public ArchiveDialogBase setWorkspaceRelative() {
		new RadioButton(this, "Workspace Relative").click();
		return this;
	}
	
	public ArchiveDialogBase setZipStandardArchiveType() {
		new RadioButton(this, "Standard archive using zip compression").click();
		return this;
	}
	
	public ArchiveDialogBase setNoCompressionArchiveType() {
		new RadioButton(this, "Exploded archive resulting in a folder (no compression)").click();
		return this;
	}
	
	public void cancel() {
		new PushButton(this, "Cancel").click();
		new WaitWhile(new ShellIsAvailable(this));
	}
	
	public void finish() {
		new PushButton(this, "Finish").click();
		new WaitWhile(new ShellIsAvailable(this));
		new WaitWhile(new JobIsRunning());
	}

}
