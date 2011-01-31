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
package org.jboss.ide.eclipse.archives.ui.util.composites;


import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.ide.IDE;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class ArchiveFilesetDestinationComposite {

	protected Composite parent;
	protected Label destinationImage;
	protected Text destinationText;
	protected Object nodeDestination;
	private Label destinationKey;
	private Composite browseComposite;

	public ArchiveFilesetDestinationComposite(Composite parent, int style, Object destination) {
		this.parent = parent;
		this.nodeDestination = destination;

		createComposite();
	}

	protected void createComposite() {
		destinationKey = new Label(this.parent,SWT.NONE);
		destinationKey.setText(ArchivesUIMessages.FilesetInfoWizardPage_destination_label);
		destinationKey.setLayoutData(new GridData(SWT.END,SWT.CENTER,false,false));
		// create widgets
		destinationImage = new Label(this.parent, SWT.NONE);
		
		browseComposite = new Composite(this.parent, SWT.NONE);
		GridLayout gl = new GridLayout(2,false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		browseComposite.setLayout(gl);
		browseComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		destinationText = new Text(this.browseComposite, SWT.BORDER);
		destinationText.setEditable(false);
		destinationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button filesystemBrowseButton = new Button(browseComposite, SWT.PUSH);
		filesystemBrowseButton.setText(ArchivesUIMessages.PackageDestinationComposite_workspaceBrowseButton_label);
		filesystemBrowseButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				openDestinationDialog();
			}
		});
		updateDestinationViewer();
	}

	public void setPackageNodeDestination (Object destination) {
		nodeDestination = destination;
		updateDestinationViewer();
		//fireDestinationChanged();
	}

	protected void updateDestinationViewer () {
		if (nodeDestination == null) return;
		destinationText.setText(""); //$NON-NLS-1$

		if (nodeDestination instanceof IArchive) {
			IArchive pkg = (IArchive) nodeDestination;
			String txt = pkg.isTopLevel() ? pkg.getName() : pkg.getRootArchiveRelativePath().toOSString();
			String imgKey = pkg.isExploded() ? ArchivesSharedImages.IMG_PACKAGE_EXPLODED : ArchivesSharedImages.IMG_PACKAGE;

			destinationText.setText(txt);
			destinationImage.setImage(ArchivesSharedImages.getImage(imgKey));
		} else if (nodeDestination instanceof IArchiveFolder) {
			IArchiveFolder folder = (IArchiveFolder) nodeDestination;
			destinationText.setText(folder.getRootArchiveRelativePath().toString());
			destinationImage.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
		} else if (nodeDestination instanceof IProject) {
			IProject project = (IProject) nodeDestination;
			destinationText.setText(project.getName());
			destinationImage.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT));
		} else if (nodeDestination instanceof IFolder) {
			IFolder folder = (IFolder) nodeDestination;
			destinationText.setText(Path.SEPARATOR + folder.getProject().getName() + Path.SEPARATOR + folder.getProjectRelativePath().toString());
			destinationImage.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
		}
	}

	public Object getPackageNodeDestination () {
		return nodeDestination;
	}


	protected void openDestinationDialog() {
		ArchiveNodeDestinationDialog dialog = new ArchiveNodeDestinationDialog(parent.getShell(), false, true);
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if( selection != null && selection.length == 1 ) {
					if( selection[0] instanceof IArchiveNode && !(selection[0] instanceof IArchiveFileSet) )
						return Status.OK_STATUS;
				}
				return new Status(IStatus.ERROR, ArchivesCorePlugin.PLUGIN_ID, ArchivesUIMessages.SelectionNotValid);
			}
		});
		if (nodeDestination != null)
			dialog.setInitialSelection(nodeDestination);

		if (dialog.open() == Dialog.OK)
			setPackageNodeDestination(dialog.getResult()[0]);
	}
}
