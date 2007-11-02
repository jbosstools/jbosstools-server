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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.PublishUtilStatusWrapper;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class PackagesPublishLabelProvider extends ComplexEventLogLabelProvider implements
		IEventLogLabelProvider {

	protected void addSupportedTypes() {
		supported.add(PublisherEventLogger.ROOT_EVENT);
		supported.add(PublisherEventLogger.MODULE_ROOT_EVENT);
		supported.add(PublisherEventLogger.FILE_COPPIED_EVENT);
		supported.add(PublisherEventLogger.FILE_DELETED_EVENT);
		supported.add(PublisherEventLogger.FOLDER_DELETED_EVENT);
		supported.add(PublisherEventLogger.PUBLISH_UTIL_STATUS_WRAPPER_TYPE);
	}
	protected void loadPropertyMap() {
		propertyToMessageMap.put(PublisherEventLogger.SOURCE_PROPERTY, "Source");
		propertyToMessageMap.put(PublisherEventLogger.DEST_PROPERTY, "Destination");
		propertyToMessageMap.put(PublisherEventLogger.EXCEPTION_MESSAGE, "Exception");
		propertyToMessageMap.put(PublisherEventLogger.SUCCESS_PROPERTY, "Action Succeeded");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_NAME, "Module Name");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND, "Module's Change Type");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND, "Server's Publish Type");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_PUBLISH_STATE, "Module's Publish Type");
		
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_AUTO, "Auto");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_CLEAN, "Clean");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_FULL, "Full");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_KIND + DELIMITER + IServer.PUBLISH_INCREMENTAL, "Incremental");

		propertyToMessageMap.put(PublisherEventLogger.MODULE_PUBLISH_STATE + DELIMITER + IServer.PUBLISH_STATE_FULL, "Full");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_PUBLISH_STATE + DELIMITER + IServer.PUBLISH_STATE_INCREMENTAL, "Incremental");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_PUBLISH_STATE + DELIMITER + IServer.PUBLISH_STATE_NONE, "None");
		propertyToMessageMap.put(PublisherEventLogger.MODULE_PUBLISH_STATE + DELIMITER + IServer.PUBLISH_STATE_UNKNOWN, "Unknown");

		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.ADDED, "Added");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.CHANGED, "Changed");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.REMOVED, "Removed");
		propertyToMessageMap.put(PublisherEventLogger.DELTA_KIND + DELIMITER + ServerBehaviourDelegate.NO_CHANGE, "No Change");
		
		propertyToMessageMap.put(PublisherEventLogger.CHANGED_FILE_COUNT, "Changed File(s)");
		propertyToMessageMap.put(PublisherEventLogger.CHANGED_MODULE_COUNT, "Changed Module(s)");
		
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
		if( type.equals(PublisherEventLogger.PUBLISH_UTIL_STATUS_WRAPPER_TYPE)) {
			Object data = item.getData();
			if( data != null && data instanceof IStatus ) {
				int sev = ((IStatus)data).getSeverity();
				switch (sev) {
				case IStatus.ERROR: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				case IStatus.WARNING: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				}
			}
		}
		return null;
	}

	protected String getKindDeltaKind(EventLogTreeItem item) {
		int kind = ((Integer)item.getProperty(PublisherEventLogger.MODULE_KIND)).intValue();
		int deltaKind = ((Integer)item.getProperty(PublisherEventLogger.DELTA_KIND)).intValue();
		int modPublishState = ((Integer)item.getProperty(PublisherEventLogger.MODULE_PUBLISH_STATE)).intValue();
		String r = "[";
		switch(kind) {
			case IServer.PUBLISH_CLEAN: r += "Clean, "; break;
			case IServer.PUBLISH_FULL: r += "Full, "; break;
			case IServer.PUBLISH_AUTO: 
			case IServer.PUBLISH_INCREMENTAL: 
				if( modPublishState == IServer.PUBLISH_STATE_FULL || modPublishState == IServer.PUBLISH_STATE_UNKNOWN)
					r += "Full, ";
				else
					r += "Incremental, ";
				break;
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
		
		if( type.equals(PublisherEventLogger.PUBLISH_UTIL_STATUS_WRAPPER_TYPE)) {
			Object o = item.getData();
			if( o == null || !(o instanceof IStatus ))
				return "Unknown Status Event: " + o;
			return ((IStatus)o).getMessage();
		}
		
		if( type.equals(PublisherEventLogger.ROOT_EVENT)) {
			Integer tmp;
			int files, mods;
			tmp = (Integer)item.getProperty(PublisherEventLogger.CHANGED_FILE_COUNT);
			files = tmp == null ? 0 : tmp.intValue();
			tmp = (Integer)item.getProperty(PublisherEventLogger.CHANGED_MODULE_COUNT);
			mods = tmp == null ? 0 : tmp.intValue();
			return "Publishing [" + mods + " modules, " + files + " files changed]";
		}
		
		if( type.equals(PublisherEventLogger.MODULE_ROOT_EVENT)) {
			return getKindDeltaKind(item) + " " + item.getProperty(PublisherEventLogger.MODULE_NAME);
		}
		
		Boolean b = (Boolean)item.getProperty(PublisherEventLogger.SUCCESS_PROPERTY);
		String fail = "Failed to ";
		if( type.equals(PublisherEventLogger.FILE_COPPIED_EVENT)) {
			return (!b.booleanValue() ? fail : "") +  
				"Copy File: " + new Path((String)item.getProperty(PublisherEventLogger.SOURCE_PROPERTY)).lastSegment();
		}
		if( type.equals(PublisherEventLogger.FILE_DELETED_EVENT)) {
			return (!b.booleanValue() ? fail : "") +  
				"Delete File: " + new Path((String)item.getProperty(PublisherEventLogger.DEST_PROPERTY)).lastSegment();
		}
		if( type.equals(PublisherEventLogger.FOLDER_DELETED_EVENT)) {
			return (!b.booleanValue() ? fail : "") +  
				"Delete Folder: " + new Path((String)item.getProperty(PublisherEventLogger.DEST_PROPERTY)).lastSegment();
		}
		if( type.equals(PublisherEventLogger.PUBLISH_UTIL_STATUS_WRAPPER_TYPE)) {
			Object data = item.getData();
			if( data != null && data instanceof IStatus ) {
				IStatus s = (IStatus)data;
				return s.getMessage();
			}
			return data == null ? "null" : data.toString();
		}
		return item.getSpecificType();
	}

}
