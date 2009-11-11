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

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.jboss.ide.eclipse.archives.ui.actions.INodeActionDelegate;
import org.osgi.framework.Bundle;

/**
 * This represents a menu element to be shown in the archives view
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class NodeContribution implements Comparable {
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String LABEL = "label"; //$NON-NLS-1$
	private static final String ICON = "icon"; //$NON-NLS-1$
	private static final String WEIGHT = "weight"; //$NON-NLS-1$
	private static final String CLASS = "class"; //$NON-NLS-1$

	private String id, label;
	private INodeActionDelegate actionDelegate;
	private ImageDescriptor icon;
	private int weight;

	public NodeContribution (IConfigurationElement element) {
		id = element.getAttribute(ID);
		label = element.getAttribute(LABEL);

		try {
			actionDelegate = (INodeActionDelegate) element.createExecutableExtension(CLASS);
		} catch (CoreException e) {
			//TODO			Trace.trace(getClass(), e);
		}

		String iconPath = element.getAttribute(ICON);
		String pluginId = element.getDeclaringExtension().getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(pluginId);
		URL iconURL = iconPath == null ? null : FileLocator.find(bundle, new Path(iconPath), null);
		if (iconURL != null) {
			icon = ImageDescriptor.createFromURL(iconURL);
		}
		String weightString = element.getAttribute(WEIGHT);
		weight = Integer.parseInt(weightString == null ? new Integer(100).toString() : weightString);
	}

	public int compareTo(Object o) {
		if (o instanceof NodeContribution) {
			NodeContribution other = (NodeContribution) o;
			if (weight < other.getWeight()) return -1;
			else if (weight > other.getWeight()) return 1;
			else if (weight == other.getWeight()) {
				return label.compareTo(other.getLabel());
			}
		}
		return -1;
	}


	public INodeActionDelegate getActionDelegate() {
		return actionDelegate;
	}

	public ImageDescriptor getIcon() {
		return icon;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public int getWeight() {
		return weight;
	}
}
