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
import org.jboss.tools.archives.reddeer.archives.ui.FilesetDialog;

/**
 * Fileset retrieved from Project Archives view/explorer
 * 
 * @author jjankovi
 *
 */
public class Fileset {

	private TreeItem fileset;

	public Fileset(TreeItem fileset) {
		this.fileset = fileset;
	}
	
	public String getName() {
		return fileset.getText();
	}
	
	public FilesetDialog editFileset() {
		fileset.select();
		new ContextMenuItem("Edit Fileset").select();
		return new FilesetDialog();
	}
	
	public void deleteFileset(boolean withContextMenuItem) {
		fileset.select();
		
		new ContextMenuItem("Delete Fileset").select();
		try {
			Shell s = new DefaultShell("Delete selected nodes?");
			new PushButton(s, "Yes").click();
			new WaitWhile(new ShellIsAvailable(s));
		} catch (RedDeerException e) {
			//do nothing here
		}
		new WaitWhile(new JobIsRunning());
	}
	
}
