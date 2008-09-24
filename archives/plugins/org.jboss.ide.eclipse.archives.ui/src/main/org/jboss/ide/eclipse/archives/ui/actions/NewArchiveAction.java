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
package org.jboss.ide.eclipse.archives.ui.actions;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IActionDelegate;
import org.osgi.framework.Bundle;

/**
 * This is an action wrapper. It wraps the extension point
 */
public class NewArchiveAction {
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String LABEL = "label"; //$NON-NLS-1$
	private static final String ICON = "icon"; //$NON-NLS-1$
	private static final String CLASS = "class"; //$NON-NLS-1$

	private String id, label;
	private ImageDescriptor icon;
	private Image iconImage;
	private IActionDelegate action;

	public NewArchiveAction(IConfigurationElement element) {
		id = element.getAttribute(ID);
		label = element.getAttribute(LABEL);

		try {
			action = (IActionDelegate) element.createExecutableExtension(CLASS);
		} catch (CoreException e) {
			//TODO
		}

		String iconPath = element.getAttribute(ICON);
		String pluginId = element.getDeclaringExtension().getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(pluginId);
		URL iconURL = FileLocator.find(bundle, new Path(iconPath), null);
		if (iconURL == null) {
			iconURL = bundle.getEntry(iconPath);
		}
		icon = ImageDescriptor.createFromURL(iconURL);
		iconImage = icon.createImage();
	}

	public IActionDelegate getAction() {
		return action;
	}

	public ImageDescriptor getIconDescriptor() {
		return icon;
	}

	public Image getIcon() {
		return iconImage;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	protected void finalize() throws Throwable {
		iconImage.dispose();
	}

}
