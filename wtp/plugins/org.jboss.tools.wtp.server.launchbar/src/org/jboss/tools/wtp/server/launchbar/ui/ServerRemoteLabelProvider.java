package org.jboss.tools.wtp.server.launchbar.ui;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.tools.wtp.server.launchbar.RemoteConnectionHandler;

public class ServerRemoteLabelProvider extends org.eclipse.jface.viewers.LabelProvider {

	public ServerRemoteLabelProvider() {
	}
	
	public Image getImage(Object element) {
		if( element instanceof IRemoteConnection) {
			IRemoteConnection rc = (IRemoteConnection)element;
			String serverId = rc.getAttribute(RemoteConnectionHandler.SERVER_ID);
			if( serverId != null ) {
				IServer s = ServerCore.findServer(serverId);
				if( s != null ) {
					return ServerUICore.getLabelProvider().getImage(s);
				}
			}
		}
		return null;
	}
}
