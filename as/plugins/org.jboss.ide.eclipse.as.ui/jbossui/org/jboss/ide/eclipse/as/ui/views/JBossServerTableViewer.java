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

public class JBossServerTableViewer extends TreeViewer {
	
	public JBossServerTableViewer(Tree tree) {
		super(tree);
		
		setContentProvider(new ContentProviderDelegator());
		setLabelProvider(new LabelProviderDelegator());
	}

			
	
	protected class LabelProviderDelegator extends LabelProvider {
		public String getText(Object obj) {
			if( obj instanceof JBossServer) {
				JBossServer server = (JBossServer)obj;
				String ret = server.getServer().getName(); 
//				ret += "  (";
//				String home = server.getRuntimeConfiguration().getServerHome(); 
//				ret += (home.length() > 30 ? home.substring(0,30) + "..." : home);
//				ret += ", " + server.getRuntimeConfiguration().getJbossConfiguration() + ")";
				return ret;
			}
			if( obj instanceof ServerViewProvider) {
				return ((ServerViewProvider)obj).getName();
			}
			
			try {
				return getParentViewProvider(obj).getDelegate().getLabelProvider().getText(obj);
			} catch( Exception e) {
			}
			return obj.toString();
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
		action1.setText("refresh viewer");
		
		menu.add(action1);
	}

}
