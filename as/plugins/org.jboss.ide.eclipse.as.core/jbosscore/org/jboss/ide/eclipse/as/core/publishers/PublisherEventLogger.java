package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;

import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;

public class PublisherEventLogger {
	public static final String PUBLISH_MAJOR_TYPE = "org.jboss.ide.eclipse.as.core.publishers.Events.MajorType";
	public static final String MODULE_NAME = "org.jboss.ide.eclipse.as.core.publishers.Events.Properties.ModuleName";

	public static class PublishEvent extends EventLogTreeItem {
		public PublishEvent(SimpleTreeItem parent, String specificType) {
			super(parent, PUBLISH_MAJOR_TYPE, specificType);
		}
		public PublishEvent(SimpleTreeItem parent, String specificType, IModule module) {
			super(parent, PUBLISH_MAJOR_TYPE, specificType);
			setProperty(MODULE_NAME, module.getName());
		}
	}

	// type
	public static final String MULTIPLE_MODULE_TOP_EVENT = "org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.MULTIPLE_MODULE_TOP_EVENT";
	
	// properties
	public static final String MULTIPLE_MODULE_MODULE_COUNT = "org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.MULTIPLE_MODULE_MODULE_COUNT";

	public static PublishEvent createMultipleModuleTopLevelEvent(EventLogTreeItem parent, int numMods) {
		PublishEvent event = new PublishEvent(parent, MULTIPLE_MODULE_TOP_EVENT );
		event.setProperty(MULTIPLE_MODULE_MODULE_COUNT, new Integer(numMods));
		EventLogModel.markChanged(parent);
		return event;
	}
	
	// type
	public static final String SINGLE_MODULE_TOP_EVENT = "org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.SINGLE_MODULE_TOP_EVENT";
	// properties
	public static final String MODULE_KIND = "org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.MODULE_KIND"; 
	public static final String DELTA_KIND = "org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.DELTA_KIND"; 
	public static PublishEvent createSingleModuleTopEvent(EventLogTreeItem parent, IModule module, int kind, int deltaKind) {
		PublishEvent event = new PublishEvent(parent, SINGLE_MODULE_TOP_EVENT);
		event.setProperty(MODULE_KIND, new Integer(kind));
		event.setProperty(DELTA_KIND, new Integer(deltaKind));
		event.setProperty(MODULE_NAME, module.getName());
		EventLogModel.markChanged(parent);
		return event;
	}
	
	public static PublishEvent createRemoveResultsEvent() {
		return null;
	}
	
	public static class PublisherFileUtilListener implements IFileUtilListener {
		EventLogTreeItem log;
		public PublisherFileUtilListener(EventLogTreeItem log) {
			this.log = log;
		}
		public void fileCoppied(File source, File dest, boolean result, Exception e) {
			new CoppiedEvent(log, source, dest, result, e);
			EventLogModel.markChanged(log);
		}
		public void fileDeleted(File file, boolean result, Exception e) {
			new DeletedEvent(log, file, result, e);
			EventLogModel.markChanged(log);
		}
		public void folderDeleted(File file, boolean result, Exception e) {
			new DeletedEvent(log, file, result, e);
			EventLogModel.markChanged(log);
		}
	}
	// event types
	public static final String FILE_EVENT_MAJOR_TYPE = "org.jboss.ide.eclipse.as.core.publishers.Events.FILE_EVENT_MAJOR_TYPE";
	public static final String FOLDER_DELETED_EVENT = "org.jboss.ide.eclipse.as.core.publishers.Events.FOLDER_DELETED_EVENT";
	public static final String FILE_DELETED_EVENT = "org.jboss.ide.eclipse.as.core.publishers.Events.FILE_DELETED_EVENT";
	public static final String FILE_COPPIED_EVENT = "org.jboss.ide.eclipse.as.core.publishers.Events.FILE_COPPIED_EVENT";
	// properties
	public static final String SUCCESS_PROPERTY = "org.jboss.ide.eclipse.as.core.publishers.Events.Properties.SUCCESS_PROPERTY";
	public static final String SOURCE_PROPERTY = "org.jboss.ide.eclipse.as.core.publishers.Events.Properties.SOURCE_PROPERTY";
	public static final String DEST_PROPERTY = "org.jboss.ide.eclipse.as.core.publishers.Events.Properties.DEST_PROPERTY";
	public static final String EXCEPTION_MESSAGE = "org.jboss.ide.eclipse.as.core.publishers.Events.Properties.EXCEPTION_PROPERTY";
	public static class DeletedEvent extends EventLogTreeItem {
		public DeletedEvent(EventLogTreeItem parent, File file, boolean result, Exception e) {
			super(parent, FILE_EVENT_MAJOR_TYPE, file.isDirectory() ? FOLDER_DELETED_EVENT : FILE_DELETED_EVENT);
			setProperty(SUCCESS_PROPERTY, new Boolean(result));
			setProperty(DEST_PROPERTY, file.getAbsolutePath());
			if( e != null ) 
				setProperty(EXCEPTION_MESSAGE, e.getMessage());
		}
		
	}
	public static class CoppiedEvent extends EventLogTreeItem {
		public CoppiedEvent(EventLogTreeItem parent, File source, File destination, boolean result, Exception e) {
			super(parent, FILE_EVENT_MAJOR_TYPE, FILE_COPPIED_EVENT);
			setProperty(SOURCE_PROPERTY, source.getAbsolutePath());
			setProperty(DEST_PROPERTY, destination.getAbsolutePath());
			setProperty(SUCCESS_PROPERTY, new Boolean(result));
			if( e != null ) 
				setProperty(EXCEPTION_MESSAGE, e.getMessage());
		}
	}
	
}
