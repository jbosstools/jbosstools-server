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

import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;

/**
* Represents Preference page: 
* 		Project Archives
* 
* @author jjankovi
*
*/
public class MainPreferencePage extends PreferencePage {

	public MainPreferencePage(ReferencedComposite composite) {
		super(composite, "Project Archives");
	}
	
	public void enableIncrementalBuilder(boolean enable) {
		new CheckBox(referencedComposite, "Enable incremental builder").toggle(enable);
	}
	
	public boolean isIncrementalBuilderEnabled() {
		return new CheckBox(referencedComposite, "Enable incremental builder").isChecked();
	}
	
	public void showBuildErrorDialog(boolean show) {
		new CheckBox(referencedComposite, "Show build error dialog").toggle(show);
	}
	
	public boolean isBuildErrorDialogShown() {
		return new CheckBox(referencedComposite, "Show build error dialog").isChecked();
	}
	
	public void showOutputPathNextToPackages(boolean show) {
		new CheckBox(referencedComposite, "Show output path next to packages.").toggle(show);
	}
	
	public boolean isOutputPathNextToPackagesShown() {
		return new CheckBox(referencedComposite, "Show output path next to packages.").isChecked();
	}
	
	public void showRootDirectoryOfFilesets(boolean show) {
		new CheckBox(referencedComposite, "Show the root directory of filesets.").toggle(show);
	}
	
	public boolean isRootDirectoryOfFilesetsShown() {
		return new CheckBox(referencedComposite, "Show the root directory of filesets.").isChecked();
	}
	
	public void showProjectAtTheRoot(boolean show) {
		new CheckBox(referencedComposite, "Show project at the root").toggle(show);
	}
	
	public boolean isProjectAtTheRootShown() {
		return new CheckBox(referencedComposite, "Show project at the root").isChecked();
	}
	
	public void showAllProjectsThatContainPackages(boolean show) {
		new CheckBox(referencedComposite, "Show all projects that contain packages").toggle(show);
	}
	
	public boolean areAllProjectsThatContainPackagesShown() {
		return new CheckBox(referencedComposite, "Show all projects that contain packages").isChecked();
	}
	
	public void showNodeInAllProjects(boolean show) {
		new CheckBox(referencedComposite, "Show node in all projects").toggle(show);
	}
	
	public boolean isNodeInAllProjectShown() {
		return new CheckBox(referencedComposite, "Show node in all projects").isChecked();
	}
	
	public void enableDefaultExcludes(boolean enable) {
		new CheckBox(referencedComposite, "Enable Default Excludes").toggle(enable);
	}
	
	public boolean isDefaultExcludesEnabled() {
		return new CheckBox(referencedComposite, "Enable Default Excludes").isChecked();
	}
}
