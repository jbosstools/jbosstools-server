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
import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PathPublisher;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PathPublisherLabelProvider extends ComplexEventLogLabelProvider implements IEventLogLabelProvider {

	protected void addSupportedTypes() {
		supported = new ArrayList();
		supported.add(PathPublisher.PUBLISH_TOP_EVENT);
		supported.add(PathPublisher.UNPUBLISH_TOP_EVENT);
		supported.add(PathPublisher.PUBLISH_EVENT);
		supported.add(PathPublisher.UNPUBLISH_EVENT);
	}

	protected void loadPropertyMap() {
		propertyToMessageMap.put(IJBossServerPublisher.MODULE_NAME, "Module Name");
		propertyToMessageMap.put(PathPublisher.SOURCE_FILE, "Source File");
		propertyToMessageMap.put(PathPublisher.DEST_FILE, "Destination File");
		propertyToMessageMap.put(PathPublisher.TARGET_FILE, "Target File");
		propertyToMessageMap.put(PathPublisher.SUCCESS, "Success");
		propertyToMessageMap.put(PathPublisher.EXCEPTION, "Exception");
	}

	public Image getImage(EventLogTreeItem item) {
		String type = item.getSpecificType();
		if( type.equals(PathPublisher.PUBLISH_EVENT)) 
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
		if( type.equals(PathPublisher.UNPUBLISH_EVENT)) 
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.UNPUBLISH_IMAGE);
		return null;
	}

	public String getText(EventLogTreeItem item) {
		String type = item.getSpecificType();
		if( type.equals(PathPublisher.PUBLISH_EVENT)) return "Publishing: " + item.getProperty(IJBossServerPublisher.MODULE_NAME);
		if( type.equals(PathPublisher.UNPUBLISH_EVENT)) return "Unpublishing: " + item.getProperty(IJBossServerPublisher.MODULE_NAME);
		
		return item.toString();
	}

}
