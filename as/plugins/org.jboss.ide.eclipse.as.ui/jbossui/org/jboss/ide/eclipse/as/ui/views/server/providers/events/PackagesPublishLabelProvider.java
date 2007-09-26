package org.jboss.ide.eclipse.as.ui.views.server.providers.events;

import java.util.ArrayList;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

public class PackagesPublishLabelProvider extends ComplexEventLogLabelProvider implements
		IEventLogLabelProvider {

	protected void addSupportedTypes() {
		supported = new ArrayList();
		supported.add(PublisherEventLogger.ROOT_EVENT);
		supported.add(PublisherEventLogger.MODULE_ROOT_EVENT);
		supported.add(PublisherEventLogger.FILE_COPPIED_EVENT);
		supported.add(PublisherEventLogger.FILE_DELETED_EVENT);
		supported.add(PublisherEventLogger.FOLDER_DELETED_EVENT);
	}
	protected void loadPropertyMap() {
		propertyToMessageMap.put(PublisherEventLogger.SOURCE_PROPERTY, "Source");
		propertyToMessageMap.put(PublisherEventLogger.DEST_PROPERTY, "Destination");
		propertyToMessageMap.put(PublisherEventLogger.EXCEPTION_MESSAGE, "Exception");
		propertyToMessageMap.put(PublisherEventLogger.SUCCESS_PROPERTY, "Action Succeeded");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_NAME, "Module Name");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND, "Change Type");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND, "Publish Type");
		
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_AUTO, "Auto");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_CLEAN, "Clean");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_FULL, "Full");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_INCREMENTAL, "Incremental");
		
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.ADDED, "Added");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.CHANGED, "Changed");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.REMOVED, "Removed");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.NO_CHANGE, "No Change");
		
		
//		propertyToMessageMap.put(IJBossServerPublisher.MODULE_NAME, "Module Name");
//		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherRemoveEvent.PACKAGE_NAME, "Package Name");
//		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherRemoveEvent.DESTINATION, "Package File");
//		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherMoveEvent.MOVE_SOURCE, "Source File");
//		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherMoveEvent.MOVE_DESTINATION, "Destination File");
//		propertyToMessageMap.put(PackagesPublisher.PackagesPublisherRemoveEvent.EXCEPTION_MESSAGE, "Exception Message");
	}

	public Image getImage(EventLogTreeItem item) {
		String type = item.getSpecificType();

		if( type.equals(PublisherEventLogger.ROOT_EVENT)) {
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
		}
		if( type.equals(PublisherEventLogger.MODULE_ROOT_EVENT)) {
			int deltaKind = ((Integer)item.getProperty(PublisherEventLogger.DELTA_KIND)).intValue();
			if( deltaKind == ServerBehaviourDelegate.REMOVED)
				return JBossServerUISharedImages.getImage(JBossServerUISharedImages.UNPUBLISH_IMAGE);
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
		}

		if( item.getProperty(PublisherEventLogger.EXCEPTION_MESSAGE) != null ) 
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		
		if( type.equals(PublisherEventLogger.FILE_COPPIED_EVENT))
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		if( type.equals(PublisherEventLogger.FILE_DELETED_EVENT) || type.equals(PublisherEventLogger.FOLDER_DELETED_EVENT))
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
		return null;
	}

	protected String getKindDeltaKind(EventLogTreeItem item) {
		int kind = ((Integer)item.getProperty(PublisherEventLogger.MODULE_KIND)).intValue();
		int deltaKind = ((Integer)item.getProperty(PublisherEventLogger.DELTA_KIND)).intValue();
		
		String r = "[";
		switch(kind) {
			case IServer.PUBLISH_AUTO: r += "Auto, "; break;
			case IServer.PUBLISH_CLEAN: r += "Clean, "; break;
			case IServer.PUBLISH_FULL: r += "Full, "; break;
			case IServer.PUBLISH_INCREMENTAL: r += "Incremental, "; break;
			default: r += "Unknown, ";
		}
		switch( deltaKind ) {
		
			case ServerBehaviourDelegate.NO_CHANGE: r += "No Change]"; break;
			case ServerBehaviourDelegate.ADDED: r += "Added]"; break;
			case ServerBehaviourDelegate.CHANGED: r += "Changed]"; break;
			case ServerBehaviourDelegate.REMOVED: r += "Removed]"; break;
			default: r += "Unknown]";
		}
		return r;
	}
	public String getText(EventLogTreeItem item) {
		String type = item.getSpecificType();
		
		if( type.equals(PublisherEventLogger.ROOT_EVENT)) {
			return "Publishing to server";
		}
		if( type.equals(PublisherEventLogger.MODULE_ROOT_EVENT)) {
			return getKindDeltaKind(item) + " " + item.getProperty(PublisherEventLogger.MODULE_NAME);
		}
		if( type.equals(PublisherEventLogger.FILE_COPPIED_EVENT)) {
			return "File Copy: " + new Path((String)item.getProperty(PublisherEventLogger.SOURCE_PROPERTY)).lastSegment();
		}
		if( type.equals(PublisherEventLogger.FILE_DELETED_EVENT)) {
			return "File Delete: " + new Path((String)item.getProperty(PublisherEventLogger.DEST_PROPERTY)).lastSegment();
		}
		
		return item.getSpecificType();
	}

}
