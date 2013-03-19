/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.jboss.ide.eclipse.as.ui.Messages;

public class ConfigureRuntimeMarkerResolution implements IMarkerResolution2 {

	private static final String RUNTIME_PROPERTY_PAGE = "org.eclipse.wst.common.project.facet.ui.internal.RuntimesPropertyPage"; //$NON-NLS-1$
	public String getDescription() {
		return Messages.ConfigureRuntimeMarkerResolution_Description;
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		return Messages.ConfigureRuntimeMarkerResolution_Description;
	}

	public void run(IMarker marker) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		IResource resource = marker.getResource();
		PropertyDialog dialog = PropertyDialog.createDialogOn(shell, RUNTIME_PROPERTY_PAGE, resource);
		
		if (dialog != null) {
			dialog.open();
		}

	}

}
