package org.jboss.ide.eclipse.as.ui.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.eclipse.wst.server.ui.internal.view.servers.ServerTableLabelProvider;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.server.IServerLogListener;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;

public class JBossServerTableViewer extends TreeViewer implements IServerLogListener {
	
	public JBossServerTableViewer(Tree tree) {
		super(tree);
		
		setContentProvider(new ContentProviderDelegator());
		setLabelProvider(new LabelProviderDelegator());
		ServerProcessModel.getDefault().addLogListener(this);

	}

			
	
	protected class LabelProviderDelegator extends LabelProvider {
		public String getText(Object obj) {
			if( obj instanceof JBossServer) {
				JBossServer server = (JBossServer)obj;
				String ret = server.getServer().getName(); 
				ret += "  (";
				String home = server.getRuntimeConfiguration().getServerHome(); 
				ret += (home.length() > 30 ? home.substring(0,30) + "..." : home);
				ret += ", " + server.getRuntimeConfiguration().getJbossConfiguration() + ")";
				return ret;
			}
			if( obj instanceof ServerViewProvider) {
				return ((ServerViewProvider)obj).getName();
			}
			
			try {
				return getParentViewProvider(obj).getDelegate().getLabelProvider().getText(obj);
			} catch( Exception e) {
			}
			return "not parsable: " + obj.toString();
		}
		public Image getImage(Object obj) {
			if( obj instanceof JBossServer ) {
				return ServerUICore.getLabelProvider().getImage(((JBossServer)obj).getServer());				
			}
			try {
				return getParentViewProvider(obj).getDelegate().getLabelProvider().getImage(obj);
			} catch( Exception e) {
			}
			return null;
		}
	}
	protected class ContentProviderDelegator implements ITreeContentProvider {
		
		public ContentProviderDelegator() {
		}

		public Object[] getElements(Object inputElement) {
			return new Object[] { JBossServerCore.getServer((IServer)inputElement) };
		}

		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof JBossServer) {
				return JBossServerUIPlugin.getDefault().getEnabledViewProviders();
			}
			if( parentElement instanceof ServerViewProvider) {
				return ((ServerViewProvider)parentElement).getDelegate().getContentProvider().getChildren(parentElement);
			}
			
			Object[] o = null;
			try {
				o = getParentViewProvider(parentElement).getDelegate().getContentProvider().getChildren(parentElement);
			} catch( Exception e) {
			}
			
			return o == null ? new Object[0] : o;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			ServerViewProvider[] providers = JBossServerUIPlugin.getDefault().getEnabledViewProviders();
			for( int i = 0; i < providers.length; i++ ) {
				providers[i].getDelegate().getContentProvider().inputChanged(viewer, oldInput, newInput);
			}
		}
		
	}

	public ServerViewProvider getParentViewProvider(Object o) {
		ServerViewProvider[] providers = JBossServerUIPlugin.getDefault().getEnabledViewProviders();
		for( int i = 0; i < providers.length; i++ ) {
			if( containsObject(providers[i], o)) {
				return providers[i];
			}
		}
		return null;
	}
	
	public boolean containsObject(ServerViewProvider provider, Object obj) {
		Object parent = provider.getDelegate().getContentProvider().getParent(obj);
		while( parent != null && !(parent instanceof ServerViewProvider)) {
			parent = provider.getDelegate().getContentProvider().getParent(parent);
		}
		
		if( parent instanceof ServerViewProvider ) 
			return true;
		
		return false;
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
			if( obj instanceof JBossServer) {
				JBossServer server = (JBossServer)obj;
				String ret = server.getServer().getName(); 
				ret += "  (";
				String home = server.getRuntimeConfiguration().getServerHome(); 
				ret += (home.length() > 30 ? home.substring(0,30) + "..." : home);
				ret += ", " + server.getRuntimeConfiguration().getJbossConfiguration() + ")";
				return ret;
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
			if( obj instanceof JBossServer) {
				return ServerUICore.getLabelProvider().getImage(((JBossServer)obj).getServer());
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
			//list.toArray(topLevelCategories);
		}


		public Object[] getElements(Object inputElement) {
			return new Object[] { JBossServerCore.getServer((IServer)inputElement) };
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			
		}

		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof JBossServer ) {
				return topLevelCategories;
			}
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
	
	
	
	protected void fillJBContextMenu(Shell shell, IMenuManager menu) {
		Action action1 = new Action() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						refresh();
					} 
					
				});
			}
			
		};
		action1.setText("refresh");
		
		menu.add(action1);
	}

	public void logChanged(ServerProcessModelEntity ent) {
		final ServerProcessModelEntity e2 = ent;
		
		IServer input = getInput() instanceof IServer ? (IServer)getInput() : null;
		if( input != null && input.getId().equals(ent.getServerID())) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						Object[] expanded = getExpandedElements();
						ISelection selected = getSelection();
						//viewer.setInput(ServerProcessModel.getDefault());
						refresh();
						setExpandedElements(expanded);
						setSelection(selected);
					} catch( Exception e) {
						// do nothing
					}
				} 
			} );
		}
	}

}
