package org.jboss.ide.eclipse.as.jmx.integration;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunner;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JMXPollerModel implements IServerJMXRunner {

	private String user, pass;
	public void setUser(String user) {
		this.user = user;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	
	
	public void run(IServer s, IJMXRunnable r, String user, String pass) throws JMXException {
		IConnectionWrapper c = findConnection(s);
		try {
			if( c != null ) {
				System.out.println("Not connected");
				if( !c.isConnected()) {
					c.connect();
				}
				c.run(r, getDefaultPreferences(s));
			}
		} catch(IOException ioe) {
			ioe.printStackTrace(); // TODO LOG and handle
		}
	}
	
	protected HashMap<String, String> getDefaultPreferences(IServer s) {
		HashMap<String, String> prefs = new HashMap<String, String>();
		prefs.put("force", "true");
		prefs.put("user", user);
		prefs.put("pass", pass);
		return prefs;
	}
	
	protected IConnectionWrapper findConnection(IServer server) {
		ExtensionManager.getProviders(); // todo clean up, this is here to ensure it's initialized 
		IConnectionWrapper c = JBossJMXConnectionProviderModel.getDefault().getConnection(server);
		return c;
	}
	
	@Override
	public void run(IServer server, IJMXRunnable runnable) throws CoreException {
		run(server, runnable, null, null);
	}

	public void beginTransaction(IServer server, Object lock) {
		AbstractJBossJMXConnectionProvider provider = JBossJMXConnectionProviderModel.getDefault().getProvider(server);
		if( provider != null && provider.hasClassloaderRepository())
			provider.getClassloaderRepository().addConcerned(server, lock);
	}

	public void endTransaction(IServer server, Object lock) {
		AbstractJBossJMXConnectionProvider provider = JBossJMXConnectionProviderModel.getDefault().getProvider(server);
		if( provider != null && provider.hasClassloaderRepository())
			provider.getClassloaderRepository().removeConcerned(server, lock);
	}

}
