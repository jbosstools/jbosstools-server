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
package org.jboss.ide.eclipse.archives.ui.wizards.pages;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.archives.core.model.ArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.util.composites.ArchiveSourceDestinationComposite;
import org.jboss.ide.eclipse.archives.ui.util.composites.ArchiveSourceDestinationComposite.ChangeListener;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.archives.ui.wizards.WizardPageWithNotification;
import org.jboss.ide.eclipse.archives.ui.wizards.WizardWithNotification;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class ArchiveInfoWizardPage extends WizardPageWithNotification {

	private AbstractArchiveWizard wizard;
	private Text packageNameText;
	private Button compressedButton;
	private Button explodedButton;
	private String packageName;
	private boolean packageExploded;
	private ArchiveSourceDestinationComposite destinationComposite;
	private IArchive archive;

	public ArchiveInfoWizardPage (AbstractArchiveWizard wizard, IArchive existingPackage) {
		super (ArchivesUIMessages.PackageInfoWizardPage_title, ArchivesUIMessages.PackageInfoWizardPage_title, wizard.getImageDescriptor());
		setWizard(wizard);
		this.archive = existingPackage;
	}

	public void createControl(Composite parent) {
		setMessage(ArchivesUIMessages.PackageInfoWizardPage_message);

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));

		Group infoGroup = new Group(main, SWT.NONE);
		infoGroup.setLayout(new GridLayout(3, false));
		infoGroup.setText(ArchivesUIMessages.PackageInfoWizardPage_infoGroup_label);
		expand(infoGroup);

		new Label(infoGroup, SWT.NONE).setText(ArchivesUIMessages.PackageInfoWizardPage_packageName_label);
		Composite pkgNameComposite = new Composite(infoGroup, SWT.NONE);
		GridLayout pkgNameLayout = new GridLayout(2, false);
		pkgNameLayout.marginHeight = 0;
		pkgNameLayout.marginWidth = 0;
		pkgNameComposite.setLayout(pkgNameLayout);
		pkgNameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(pkgNameComposite, SWT.NONE).setImage(ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_PACKAGE));

		packageNameText = new Text(pkgNameComposite, SWT.BORDER);
		packageName = wizard.getProject().getName() + "." + wizard.getArchiveExtension(); //$NON-NLS-1$
		packageNameText.setText(packageName);
		packageNameText.setSelection(0, wizard.getProject().getName().length());
		expand(packageNameText);

		GridData pkgNameData = new GridData(GridData.FILL_HORIZONTAL);
		pkgNameData.horizontalSpan = 2;
		pkgNameComposite.setLayoutData(pkgNameData);

		packageNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (validate()) {
					packageName = packageNameText.getText();
				}
			}
		});

		Label l = new Label(infoGroup, SWT.NONE);
		l.setText(ArchivesUIMessages.PackageInfoWizardPage_destination_label);
		GridData lData = new GridData(GridData.BEGINNING, GridData.BEGINNING,false,false);
		l.setLayoutData(lData);

		GridData destinationTextData = new GridData(GridData.FILL_BOTH);
		destinationTextData.horizontalSpan = 2;
		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		buttonData.horizontalSpan = 3;
		buttonData.horizontalAlignment = SWT.END;

		destinationComposite = new ArchiveSourceDestinationComposite(infoGroup, wizard.getProject().getName(), getDescriptorVersion());
		destinationComposite.addChangeListener(new ChangeListener () {
			public void compositeChanged() {
				validate();
			}
		});
		destinationComposite.setLayoutData(destinationTextData);

		Group packageTypeGroup = new Group(main, SWT.NONE);
		packageTypeGroup.setLayout(new GridLayout(1, false));
		packageTypeGroup.setText(ArchivesUIMessages.PackageInfoWizardPage_packageTypeGroup_label);
		expand(packageTypeGroup);

		if (archive != null) {
			packageExploded = archive.isExploded();
		} else {
			packageExploded = false;
		}
		compressedButton = new Button(packageTypeGroup, SWT.RADIO);
		compressedButton.setText(ArchivesUIMessages.PackageInfoWizardPage_compressedButton_label);
		compressedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				packageExploded = false;
			}
		});
		compressedButton.setSelection(!packageExploded);
		explodedButton = new Button(packageTypeGroup, SWT.RADIO);
		explodedButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				packageExploded = true;
			}
		});
		explodedButton.setText(ArchivesUIMessages.PackageInfoWizardPage_explodedButton_label);
		setControl(main);

		fillDefaults();
		validate();
	}

	private void fillDefaults () {
		if (archive != null) {
			compressedButton.setSelection(!archive.isExploded());
			explodedButton.setSelection(archive.isExploded());
			packageNameText.setText(archive.getName());
			packageName = archive.getName();
			explodedButton.setSelection(archive.isExploded());
			compressedButton.setSelection(!archive.isExploded());
			IArchiveNode parent = archive.getParent();
			if( parent != null && !(parent instanceof IArchiveModelRootNode)) {
				destinationComposite.init(parent);
			} else {
				destinationComposite.init(archive.getRawDestinationPath(), archive.isDestinationInWorkspace());
			}
		} else {
			if(wizard.getInitialNode() != null )
				destinationComposite.init(wizard.getInitialNode());
			else
				destinationComposite.init(wizard.getInitialPath(), wizard.isInitialPathWorkspaceRelative());
		}
	}

	private boolean validate () {
		String message = null;
		int messageType = IStatus.OK;
		if (packageNameText.getText() == null || packageNameText.getText().length() == 0) {
			message = ArchivesUIMessages.PackageInfoWizardPage_error_noPackageName;
			messageType = IStatus.ERROR;
		} else if( destinationComposite.getMessage() != null ) {
			message = destinationComposite.getMessage();
			messageType = destinationComposite.getStatusType();
		} else if( destinationComposite.getDestinationNode() != null ) {
				IArchiveNode parentNode = destinationComposite.getDestinationNode();
				// verify no child has the same name
				IArchiveNode subPackages[] = parentNode.getChildren(IArchiveNode.TYPE_ARCHIVE);
				for (int i = 0; i < subPackages.length; i++) {
					IArchive subPackage = (IArchive) subPackages[i];
					if (subPackage.getName().equals(packageNameText.getText())
						&& (!subPackage.equals(this.archive))) {
							message =  ArchivesUIMessages.bind(
								ArchivesUIMessages.PackageInfoWizardPage_error_packageAlreadyExists,
								packageNameText.getText());
							messageType = IStatus.ERROR;
					}
				}
		} else if( destinationComposite.getPath() != null ) {
			// checking for another archive with the same destination / name
			boolean relative = destinationComposite.isWorkspaceRelative();
			IPath destinationLocation =
				PathUtils.getGlobalLocation(destinationComposite.getPath(),
						wizard.getProject().getName(), relative, 1);

			ArrayList<IArchive> allArchives = new ArrayList<IArchive>();
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for( int i = 0; i < projects.length; i++ )
				allArchives.addAll(Arrays.asList(ModelUtil.getProjectArchives(projects[i].getLocation())));

			IArchive[] packages = (IArchive[]) allArchives.toArray(new IArchive[allArchives.size()]);
			if (packages != null) {
				for( int i = 0; i < packages.length; i++ ) {
					IArchive pkg = (IArchive) packages[i];
					if (pkg.getName().equals(packageNameText.getText())
						&& (PathUtils.getGlobalLocation(pkg)!= null && PathUtils.getGlobalLocation(pkg).equals(destinationLocation))
						&& (!pkg.equals(this.archive))) {
						message = ArchivesUIMessages.bind(
									ArchivesUIMessages.PackageInfoWizardPage_error_packageAlreadyExists, packageNameText.getText());
						messageType = IStatus.ERROR;
					}
				}
			}
		} else {
			message = (ArchivesUIMessages.PackageInfoWizardPage_error_noDestination);
		}

		setPageComplete(message == null || messageType <= IStatus.WARNING);
		setMessage(message, messageType);
		return messageType <= IStatus.WARNING;
	}


	public void pageExited(int button) {
		if (button == WizardWithNotification.NEXT || button == WizardWithNotification.FINISH) {
			createPackage();
		}
	}

	private void createPackage () {
		if (archive == null) {
			archive = ArchiveNodeFactory.createArchive();
		}

		archive.setName(getPackageName());
		archive.setExploded(isPackageExploded());
		if( destinationComposite.getPath() != null ) {
			archive.setInWorkspace(destinationComposite.isWorkspaceRelative());
			archive.setDestinationPath(new Path(destinationComposite.getPath()));
		}
	}

	private void expand(Control control) {
		control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	public String getPackageName() {
		return packageName;
	}

	public boolean isPackageExploded() {
		return packageExploded;
	}

	private void setWizard(AbstractArchiveWizard wizard) {
		this.wizard = wizard;
	}

	public IArchive getArchive () {
		return archive;
	}

	protected double getDescriptorVersion() {
		IPath loc = wizard.getProject().getLocation();
		if( ArchivesModel.instance().isProjectRegistered(loc))
			return ArchivesModel.instance().getRoot(loc).getDescriptorVersion();
		return IArchiveModelRootNode.DESCRIPTOR_VERSION_LATEST;
	}

	// Getters for the wizard to call
	public String getDestinationPath() {
		return destinationComposite.getPath();
	}
	public IArchiveNode getDestinationNode() {
		return destinationComposite.getDestinationNode();
	}
	public boolean isDestinationWorkspaceRelative() {
		return destinationComposite.isWorkspaceRelative();
	}
}
