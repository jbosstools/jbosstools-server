/**
 * JBoss, a Division of Red Hat
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
package org.jboss.ide.eclipse.as.ui.views.server.providers.events;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.model.PackagesBuildListener;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.PollThread;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.TwiddlePoller;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackageBuildLabelProvider extends ComplexEventLogLabelProvider implements IEventLogLabelProvider {

	public Image getImage(EventLogTreeItem item) {
		return null;
	}

	public String getText(EventLogTreeItem item) {
//		if( item.getSpecificType().equals(PackagesBuildListener.PROJECT_BUILD_STARTED)) return "Build Started";
//		if( item.getSpecificType().equals(PackagesBuildListener.PROJECT_BUILD_FINISHED)) return "Build Finished";
//		if( item.getSpecificType().equals(PackagesBuildListener.PACKAGE_BUILD_STARTED)) return "Package Build Started";
//		if( item.getSpecificType().equals(PackagesBuildListener.PACKAGE_BUILD_FINISHED)) return "Package Build Finished";
//		if( item.getSpecificType().equals(PackagesBuildListener.PACKAGE_BUILD_FINISHED)) return "Package Build Failed";
//		if( item.getSpecificType().equals(PackagesBuildListener.FILESET_FINISHED)) return "Fileset Complete";
//		if( item.getSpecificType().equals(PackagesBuildListener.FILESET_START)) return "Fileset Started";
		
		return "";
	}


	protected void addSupportedTypes() {
//		supported.add(PackagesBuildListener.PROJECT_BUILD_STARTED);
//		supported.add(PackagesBuildListener.PROJECT_BUILD_FINISHED);
//		supported.add(PackagesBuildListener.PACKAGE_BUILD_STARTED);
//		supported.add(PackagesBuildListener.PACKAGE_BUILD_FINISHED);
//		supported.add(PackagesBuildListener.PACKAGE_BUILD_FAILED);
//		supported.add(PackagesBuildListener.FILESET_FINISHED);
//		supported.add(PackagesBuildListener.FILESET_START);
	}

	protected void loadPropertyMap() {
		// property names and their readable forms
//		propertyToMessageMap.put(EventLogTreeItem.DATE, "Time");
//
//		propertyToMessageMap.put(PackagesBuildListener.PACKAGE_NAME, "Package Name");
//		propertyToMessageMap.put(PackagesBuildListener.PROJECT_NAME, "Project Name");
//		
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_DESTINATION_FILENAME, "Destination Filename");
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_EXCLUDES_PATTERN, "Excludes Pattern");
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_FILE_PATH, "File Path");
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_INCLUDES_PATTERN, "Includes Pattern");
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_PROJECT, "Project");
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_SOURCE, "Source Container");
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_SOURCE_FOLDER, "Source Folder");
//		propertyToMessageMap.put(PackagesBuildListener.FILESET_SOURCE_PROJECT, "Source Project");
	}

}
