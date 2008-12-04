package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.AbstractServerAction;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnection;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.internal.views.navigator.JMXNavigator;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerContentProvider;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerLabelProvider;

public class JMXProvider {

	public static class ActionProvider extends CommonActionProvider {
		private ICommonActionExtensionSite actionSite;
		private ShowInJMXViewAction showInJMXViewAction;
		public ActionProvider() {
			super();
		}
		
		public void init(ICommonActionExtensionSite aSite) {
			super.init(aSite);
			this.actionSite = aSite;
			createActions(aSite);
		}

		protected void createActions(ICommonActionExtensionSite aSite) {
			ICommonViewerSite site = aSite.getViewSite();
			if( site instanceof ICommonViewerWorkbenchSite ) {
				StructuredViewer v = aSite.getStructuredViewer();
				if( v instanceof CommonViewer ) {
					CommonViewer cv = (CommonViewer)v;
					ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
					showInJMXViewAction = new ShowInJMXViewAction(wsSite.getSelectionProvider());
				}
			}
		}

		public void fillContextMenu(IMenuManager menu) {
			ICommonViewerSite site = actionSite.getViewSite();
			IStructuredSelection selection = null;
			if (site instanceof ICommonViewerWorkbenchSite) {
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
				selection = (IStructuredSelection) wsSite.getSelectionProvider()
						.getSelection();
			}
			if( selection != null && selection.toArray().length == 1 ) {
				if( selection.getFirstElement() instanceof IServer ) {
					if( menu instanceof MenuManager ) {
						MenuManager mgr = (MenuManager)menu;
						IContributionItem[] items = mgr.getItems();
						for( int i = 0; i < items.length; i++ ) {
							if( "org.eclipse.ui.navigate.showInQuickMenu".equals(items[i].getId())) {
								((MenuManager)items[i]).add(showInJMXViewAction);
							}
						}
					}
				}
			}
		}
		
		public class ShowInJMXViewAction extends AbstractServerAction {
			public ShowInJMXViewAction(ISelectionProvider sp) {
				super(sp, null);
				
				IViewRegistry reg = PlatformUI.getWorkbench().getViewRegistry();
				IViewDescriptor desc = reg.find(JMXNavigator.VIEW_ID);
				setText(desc.getLabel());
				setImageDescriptor(desc.getImageDescriptor());
			}

			public boolean accept(IServer server) {
				return (server.getServerType() != null && 
						server.loadAdapter(JBossServer.class, new NullProgressMonitor()) != null
						&& server.getServerState() == IServer.STATE_STARTED);
			}

			public void perform(final IServer server) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow() ;
				if (window != null) {
					IWorkbenchPage page = window.getActivePage();
					if (page != null) {
						IWorkbenchPart part = page.findView(JMXNavigator.VIEW_ID);
						if (part == null) {
							try {
								part = page.showView(JMXNavigator.VIEW_ID);
							} catch (PartInitException e) {
							}
						} else /* if( part != null ) */ {
							final JMXNavigator view = (JMXNavigator) part.getAdapter(JMXNavigator.class);
							if (view != null) {
								view.setFocus();
								Display.getDefault().asyncExec(new Runnable() { 
									public void run() {
										JBossServerConnectionProvider provider = 
											(JBossServerConnectionProvider)ExtensionManager.getProvider(
													JBossServerConnectionProvider.PROVIDER_ID);
										JBossServerConnection connection = provider.getConnection(server);
										if( connection != null ) {
											view.getCommonViewer().collapseAll();
											ISelection sel = new StructuredSelection(new Object[] { connection });
											view.getCommonViewer().setSelection(sel, true);
											view.getCommonViewer().expandToLevel(connection, 2);
										}
									}
								});
							}
						}
					}
				}
			}
		}
	}
	
	public static class ContentProvider implements ITreeContentProvider {
		private MBeanExplorerContentProvider delegate;
		public ContentProvider() {
			delegate = new MBeanExplorerContentProvider();
		}
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof IServer ) {
				Object sel = JBossServerConnectionProvider.getConnection((IServer)parentElement);
				if( sel != null )
					return new Object[] { sel };
			}
			return delegate.getChildren(parentElement);
		}
		public Object getParent(Object element) {
			return delegate.getParent(element);
		}
		public boolean hasChildren(Object element) {
			return delegate.hasChildren(element);
		}
		public Object[] getElements(Object inputElement) {
			return delegate.getElements(inputElement);
		}
		public void dispose() {
			delegate.dispose();
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			delegate.inputChanged(viewer, oldInput, newInput);
		}
	}
	
	public static  class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		private MBeanExplorerLabelProvider delegate;
		public LabelProvider() {
			delegate = new MBeanExplorerLabelProvider();
		}
		
		public void dispose() {
			delegate.dispose();
		}
		
		public String getText(Object element) {
			if( element instanceof IConnectionWrapper ) {
				return "JMX"; 
			}
			return delegate.getText(element);
		}
		
		public Image getImage(Object element) {
			if( element instanceof IConnectionWrapper ) {
				return JBossServerUISharedImages.getImage(JBossServerUISharedImages.JMX_IMAGE); 
			}
			return delegate.getImage(element);
		}
	}
	
}
