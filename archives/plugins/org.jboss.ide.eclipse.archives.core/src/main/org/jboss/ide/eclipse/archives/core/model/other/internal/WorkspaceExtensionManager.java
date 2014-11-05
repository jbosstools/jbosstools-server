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
package org.jboss.ide.eclipse.archives.core.model.other.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IActionType;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.core.model.IExtensionManager;

/**
 * This class will be responsible for loading extension points in the core.
 *
 * @author Rob Stryker (rob.stryker@redhat.com)
 *
 */
public class WorkspaceExtensionManager implements IExtensionManager {
	public static final String ARCHIVE_TYPES_EXTENSION_ID = "org.jboss.ide.eclipse.archives.core.archiveTypes"; //$NON-NLS-1$
	public static final String ACTION_TYPES_EXTENSION_ID = "org.jboss.ide.eclipse.archives.core.actionTypes"; //$NON-NLS-1$
	public static final String VARIABLE_PROVIDER_EXTENSION_ID = "org.jboss.ide.eclipse.archives.core.variableProviders"; //$NON-NLS-1$
	public static final String NODE_PROVIDER_EXTENSION_ID = "org.jboss.ide.eclipse.archives.core.nodeProvider"; //$NON-NLS-1$

	
	private IExtension[] findExtension (String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionId);
		return extensionPoint.getExtensions();
	}

	private static Hashtable<String, IArchiveType> archiveTypes;
	private void loadPackageTypes () {
		archiveTypes = new Hashtable<String, IArchiveType>();
		IExtension[] extensions = findExtension(ARCHIVE_TYPES_EXTENSION_ID);

		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement elements[] = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				try {
					Object executable = elements[j].createExecutableExtension("class"); //$NON-NLS-1$
					IArchiveType type = (IArchiveType)executable;
					archiveTypes.put(type.getId(), type);
				} catch (InvalidRegistryObjectException e) {
					ArchivesCore.getInstance().getLogger().log(IStatus.WARNING, e.getMessage(), e);
				} catch( CoreException e ) {
					ArchivesCore.getInstance().getLogger().log(IStatus.WARNING, e.getMessage(), e);
				}
			}
		}
	}

	public IArchiveType getArchiveType (String packageType) {
		if (archiveTypes == null)
			loadPackageTypes();
		return archiveTypes.get(packageType);
	}

	public IArchiveType[] getArchiveTypes() {
		if( archiveTypes == null )
			loadPackageTypes();
		Collection<IArchiveType> c = archiveTypes.values();
		return c.toArray(new IArchiveType[c.size()]);
	}


	private static Hashtable<String, IActionType> actionTypes;
	public IActionType getActionType(String id) {
		if (actionTypes == null)
			loadActionTypes();
		return actionTypes.get(id);
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IExtensionManager#getActionTypes()
	 */
	public IActionType[] getActionTypes() {
		if( actionTypes == null )
			loadActionTypes();
		Collection<IArchiveType> c = archiveTypes.values();
		return c.toArray(new IActionType[c.size()]);
	}

	private void loadActionTypes() {
		actionTypes = new Hashtable<String, IActionType>();
		IExtension[] extensions = findExtension(ACTION_TYPES_EXTENSION_ID);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement elements[] = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				try {
					Object executable = elements[j].createExecutableExtension("class"); //$NON-NLS-1$
					IActionType type = (IActionType)executable;
					actionTypes.put(type.getId(), type);
				} catch (InvalidRegistryObjectException e) {
					ArchivesCore.getInstance().getLogger().log(IStatus.WARNING, e.getMessage(), e);
				} catch( CoreException e ) {
					ArchivesCore.getInstance().getLogger().log(IStatus.WARNING, e.getMessage(), e);
				}
			}
		}
	}
	
	

	private static ArrayList<INodeProvider> nodeProviders = null;
	public INodeProvider[] getNodeProviders() {
		if (nodeProviders == null)
			loadNodeProviders();
		return (INodeProvider[]) nodeProviders.toArray(new INodeProvider[nodeProviders.size()]);
	}

	private void loadNodeProviders() {
		nodeProviders = new ArrayList<INodeProvider>();
		IExtension[] extensions = findExtension(NODE_PROVIDER_EXTENSION_ID);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement elements[] = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				try {
					Object executable = elements[j].createExecutableExtension("class"); //$NON-NLS-1$
					INodeProvider type = (INodeProvider)executable;
					nodeProviders.add(type);
				} catch (InvalidRegistryObjectException e) {
					ArchivesCore.getInstance().getLogger().log(IStatus.WARNING, e.getMessage(), e);
				} catch( CoreException e ) {
					ArchivesCore.getInstance().getLogger().log(IStatus.WARNING, e.getMessage(), e);
				}
			}
		}
	}
	
}
