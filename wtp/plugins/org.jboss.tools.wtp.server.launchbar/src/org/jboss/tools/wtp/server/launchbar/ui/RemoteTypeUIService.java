package org.jboss.tools.wtp.server.launchbar.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.tools.wtp.server.launchbar.RemoteConnectionHandler;

public class RemoteTypeUIService extends AbstractRemoteUIConnectionService {
	private IRemoteConnectionType connectionType;
	public RemoteTypeUIService(IRemoteConnectionType connectionType) {
		this.connectionType = connectionType;
	}
	
	@Override
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		// stub do not implement
		return null;
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}
	
	public static class Factory implements IRemoteConnectionType.Service.Factory {

		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIConnectionService.class.equals(service)) {
				return (T) new RemoteTypeUIService(connectionType);
			}
			return null;
		}
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() { 
			@Override
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
			@Override
			public String getText(Object element) {
				if( element instanceof IRemoteConnection) {
					return ((IRemoteConnection)element).getName();
				}
				return element.toString();
			}
		};
	}
}
