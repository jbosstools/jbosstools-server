package org.jboss.ide.eclipse.as.core.server;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;

public class UnitedServerListenerManager implements 
	IServerLifecycleListener, IServerListener, IPublishListener {
	protected static UnitedServerListenerManager instance;
	public static UnitedServerListenerManager getDefault() {
		if( instance == null )
			instance = new UnitedServerListenerManager();
		return instance;
	}
	
	protected ArrayList<UnitedServerListener> list;
	protected UnitedServerListenerManager() {
		list = new ArrayList<UnitedServerListener>();
		ServerCore.addServerLifecycleListener(this);
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			allServers[i].addServerListener(this);
			allServers[i].addPublishListener(this);
		}
	}
	
	
	public void addListener(UnitedServerListener listener) {
		if( !list.contains(listener)) {
			list.add(listener);
			IServer[] allServers = ServerCore.getServers();
			for( int i = 0; i < allServers.length; i++ )
				listener.init(allServers[i]);
		}
	}
	public void removeListener(UnitedServerListener listener) {
		list.remove(listener);
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ )
			listener.cleanUp(allServers[i]);
	}

	public void serverAdded(IServer server) {
		server.addServerListener(this);
		server.addPublishListener(this);
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverAdded(server);
		}
	}
	public void serverChanged(IServer server) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverChanged(server);
		}
	}
	public void serverRemoved(IServer server) {
		server.removeServerListener(this);
		server.removePublishListener(this);
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverRemoved(server);
		}
	}
	
	public void serverChanged(ServerEvent event) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) {
			i.next().serverChanged(event);
		}
	}

	public void publishStarted(IServer server) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) 
			i.next().publishStarted(server);
	}

	public void publishFinished(IServer server, IStatus status) {
		for( Iterator<UnitedServerListener> i = list.iterator(); i.hasNext(); ) 
			i.next().publishFinished(server, status);
	}
	
}
