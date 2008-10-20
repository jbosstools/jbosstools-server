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
package org.jboss.ide.eclipse.archives.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.AbstractBuildListener;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class ArchivesUIBuildListener extends AbstractBuildListener {

	public void error(final IArchiveNode node, final IStatus[] multi) {
		final MultiStatus ms = new MultiStatus(ArchivesCore.PLUGIN_ID, 0, ArchivesUIMessages.BuildError, null);
		for( int i = 0; i < multi.length; i++ ) {
			ms.add(multi[i]);
		}
		if( PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_BUILD_ERROR_DIALOG)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					ErrorDialog ed = new ErrorDialogWithPreference(
							new Shell(), ArchivesUIMessages.BuildError, 
							NLS.bind(ArchivesUIMessages.BuildError2, node.toString()), 
							ms, IStatus.ERROR );
					ed.open();
				}
			} );
		} else {
			ArchivesCore.getInstance().getLogger().log(ms);
		}
	}

	public static class ErrorDialogWithPreference extends ErrorDialog {
		private Button checkbox;
		public ErrorDialogWithPreference(Shell parentShell, String dialogTitle,
				String message, IStatus status, int displayMask) {
			super(parentShell, dialogTitle, message, status, displayMask);
		}
		protected void okPressed() {
			// save the checkbox selection
			if( checkbox.getSelection()) {
				PrefsInitializer.setBoolean(PrefsInitializer.PREF_SHOW_BUILD_ERROR_DIALOG, false);
			}
			super.okPressed();
		}
		protected Control createMessageArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.FILL));
			composite.setLayout(new FormLayout());

			// create composite
			// create image
			Image image = getImage();
			if (image != null) {
				imageLabel = new Label(composite, SWT.NULL);
				image.setBackground(imageLabel.getBackground());
				imageLabel.setImage(image);
				addAccessibleListeners(imageLabel, image);
				FormData fd = new FormData();
				fd.left = new FormAttachment(0,5);
				fd.top = new FormAttachment(0,5);
				imageLabel.setLayoutData(fd);
			}
			// create message
			if (message != null) {
				messageLabel = new Label(composite, getMessageLabelStyle());
				messageLabel.setText(message);
				FormData fd = new FormData();
				fd.top = new FormAttachment(0,5);
				fd.left = image == null ? new FormAttachment(0,5) : new FormAttachment(imageLabel,5);
				messageLabel.setLayoutData(fd);
			}

			checkbox = new Button(composite, SWT.CHECK);
			checkbox.setText(ArchivesUIMessages.DoNotShowThisAgain);
			FormData fd = new FormData();
			fd.top = new FormAttachment(messageLabel,5);
			fd.left = new FormAttachment(0,5);
			fd.right = new FormAttachment(0,IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			checkbox.setLayoutData(fd);

			return parent;
		}
		private void addAccessibleListeners(Label label, final Image image) {
			label.getAccessible().addAccessibleListener(new AccessibleAdapter() {
				public void getName(AccessibleEvent event) {
					final String accessibleMessage = getAccessibleMessageFor(image);
					if (accessibleMessage == null) {
						return;
					}
					event.result = accessibleMessage;
				}
			});
		}
		private String getAccessibleMessageFor(Image image) {
			if (image.equals(getErrorImage())) {
				return JFaceResources.getString("error");//$NON-NLS-1$
			}

			if (image.equals(getWarningImage())) {
				return JFaceResources.getString("warning");//$NON-NLS-1$
			}

			if (image.equals(getInfoImage())) {
				return JFaceResources.getString("info");//$NON-NLS-1$
			}

			if (image.equals(getQuestionImage())) {
				return JFaceResources.getString("question"); //$NON-NLS-1$
			}

			return null;
		}
	}
}
