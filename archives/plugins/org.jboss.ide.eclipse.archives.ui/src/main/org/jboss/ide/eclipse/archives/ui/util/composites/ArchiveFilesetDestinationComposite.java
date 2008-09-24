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
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
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
public class ArchiveFilesetDestinationComposite extends Composite {

	protected Composite parent;
	protected Label destinationImage;
	protected Text destinationText;
	protected Object nodeDestination;

	public ArchiveFilesetDestinationComposite(Composite parent, int style, Object destination) {
		super(parent, style);
		this.parent = parent;
		this.nodeDestination = destination;

		createComposite();
	}

	protected void createComposite() {
		setLayout(new FormLayout());

		// create widgets
		destinationImage = new Label(this, SWT.NONE);
		destinationText = new Text(this, SWT.BORDER);
		Composite browseComposite = new Composite(this, SWT.NONE);

		// set up their layout positioning
		destinationImage.setLayoutData(createFormData(0,5,null, 0, 0, 0, null, 0));
		destinationText.setLayoutData(createFormData(0, 5, null, 0, destinationImage, 5, destinationImage, 205));


		// set text, add listeners, etc
		destinationText.setEditable(false);

		browseComposite.setLayout(new FillLayout());
		browseComposite.setLayoutData(createFormData(0,0,null,0,destinationText,5,100,-5));
		fillBrowseComposite(browseComposite);

		// call other functions required for startup
		updateDestinationViewer();
	}

	private FormData createFormData(Object topStart, int topOffset, Object bottomStart, int bottomOffset,
									Object leftStart, int leftOffset, Object rightStart, int rightOffset) {
		FormData data = new FormData();

		if( topStart != null ) {
			data.top = topStart instanceof Control ? new FormAttachment((Control)topStart, topOffset) :
				new FormAttachment(((Integer)topStart).intValue(), topOffset);
		}

		if( bottomStart != null ) {
			data.bottom = bottomStart instanceof Control ? new FormAttachment((Control)bottomStart, bottomOffset) :
				new FormAttachment(((Integer)bottomStart).intValue(), bottomOffset);
		}

		if( leftStart != null ) {
			data.left = leftStart instanceof Control ? new FormAttachment((Control)leftStart, leftOffset) :
				new FormAttachment(((Integer)leftStart).intValue(), leftOffset);
		}

		if( rightStart != null ) {
			data.right = rightStart instanceof Control ? new FormAttachment((Control)rightStart, rightOffset) :
				new FormAttachment(((Integer)rightStart).intValue(), rightOffset);
		}

		return data;
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

	protected void fillBrowseComposite(Composite parent) {
		Composite browseComposite = new Composite(parent, SWT.NONE);
		browseComposite.setLayout(new GridLayout(2, false));

		Button filesystemBrowseButton = new Button(browseComposite, SWT.PUSH);
		filesystemBrowseButton.setText(ArchivesUIMessages.PackageDestinationComposite_workspaceBrowseButton_label);
		filesystemBrowseButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				openDestinationDialog();
			}
		});
	}

	protected void openDestinationDialog() {
		ArchiveNodeDestinationDialog dialog = new ArchiveNodeDestinationDialog(getShell(), false, true);
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
