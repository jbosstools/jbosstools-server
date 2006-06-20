package org.jboss.ide.eclipse.as.ui.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.eclipse.wst.server.ui.internal.view.servers.ServerTableLabelProvider;
import org.eclipse.wst.server.ui.internal.view.servers.ServerTableViewer;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

public class JBossServerTableViewer extends TreeViewer {

	private ServerTableViewer serverViewer;
	
	
	public JBossServerTableViewer(Tree tree, ServerTableViewer serverViewer) {
		super(tree);
		this.serverViewer = serverViewer;
		
		addListeners();
		setContentProvider(new JBSContentProvider());
		setLabelProvider(new JBSLabelProvider());
	}

	
	protected void addListeners() {
		serverViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object current = getInput();
				Object selection = ((TreeSelection)event.getSelection()).getFirstElement();
				Object server = selection;
				if( selection instanceof ModuleServer ) {
					server = ((ModuleServer)selection).server;
				}
				
				if( getInput() != server )
					setInput(server);
			} 
			
		});
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
	
	protected class JBSLabelProvider extends LabelProvider {
		private EventLogLabelProvider logDelegate;
		private ServerTableLabelProvider moduleDelegate;
		
		public JBSLabelProvider() {
			logDelegate = new EventLogLabelProvider();
			moduleDelegate = new ServerTableLabelProvider();
		}
		
		public String getText(Object obj) {
			if( obj instanceof ProcessLogEvent) {
				return logDelegate.getText(obj);
			}
			if( obj instanceof IModule ) {
				return ServerUICore.getLabelProvider().getText(obj);
			}
			return obj.toString();
		}
		public Image getImage(Object obj) {
			if( obj instanceof ProcessLogEvent) {
				return logDelegate.getImage(obj);
			}
			if( obj instanceof IModule ) {
				return ServerUICore.getLabelProvider().getImage(obj);
			}
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}

	}
	
	protected class JBSContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		
		protected String[] topLevelCategories;
		public JBSContentProvider() {
			initializeCategories();
		}
		protected void initializeCategories() {
			ArrayList list = new ArrayList();
			
			list.add(Messages.ModulesCategory);
			list.add(Messages.EventLogCategory);
			
			
			topLevelCategories = new String[list.size()];
			list.toArray(topLevelCategories);
		}


		public Object[] getElements(Object inputElement) {
			return topLevelCategories;
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}

		public Object[] getChildren(Object parentElement) {
			if( parentElement.equals(Messages.ModulesCategory)) {
				IServer server = (IServer)getInput();
				return server.getModules();
			}
			if( parentElement.equals(Messages.EventLogCategory)) {
				IServer server = (IServer)getInput();
				ServerProcessModelEntity ent = ServerProcessModel.getDefault().getModel(server.getId());
				return ent.getEventLog().getChildren();
			}
			if( parentElement instanceof ProcessLogEvent ) {
				return ((ProcessLogEvent)parentElement).getChildren();
			}
			
			return new Object[0];
		}

		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}
		
	}
}
