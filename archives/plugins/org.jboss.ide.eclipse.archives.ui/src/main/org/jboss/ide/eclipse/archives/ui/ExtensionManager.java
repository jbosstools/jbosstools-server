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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.jboss.ide.eclipse.archives.ui.actions.NewArchiveAction;

/**
 * The extension manager
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class ExtensionManager {
	public static final String NODE_POPUP_MENUS_EXTENSION_ID = "org.jboss.ide.eclipse.archives.ui.nodePopupMenus"; //$NON-NLS-1$
	public static final String NEW_PACKAGE_ACTIONS_EXTENSION_ID = "org.jboss.ide.eclipse.archives.ui.newArchiveActions"; //$NON-NLS-1$

	private NewArchiveAction[] newArchiveActions;
	private NodeContribution[] nodeContributions;
	public NewArchiveAction[] getNewArchiveActions() {
		if( newArchiveActions == null )
			newArchiveActions = findNewArchiveActions();
		return newArchiveActions;
	}
	public NodeContribution[] getNodeContributions() {
		if( nodeContributions == null )
			nodeContributions = findNodePopupMenuContributions();
		return nodeContributions;
	}

	public static IExtension[] findExtension (String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionId);
		return extensionPoint.getExtensions();
	}

	public static NodeContribution[] findNodePopupMenuContributions () {
		ArrayList<NodeContribution> contributions = new ArrayList<NodeContribution>();
		IExtension[] extensions = findExtension(NODE_POPUP_MENUS_EXTENSION_ID);
		NodeContribution tmp;
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				tmp = new NodeContribution(elements[j]);
				if( tmp.getActionDelegate() != null ) 
					contributions.add(tmp);
			}
		}

		return contributions.toArray(new NodeContribution[contributions.size()]);
	}

	public static NewArchiveAction[] findNewArchiveActions () {
		ArrayList<NewArchiveAction> contributions = new ArrayList<NewArchiveAction>();
		IExtension[] extensions = findExtension(NEW_PACKAGE_ACTIONS_EXTENSION_ID);
		NewArchiveAction tmp;
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				tmp = new NewArchiveAction(elements[j]);
				if( tmp.getAction() != null)
					contributions.add(tmp);
			}
		}

		return contributions.toArray(new NewArchiveAction[contributions.size()]);
	}
	
	
	// Probably doesn't belong here but good enough
	private static ArrayList<ILabelProvider> labelProviders = new ArrayList<ILabelProvider>();
	public static void addLabelProvider(ILabelProvider p) {
		if( !labelProviders.contains(p))
			labelProviders.add(p);
	}
	public static void removeLabelProvider(ILabelProvider p) {
		labelProviders.remove(p);
	}
	public static ILabelProvider findLabelProvider(Object element) {
		Iterator<ILabelProvider> i = labelProviders.iterator();
		ILabelProvider l;
		while(i.hasNext()) {
			l = i.next();
			if( l.getText(element) != null)
				return l;
		}
		return null;
	}
}
