package org.jboss.tools.as.ui.bot.itests.parametized;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.reddeer.eclipse.wst.server.ui.RuntimePreferencePage;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.Server;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.runtime.core.model.RuntimePath;
import org.jboss.tools.runtime.ui.RuntimeUIActivator;

public class CleanEnvironmentUtils {
	private static boolean USE_UI_DEFAULT = false;

	public static void cleanAll() {
		cleanAll(USE_UI_DEFAULT);
	}
	
	public static void cleanAll(boolean ui) {
		cleanPaths();
		cleanServers(ui);
		cleanServerRuntimes(ui);
		deleteAllProjects();
	}
	
	public static void cleanPaths() {
		// make sure that paths were removed
		for (RuntimePath path : RuntimeUIActivator.getDefault().getModel().getRuntimePaths()) {
			RuntimeUIActivator.getDefault().getModel().removeRuntimePath(path);
		}
	}


	public static void deleteAllProjects() {
		IProject[] all = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(int i = 0; i < all.length; i++ ) {
			try {
				all[i].delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void deleteServersAndRuntimes() {
		deleteServersAndRuntimes(USE_UI_DEFAULT);
	}
	
	public static void deleteServersAndRuntimes(boolean ui) {
		cleanServers(ui);
		cleanServerRuntimes(ui);
	}

	public static void cleanServerRuntimes() {
		cleanServerRuntimes(USE_UI_DEFAULT);
	}
	
	public static void cleanServerRuntimes(boolean ui) {
		if( ui ) {
			WorkbenchPreferenceDialog preferenceDialog = new WorkbenchPreferenceDialog();
			preferenceDialog.open();
			RuntimePreferencePage runtimePage = new RuntimePreferencePage(preferenceDialog);
			preferenceDialog.select(runtimePage);
			runtimePage.removeAllRuntimes();
			preferenceDialog.ok();
		} else {
			IRuntime[] all = ServerCore.getRuntimes();
			for( int i = 0; i < all.length; i++ ) {
				try {
					all[i].delete();
				} catch(CoreException ce) {
					ce.printStackTrace();
				}
			}
		}
	}

	public static void cleanServers() {
		cleanServers(false);
	}
	
	public static void cleanServers(boolean ui) {
		if( ui ) {
			ServersView2 serversView = new ServersView2();
			serversView.open();
			List<Server> servers = serversView.getServers();
			for (Server server : servers) {
				server.delete();
			}
		} else {

			IServer[] all = ServerCore.getServers();
			for( int i = 0; i < all.length; i++ ) {
				try {
					all[i].delete();
				} catch(CoreException ce) {
					ce.printStackTrace();
				}
			}
		}
	}
}
