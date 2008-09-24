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
package org.jboss.ide.eclipse.archives.ui.preferences;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IPreferenceManager;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class MainPreferencePage extends PropertyPage implements
		IWorkbenchPreferencePage {

	private Button showPackageOutputPath, showFullFilesetRootDir;
	private Button showProjectRoot, showAllProjects;
	private Button automaticBuilder, showErrorDialog, overrideButton;
	private Group corePrefGroup, viewPrefGroup;
	private Composite overrideComp;


	public MainPreferencePage() {
		super();
		setTitle(ArchivesUIMessages.PreferencePageTitle);
		setImageDescriptor(ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_PACKAGE));
	}

	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));

		createOverridePrefs(main);
		createCorePrefs(main);
		createViewPrefs(main);
		fillValues();
		return main;
	}

	public IPath getResourceLocationIfExists() {
		IAdaptable el = getElement();
		return el == null ? null :
				((IResource)el.getAdapter(IResource.class)).getLocation();
	}

	public IPreferenceManager getPrefManager() {
		return ArchivesCore.getInstance().getPreferenceManager();
	}
	protected void fillValues() {
		if( getResourceLocationIfExists() != null ) {
			overrideButton.setSelection(getPrefManager().areProjectSpecificPrefsEnabled(getResourceLocationIfExists()));
		}
		automaticBuilder.setSelection(getPrefManager().isBuilderEnabled(getResourceLocationIfExists()));
		showErrorDialog.setSelection(PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_BUILD_ERROR_DIALOG, getElement(), true));
		showAllProjects.setSelection(
				PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_ALL_PROJECTS, getElement(), false));
		showPackageOutputPath.setSelection(
				PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_PACKAGE_OUTPUT_PATH, getElement(), false));
		showFullFilesetRootDir.setSelection(
				PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_FULL_FILESET_ROOT_DIR, getElement(), false));
		showProjectRoot.setSelection(
				PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_PROJECT_ROOT, getElement(), false));

		showAllProjects.setEnabled(showProjectRoot.getSelection());
		if (!showProjectRoot.getSelection())
			showAllProjects.setSelection(false);

		if( getElement() != null ) {
			setWidgetsEnabled(overrideButton.getSelection());
		}
	}

	protected void createOverridePrefs(Composite main) {
		if( getResourceLocationIfExists() != null ) {
			overrideComp = new Composite(main, SWT.NONE);
			overrideComp.setLayout(new FillLayout());
			overrideButton = new Button(overrideComp, SWT.CHECK);
			overrideButton.setText(ArchivesUIMessages.ProjectSpecificSettings);

			overrideButton.addSelectionListener(new SelectionListener(){
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				public void widgetSelected(SelectionEvent e) {
					setWidgetsEnabled(overrideButton.getSelection());
				}
			});
		}
	}

	protected void setWidgetsEnabled(boolean val) {
		showPackageOutputPath.setEnabled(val);
		showProjectRoot.setEnabled(val);
		showFullFilesetRootDir.setEnabled(val);
		if( showProjectRoot.getSelection())
			showAllProjects.setEnabled(val);
		automaticBuilder.setEnabled(val);
		showErrorDialog.setEnabled(val);
	}

	protected void createCorePrefs(Composite main) {
		corePrefGroup = new Group(main, SWT.NONE);
		corePrefGroup.setText(ArchivesUIMessages.CorePreferences);
		corePrefGroup.setLayout(new GridLayout(1, false));
		corePrefGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		automaticBuilder = new Button(corePrefGroup, SWT.CHECK);
		automaticBuilder.setText(ArchivesUIMessages.EnableIncrementalBuilder);
		showErrorDialog = new Button(corePrefGroup, SWT.CHECK);
		showErrorDialog.setText(ArchivesUIMessages.ShowBuildErrorDialog);
	}

	protected void createViewPrefs(Composite main) {

		viewPrefGroup = new Group(main, SWT.NONE);
		viewPrefGroup.setText(ArchivesUIMessages.ProjectPackagesView);
		viewPrefGroup.setLayout(new GridLayout(1, false));
		viewPrefGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		showPackageOutputPath = new Button(viewPrefGroup, SWT.CHECK);
		showPackageOutputPath.setText(ArchivesUIMessages.ShowFullOutputPath);

		showFullFilesetRootDir = new Button(viewPrefGroup, SWT.CHECK);
		showFullFilesetRootDir.setText(ArchivesUIMessages.ShowFullRootDirectory);

		showProjectRoot = new Button(viewPrefGroup, SWT.CHECK);
		showProjectRoot.setText(ArchivesUIMessages.ShowRootProject);

		showProjectRoot.addSelectionListener(new SelectionListener () {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				showAllProjects.setEnabled(showProjectRoot.getSelection());

				if (!showProjectRoot.getSelection())
				{
					showAllProjects.setSelection(false);
				}
			}
		});

		showAllProjects = new Button(viewPrefGroup, SWT.CHECK);
		showAllProjects.setText(ArchivesUIMessages.ShowAllProjects);
		showAllProjects.setEnabled(showProjectRoot.getSelection());
		if( !showProjectRoot.getSelection() )
			showAllProjects.setSelection(false);

	}

	public void init(IWorkbench workbench) {
	}

	public void performDefaults() {
		automaticBuilder.setSelection(true);
		showErrorDialog.setSelection(true);
		showPackageOutputPath.setSelection(true);
		showFullFilesetRootDir.setSelection(true);
		showProjectRoot.setSelection(true);
		showAllProjects.setSelection(false);
		if( getResourceLocationIfExists() != null ) {
			overrideButton.setSelection(false);
			setWidgetsEnabled(overrideButton.getSelection());
		} else {
			setWidgetsEnabled(true);
		}
	}

	public boolean performOk() {
		if( getResourceLocationIfExists() != null ) {
			getPrefManager().setProjectSpecificPrefsEnabled(getResourceLocationIfExists(), overrideButton.getSelection());
		}
		getPrefManager().setBuilderEnabled(getResourceLocationIfExists(), automaticBuilder.getSelection());
		PrefsInitializer.setBoolean(PrefsInitializer.PREF_SHOW_BUILD_ERROR_DIALOG, showErrorDialog.getSelection(), getElement());
		PrefsInitializer.setBoolean(PrefsInitializer.PREF_SHOW_PACKAGE_OUTPUT_PATH, showPackageOutputPath.getSelection(), getElement());
		PrefsInitializer.setBoolean(PrefsInitializer.PREF_SHOW_FULL_FILESET_ROOT_DIR, showFullFilesetRootDir.getSelection(), getElement());
		PrefsInitializer.setBoolean(PrefsInitializer.PREF_SHOW_PROJECT_ROOT, showProjectRoot.getSelection(), getElement());
		PrefsInitializer.setBoolean(PrefsInitializer.PREF_SHOW_ALL_PROJECTS, showAllProjects.getSelection(), getElement());
		return true;
	}
}
