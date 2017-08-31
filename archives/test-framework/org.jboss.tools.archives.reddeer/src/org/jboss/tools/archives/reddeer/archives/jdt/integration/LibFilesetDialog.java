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
package org.jboss.tools.archives.reddeer.archives.jdt.integration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.common.wait.WaitWhile;

/**
 * Dialog for creating or modifying User library fileset
 * 
 * @author jjankovi
 *
 */
public class LibFilesetDialog extends DefaultShell {

	private static final String DIALOG_TITLE = "User Library Fileset Wizard";
	
	public LibFilesetDialog() {
		super(DIALOG_TITLE);
	}

	public List<String> getUserLibraries() {
		List<String> userLibraries = new ArrayList<String>();
		for (TreeItem ti : new DefaultTree(this).getItems()) {
			userLibraries.add(ti.getText());
		}
		return userLibraries;
	}
	
	public LibFilesetDialog selectUserLibrary(String library) {
		new DefaultTreeItem(new DefaultTree(this), library).select();
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
