package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.AbstractServerAction;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
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
		ICommonViewerWorkbenchSite commonViewerWorkbenchSite =
				CommonActionProviderUtils.getCommonViewerWorkbenchSite(aSite);
		if (commonViewerWorkbenchSite != null) {
			showInServerLogAction = new ShowInServerLogAction(commonViewerWorkbenchSite.getSelectionProvider());
		}
	}

	public void fillContextMenu(IMenuManager menu) {
		CommonActionProviderUtils.addToShowInQuickSubMenu(showInServerLogAction, menu, actionSite);
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
			return (server.getServerType() != null && server.loadAdapter(IDeployableServer.class,
					new NullProgressMonitor()) != null);
		}

		public void perform(IServer server) {
			try {
				IWorkbenchPart part = CommonActionProviderUtils.showView(ServerLogView.VIEW_ID);
				if (part != null) {
					ServerLogView view = (ServerLogView) part.getAdapter(ServerLogView.class);
					if (view != null) {
						view.setFocus();
						view.setServer(server);
					}
				}
			} catch (PartInitException e) {
				JBossServerUIPlugin.log("could not show view " + ServerLogView.VIEW_ID, e);
			}
		}
	}
}
