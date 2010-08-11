package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
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
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.ui.views.ServerLogView;

public class ServerLogActionProvider extends CommonActionProvider {
	private ICommonActionExtensionSite actionSite;
	private ShowInServerLogAction showInServerLogAction;
	public ServerLogActionProvider() {
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
				showInServerLogAction = new ShowInServerLogAction(wsSite.getSelectionProvider());
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
		IContributionItem quick = menu.find("org.eclipse.ui.navigate.showInQuickMenu"); //$NON-NLS-1$
		if( quick != null && selection != null && selection.toArray().length == 1 ) {
			if( selection.getFirstElement() instanceof IServer ) {
				if( menu instanceof MenuManager ) {
					((MenuManager)quick).add(showInServerLogAction);
				}
			}
		}
	}
	
	public class ShowInServerLogAction extends AbstractServerAction {
		public ShowInServerLogAction(ISelectionProvider sp) {
			super(sp, null);
			
			IViewRegistry reg = PlatformUI.getWorkbench().getViewRegistry();
			IViewDescriptor desc = reg.find(ServerLogView.VIEW_ID);
			setText(desc.getLabel());
			setImageDescriptor(desc.getImageDescriptor());
		}

		public boolean accept(IServer server) {
			return (server.getServerType() != null && 
					server.loadAdapter(IDeployableServer.class, new NullProgressMonitor()) != null);
		}

		public void perform(IServer server) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow() ;
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IWorkbenchPart part = page.findView(ServerLogView.VIEW_ID);
					if (part == null) {
						try {
							part = page.showView(ServerLogView.VIEW_ID);
						} catch (PartInitException e) {
						}
					} 
					if (part != null) {
						ServerLogView view = (ServerLogView) part.getAdapter(ServerLogView.class);
						if (view != null) {
							view.setFocus();
							view.setServer(server);
						}
					}
				}
			}
		}
	}
}
