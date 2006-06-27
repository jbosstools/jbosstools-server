package org.jboss.ide.eclipse.as.ui.viewproviders;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEventRoot;
import org.jboss.ide.eclipse.as.core.server.IServerLogListener;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.JBossServerView;

public class EventLogViewProvider extends JBossServerViewExtension implements IServerLogListener {

	protected EventLogLabelProvider categoryLabelProvider;
	protected EventLogContentProvider categoryContentProvider;
	
	public EventLogViewProvider() {
		categoryLabelProvider = new EventLogLabelProvider();
		categoryContentProvider = new EventLogContentProvider();
	}
	
	
	public void enable() {
		ServerProcessModel.getDefault().addLogListener(this);
	}
	
	public void disable() {
		ServerProcessModel.getDefault().removeLogListener(this);
	}
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
	}

	public ITreeContentProvider getContentProvider() {
		return categoryContentProvider;
	}

	public LabelProvider getLabelProvider() {
		return categoryLabelProvider;
	}

	protected class EventLogLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if( obj instanceof ProcessLogEvent ) {
				ProcessLogEvent event = ((ProcessLogEvent)obj);
				SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.S");
				return event.getText() + "   " + format.format(new Date(event.getDate()));
			}
			return obj.toString();
		}
		public Image getImage(Object obj) {
			ProcessLogEvent event = (ProcessLogEvent)obj;
			IServer server = event.getRoot().getServer();
			IServerType serverType = server.getServerType();

			if( obj instanceof ProcessLogEvent ) {
				if( event.getEventType() == ProcessLogEvent.SERVER_STARTING) {
					return getStateImage(serverType, IServer.STATE_STARTING, server.getMode());
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_STOPPING) {
					return getStateImage(serverType, IServer.STATE_STOPPING, server.getMode());
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_UP) {
					return getStateImage(serverType, IServer.STATE_STARTED, server.getMode());					
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_DOWN) {
					return getStateImage(serverType, IServer.STATE_STOPPED, server.getMode());					
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_CONSOLE) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.CONSOLE_IMAGE);
				}
				if( event.getEventType() == ProcessLogEvent.TWIDDLE) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.TWIDDLE_IMAGE);
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_PUBLISH) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_UNPUBLISH) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.UNPUBLISH_IMAGE);
				}
			}
			return null;
		}
		
		protected Image getStateImage(IServerType serverType, int state, String mode) {
			return UIDecoratorManager.getUIDecorator(serverType).getStateImage(state, mode, 0);
		}

	}
	
	protected class EventLogContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		protected ProcessLogEventRoot input;
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if( newInput instanceof IServer && JBossServerCore.getServer((IServer)newInput) != null ) {
				JBossServer s = JBossServerCore.getServer((IServer)newInput);
				input = s.getProcessModel().getEventLog();
			}
		}

		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider ) {
				return input.getChildren();
			}
			if( parentElement instanceof ProcessLogEvent ) {
				return ((ProcessLogEvent)parentElement).getChildren();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if( element instanceof ProcessLogEventRoot ) {
				return provider;
			}
			if( element instanceof ProcessLogEvent ) {
				return ((ProcessLogEvent)element).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}
	}


	public void logChanged(ProcessLogEvent event) {
		IServer s = JBossServerView.getDefault().getSelectedServer();
		if( event.getRoot().getServer().equals(s))
			refreshViewer();
	}


	public IPropertySheetPage getPropertySheetPage() {
		return null;
	}

}
