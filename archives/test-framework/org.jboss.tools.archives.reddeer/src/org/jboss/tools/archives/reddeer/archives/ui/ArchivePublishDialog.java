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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.reddeer.swt.api.Table;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.common.wait.WaitWhile;

/**
 * Dialog for deploying archive on servers 
 * 
 * @author jjankovi
 *
 */
public class ArchivePublishDialog extends DefaultShell {

	private static final String DIALOG_TITLE = "Archive Publish Settings";
	
	public ArchivePublishDialog() {
		super(DIALOG_TITLE);
	}

	public List<String> getAllServersInDialog() {
		List<String> serversInDialog = new ArrayList<String>();
		for (int i = 0; i < table().rowCount(); i++) {
			serversInDialog.add(table().getItem(i).getText(0));
		}
		return serversInDialog;
	}
	
	public List<String> getAllSelectedServersInDialog() {
		/* TODO */
		List<String> selectedServers = new ArrayList<String>();
		return selectedServers;
	}
	
	public ArchivePublishDialog selectServers(String... serversToSelect) {
		table().select(serversToSelect);
		return this;
	}
	
	public ArchivePublishDialog unselectServers(String... serversToUnselect) {
		/* get all selected servers - for future use */
		List<String> selectedServers = getAllSelectedServersInDialog();
		
		/* unselect all servers */
//		table().unselect();
		
		/* from all selected servers remove the ones that should be unselected */
		for (int i = 0; i < serversToUnselect.length; i++) {
			selectedServers.remove(serversToUnselect[i]);
		}
		
		/* select rest of the servers in selectedServers */
		String[] strArray = new String[selectedServers.size()];
		selectedServers.toArray(strArray);
		table().select(strArray);
		
		return this;
	}
	
	public ArchivePublishDialog unselectAllServers() {
//		table().unselect();
		return this;
	}
	
	public ArchivePublishDialog checkAlwaysPublish() {
		new CheckBox(this, 0).toggle(true);
		return this; 
	}
	
	public ArchivePublishDialog uncheckAlwaysPublish() {
		new CheckBox(this, 0).toggle(false);
		return this; 
	}
	
	public ArchivePublishDialog checkAutoDeploy() {
		new CheckBox(this, 1).toggle(true);
		return this; 
	}
	
	public ArchivePublishDialog uncheckAutoDeploy() {
		new CheckBox(this, 1).toggle(false);
		return this; 
	}
	
	private Table table() {
		return new DefaultTable(this);
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
