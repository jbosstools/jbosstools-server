/*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.archives.core.model.other.internal;

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
	public static final String ARCHIVE_TYPES_EXTENSION_ID = "org.jboss.ide.eclipse.archives.core.archiveTypes";
	public static final String ACTION_TYPES_EXTENSION_ID = "org.jboss.ide.eclipse.archives.core.actionTypes";
	public static final String VARIABLE_PROVIDER_EXTENSION_ID = "org.jboss.ide.eclipse.archives.core.variableProviders";
	
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
					Object executable = elements[j].createExecutableExtension("class");
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
					Object executable = elements[j].createExecutableExtension("class");
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
}
