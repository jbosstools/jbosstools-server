package org.jboss.ide.eclipse.as.ui.views;

import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.provisional.ManagedUIDecorator;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.ServerLogView.EventCategory;

public class LogLabelProvider extends LabelProvider implements ITableLabelProvider {
	public Image getImage(Object element) {
		if( element instanceof EventCategory ) {
			int type = ((EventCategory)element).getType();
			if( type == IEventCodes.POLLING_CODE) 
				return new ManagedUIDecorator().getStateImage(IServer.STATE_STARTING, ILaunchManager.RUN_MODE, 1);
			if( type == IEventCodes.PUBLISHING_CODE)
				return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
		}
		
		
		if( element instanceof LogEntry) {
			int code = ((LogEntry)element).getCode();
			int istatusCode = (code & IEventCodes.ISTATUS_MASK) >> 29;
			
			                       
			if(istatusCode != 0 ) {
				switch(istatusCode) {
					case 1: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
					case 2: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
					case 3: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				}
			}
			
			int majorType = code & IEventCodes.MAJOR_TYPE_MASK;
			switch(majorType) {
			case IEventCodes.POLLING_CODE:
				return handlePollImage((LogEntry)element, code);
			case IEventCodes.PUBLISHING_CODE:
				return handlePublishImage((LogEntry)element, code);
			}
		}
		return null;
	}

	public Image handlePublishImage(LogEntry element, int code) {
		if( (code & IEventCodes.SINGLE_FILE_SUCCESS_MASK) == 0) 
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK); // error
		if( (code & IEventCodes.SINGLE_FILE_TYPE_MASK) == 0) 
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
		else
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.UNPUBLISH_IMAGE);
	}
	
	public Image handlePollImage(LogEntry element, int code) {
		if( (code & IEventCodes.FULL_POLLER_MASK) == IEventCodes.POLLING_ROOT_CODE) {
			int state = (code & PollThread.STATE_MASK) >> 3;
			return new ManagedUIDecorator().getStateImage(state, ILaunchManager.RUN_MODE, 1);
		} else if( (code & IEventCodes.FULL_POLLER_MASK) == IEventCodes.JMXPOLLER_CODE) {
			if( element.getSeverity() == IStatus.WARNING)
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
			int state = code & 0xF;
			switch(state) {
			case IEventCodes.STATE_STARTED: 
				return new ManagedUIDecorator().getStateImage(IServer.STATE_STARTED, ILaunchManager.RUN_MODE, 1);
			case IEventCodes.STATE_STOPPED:
				return new ManagedUIDecorator().getStateImage(IServer.STATE_STOPPED, ILaunchManager.RUN_MODE, 1);
			case IEventCodes.STATE_TRANSITION:
				return new ManagedUIDecorator().getStateImage(IServer.STATE_STARTING, ILaunchManager.RUN_MODE, 1);
			}
		} else if( (code & IEventCodes.FULL_POLLER_MASK) == IEventCodes.BEHAVIOR_STATE_CODE) {
			switch(code) {
			case IEventCodes.BEHAVIOR_FORCE_STOP:
			case IEventCodes.BEHAVIOR_FORCE_STOP_FAILED:
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			case IEventCodes.BEHAVIOR_PROCESS_TERMINATED:
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			}
		}		
		return null;
	}
	
	public String getText(Object element) {
		if( element instanceof EventCategory ) {
			int type = ((EventCategory)element).getType();
			if( type == 0 ) 
				return Messages.LogLabelProvider_UnknownEventType;
			if( type == IEventCodes.POLLING_CODE)
				return Messages.LogLabelProvider_StartupShutdownEventType;
			if( type == IEventCodes.PUBLISHING_CODE)
				return Messages.LogLabelProvider_PublishingEventType;
		}

		if( element instanceof LogEntry ) {
			return ((LogEntry)element).getMessage();
		}
		return element == null ? "" : element.toString();//$NON-NLS-1$
	}
	
	protected String getSuffix(Object entry2) {
		if( entry2 instanceof LogEntry ) {
			LogEntry entry = (LogEntry)entry2;
			long diff = new Date().getTime() - entry.getDate().getTime();
			long sec = diff / 1000;
			long minutes = sec / 60;
			if( minutes > 0 )
				sec -= (minutes * 60);
			long hours = minutes / 60;
			if( hours > 0 ) {
				minutes -= (hours * 60);
				sec -= (hours * 60 * 60);
			}
			if( hours > 0 ) {
				return MessageFormat.format(Messages.LogLabelProvider_HoursMinutesAgo, hours,
						minutes);
			} else if( minutes > 0 ) {
				return MessageFormat.format(Messages.LogLabelProvider_MinutesSecondsAgo, minutes,
						sec);
			} else {
				return MessageFormat.format(Messages.LogLabelProvider_SecondsAgo, sec);
			}
		}
		return ""; //$NON-NLS-1$
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if( columnIndex == 0 ) 
			return getImage(element);
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(columnIndex == 0)
			return getText(element);
		else
			return getSuffix(element);
	}
}
