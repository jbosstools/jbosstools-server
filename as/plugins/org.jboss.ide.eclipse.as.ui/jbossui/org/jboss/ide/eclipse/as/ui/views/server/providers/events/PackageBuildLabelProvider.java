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

import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.model.PackagesBuildListener;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackageBuildLabelProvider extends LabelProvider implements IEventLogLabelProvider {

	public Image getImage(EventLogTreeItem item) {
		return null;
	}

	public Properties getProperties(EventLogTreeItem item) {
		return null;
	}

	public String getText(EventLogTreeItem item) {
		if( item.getSpecificType().equals(PackagesBuildListener.PROJECT_BUILD_STARTED)) return "Build Started: " + item.getProperty("project.name");
		if( item.getSpecificType().equals(PackagesBuildListener.PROJECT_BUILD_FINISHED)) return "Build Finished: " + item.getProperty("project.name");
		if( item.getSpecificType().equals(PackagesBuildListener.PACKAGE_BUILD_STARTED)) return "Package Build Started: " + item.getProperty("package.name");
		if( item.getSpecificType().equals(PackagesBuildListener.PACKAGE_BUILD_FINISHED)) return "Package Build Finished: " + item.getProperty("package.name");
		if( item.getSpecificType().equals(PackagesBuildListener.PACKAGE_BUILD_FINISHED)) return "Package Build Failed: " + item.getProperty("package.name");
		return "";
	}

	public boolean supports(String type) {
		ArrayList list = new ArrayList();
		list.add(PackagesBuildListener.PROJECT_BUILD_STARTED);
		list.add(PackagesBuildListener.PROJECT_BUILD_FINISHED);
		list.add(PackagesBuildListener.PACKAGE_BUILD_STARTED);
		list.add(PackagesBuildListener.PACKAGE_BUILD_FINISHED);
		list.add(PackagesBuildListener.PACKAGE_BUILD_FAILED);
		
		return list.contains(type);
	}

}
