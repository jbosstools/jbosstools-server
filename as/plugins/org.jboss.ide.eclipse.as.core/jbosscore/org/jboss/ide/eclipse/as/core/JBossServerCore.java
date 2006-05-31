package org.jboss.ide.eclipse.as.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

/**
 * 
 * @author rstryker
 *
 */
public class JBossServerCore implements IServerLifecycleListener, IRuntimeLifecycleListener {

	/*
	 * Static portion
	 */
	private static JBossServerCore instance;
	
	/*
	 * Links to other models
	 */
	private ModuleModel moduleModel;
	private ServerProcessModel processModel;
	private String[] jbossFactories;
	private ArrayList consoleListeners;
	
	public static JBossServerCore getDefault() {
		if( instance == null ) {
			instance = new JBossServerCore();
		}
		return instance;
	}
	
	public static JBossServer getServer(IServer server) {
		JBossServer jbServer = (JBossServer)server.getAdapter(JBossServer.class);
		if (jbServer == null) {
			jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}
		return jbServer;
	}
	
	/**
	 * Return all JBossServer instances from the ServerCore
	 * @return
	 */
	public static JBossServer[] getAllJBossServers() {
		ArrayList servers = new ArrayList();
		IServer[] iservers = ServerCore.getServers();
		for( int i = 0; i < iservers.length; i++ ) {
			if( getServer(iservers[i]) != null ) {
				servers.add(getServer(iservers[i]));
			}
		}
		JBossServer[] ret = new JBossServer[servers.size()];
		servers.toArray(ret);
		return ret;
	}
	
	public JBossServerCore() {
		ServerCore.addRuntimeLifecycleListener(this);
		ServerCore.addServerLifecycleListener(this);
		
		moduleModel = ModuleModel.getDefault();
		processModel = ServerProcessModel.getDefault();
		consoleListeners = new ArrayList();
	}

	
	
	
	/*
	 * May implement these methods later on. For now, do nothing.
	 */
	public void serverAdded(IServer server) {
		ASDebug.p("serverAdded", JBossServerCore.class);
	}

	public void serverChanged(IServer server) {
		//ASDebug.p("serverChanged", JBossServerCore.class);
	}

	public void serverRemoved(IServer server) {
		ASDebug.p("serverRemoved", JBossServerCore.class);
	}


	public void runtimeAdded(IRuntime runtime) {
		ASDebug.p("runtimeAdded", JBossServerCore.class);
	}

	public void runtimeChanged(IRuntime runtime) {
		ASDebug.p("runtimeChanged", JBossServerCore.class);
	}

	public void runtimeRemoved(IRuntime runtime) {
		ASDebug.p("runtimeRemoved", JBossServerCore.class);		
	}
}
