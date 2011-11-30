package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.AbstractServerAction;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.tools.common.ui.BrowserUtil;

import com.ibm.icu.text.MessageFormat;

public class ShowInWelcomePageActionProvider extends CommonActionProvider {

	private static final String WELCOME_PAGE_URL_PATTERN = "http://{0}:{1}/"; //$NON-NLS-1$

	private ICommonActionExtensionSite actionSite;
	private OpenWelcomePageAction openWelcomePageAction;

	public void init(ICommonActionExtensionSite site) {
		super.init(site);
		this.actionSite = site;
		createActions(site);
	}

	protected void createActions(ICommonActionExtensionSite site) {
		ICommonViewerWorkbenchSite commonViewerWorkbenchSite = 
				CommonActionProviderUtils.getCommonViewerWorkbenchSite(site);
		if (commonViewerWorkbenchSite != null) {
			openWelcomePageAction = 
					new OpenWelcomePageAction(commonViewerWorkbenchSite.getSelectionProvider());
		}
	}

	public void fillContextMenu(IMenuManager menu) {
		CommonActionProviderUtils.addToShowInQuickSubMenu(openWelcomePageAction, menu, actionSite);
	}

	private static class OpenWelcomePageAction extends AbstractServerAction {
		public OpenWelcomePageAction(ISelectionProvider sp) {
			super(sp, Messages.ShowInWelcomePage_Action_Text);
		}

		public boolean accept(IServer server) {
			return (ServerUtil.isJBoss7(server)
					&& 	server.getServerState() == IServer.STATE_STARTED);
		}
		
		public void perform(IServer server) {
			try {
				JBossServer jbossServer = ServerUtil.checkedGetServerAdapter(server, JBossServer.class);
				String host = jbossServer.getHost();
				int webPort = jbossServer.getJBossWebPort();
				String consoleUrl = MessageFormat.format(WELCOME_PAGE_URL_PATTERN, host, String.valueOf(webPort));
				BrowserUtil.checkedCreateInternalBrowser(
						consoleUrl, server.getName(), JBossServerUIPlugin.PLUGIN_ID, JBossServerUIPlugin.getDefault()
								.getLog());
			} catch (CoreException e) {
				IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, e.getMessage(), e);
				JBossServerUIPlugin.log(status);
				ErrorDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
						, Messages.ShowInAction_Error_Title
						, NLS.bind(Messages.ShowInAction_Error, Messages.ShowInWelcomePage_Action_Text), status);
			}
		}
	}
}
