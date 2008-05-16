package org.jboss.tools.wst.server.ui;

import java.util.ArrayList;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

public class ToolsServerUICore {

	public static ServerViewProvider[] getEnabledViewProviders(IServer server) {
		ServerViewProvider[] serverViewExtensions = getAllServerViewProviders();
		ArrayList<ServerViewProvider> list = new ArrayList<ServerViewProvider>();
		for( int i = 0; i < serverViewExtensions.length; i++ ) {
			if( serverViewExtensions[i].isEnabled() && serverViewExtensions[i].supports(server)) {
				list.add(serverViewExtensions[i]);
			}
		}
		return list.toArray(new ServerViewProvider[list.size()]);
	}
	
	public static ServerViewProvider[] getAllServerViewProviders() {
		return ExtensionManager.getDefault().getAllServerViewProviders();
	}
}
