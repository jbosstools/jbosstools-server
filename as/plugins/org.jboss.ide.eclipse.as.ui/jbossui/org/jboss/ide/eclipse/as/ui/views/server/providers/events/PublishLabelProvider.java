package org.jboss.ide.eclipse.as.ui.views.server.providers.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher.PackagesPublisherBuildEvent;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

public class PublishLabelProvider extends ComplexEventLogLabelProvider implements
		IEventLogLabelProvider {

	protected void addSupportedTypes() {
		supported = new ArrayList();
		supported.add(PackagesPublisher.REMOVE_TOP_EVENT);
		supported.add(PackagesPublisher.REMOVE_PACKAGE_SUCCESS);
		supported.add(PackagesPublisher.REMOVE_PACKAGE_SKIPPED);
		supported.add(PackagesPublisher.REMOVE_PACKAGE_FAIL);
		
		supported.add(PackagesPublisher.PUBLISH_TOP_EVENT);
		supported.add(PackagesPublisher.PROJECT_BUILD_EVENT);
		supported.add(PackagesPublisher.MOVE_PACKAGE_SUCCESS);
		supported.add(PackagesPublisher.MOVE_PACKAGE_FAIL);
		supported.add(PackagesPublisher.MOVE_PACKAGE_SKIP);
	}
	protected void loadPropertyMap() {
		propertyToMessageMap = new HashMap();
		propertyToMessageMap.put(IJBossServerPublisher.MODULE_NAME, "Module Name");
		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherRemoveEvent.PACKAGE_NAME, "Package Name");
		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherRemoveEvent.DESTINATION, "Package File");
		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherRemoveEvent.EXCEPTION_MESSAGE, "Exception Message");
	}

	public boolean supports(String type) {
		return supported.contains(type);
	}

	public Image getImage(EventLogTreeItem item) {
		return null;
	}

	public String getText(EventLogTreeItem item) {
		String type = item.getSpecificType();
		if( type.equals(PackagesPublisher.REMOVE_TOP_EVENT)) 
			return "Removing Module: " + item.getProperty(IJBossServerPublisher.MODULE_NAME);
		else if( type.equals(PackagesPublisher.REMOVE_PACKAGE_SUCCESS)) 
			return "Deleted Package: " + item.getProperty(PackagesPublisher.PackagesPublisherRemoveEvent.PACKAGE_NAME);
		else if( type.equals(PackagesPublisher.REMOVE_PACKAGE_FAIL)) 
			return "Failed to Delete Package: " + item.getProperty(PackagesPublisher.PackagesPublisherRemoveEvent.PACKAGE_NAME);
		else if( type.equals(PackagesPublisher.REMOVE_PACKAGE_SKIPPED)) 
			return "Deletion Skipped: " + item.getProperty(PackagesPublisher.PackagesPublisherRemoveEvent.PACKAGE_NAME);
		
		
		else if( type.equals(PackagesPublisher.PUBLISH_TOP_EVENT)) 
			return "Publishing Module: " + item.getProperty(IJBossServerPublisher.MODULE_NAME);
		else if( type.equals(PackagesPublisher.PROJECT_BUILD_EVENT)) 
			return "Building Project: " + item.getProperty(PackagesPublisher.PackagesPublisherRemoveEvent.PACKAGE_NAME);
		else if( type.equals(PackagesPublisher.MOVE_PACKAGE_SUCCESS))
			return "Coppied File: " + item.getProperty(PackagesPublisher.PackagesPublisherMoveEvent.PACKAGE_NAME);
		else if( type.equals(PackagesPublisher.MOVE_PACKAGE_FAIL))
			return "File Copy Failed: " + item.getProperty(PackagesPublisher.PackagesPublisherMoveEvent.PACKAGE_NAME);
		else if( type.equals(PackagesPublisher.MOVE_PACKAGE_SKIP))
			return "File Copy Skipped: " + item.getProperty(PackagesPublisher.PackagesPublisherMoveEvent.PACKAGE_NAME);
		
		return item.toString();
	}

}
