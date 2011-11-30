package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.AbstractServerAction;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.tools.common.ui.BrowserUtil;

import com.ibm.icu.text.MessageFormat;

public class OpenManagementConsoleActionProvider extends CommonActionProvider {

	private static final String CONSOLE_URL_PATTERN = "http://{0}:{1}/console";

	private ICommonActionExtensionSite actionSite;
	private OpenWebManagementConsoleAction openWebManagementConsoleAction;

	public void init(ICommonActionExtensionSite site) {
		super.init(site);
		this.actionSite = site;
		createActions(site);
	}

	protected void createActions(ICommonActionExtensionSite site) {
		ICommonViewerWorkbenchSite commonViewerWorkbenchSite = 
				CommonActionProviderUtils.getCommonViewerWorkbenchSite(site);
		if (commonViewerWorkbenchSite != null) {
			openWebManagementConsoleAction = 
					new OpenWebManagementConsoleAction(commonViewerWorkbenchSite.getSelectionProvider());
		}
	}

	public void fillContextMenu(IMenuManager menu) {
		CommonActionProviderUtils.addToShowInQuickSubMenu(openWebManagementConsoleAction, menu, actionSite);
	}

	private static class OpenWebManagementConsoleAction extends AbstractServerAction {
		public OpenWebManagementConsoleAction(ISelectionProvider sp) {
			super(sp, Messages.OpenWebManagementConsole_Action_Text);
			setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.CONSOLE));
		}

		public boolean accept(IServer server) {
			return (ServerUtil.isJBoss7(server)
					&& 	server.getServerState() == IServer.STATE_STARTED);
		}
		
		public void perform(IServer server) {
			JBossServer jbossServer;
			try {
				jbossServer = ServerUtil.checkedGetServerAdapter(server, JBossServer.class);
				String host = jbossServer.getHost();
				int webPort = jbossServer.getJBossWebPort();
				String consoleUrl = MessageFormat.format(CONSOLE_URL_PATTERN, host, String.valueOf(webPort));
				BrowserUtil.checkedCreateInternalBrowser(
						consoleUrl, server.getName(), JBossServerUIPlugin.PLUGIN_ID, JBossServerUIPlugin.getDefault()
								.getLog());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
