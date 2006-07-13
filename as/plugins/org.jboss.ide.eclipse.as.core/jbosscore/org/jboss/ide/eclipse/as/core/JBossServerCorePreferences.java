package org.jboss.ide.eclipse.as.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ServerType;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class JBossServerCorePreferences {
	private static JBossServerCorePreferences prefs;
	public static JBossServerCorePreferences getDefault() {
		if( prefs == null ) {
			prefs = new JBossServerCorePreferences();
			setMaxTimeout();
		}
		return prefs;
	}
	
	
	public JBossServerCorePreferences() {
	}
	
	public static void setMaxTimeout() {
		IServerType[] types = ServerCore.getServerTypes();
		int maxStart = 0;
		int maxStop = 0;

		for( int i = 0; i < types.length; i++ ) {
			if( types[i].getId().equals("org.jboss.ide.eclipse.as.40") || 
					types[i].getId().equals("org.jboss.ide.eclipse.as.32") ) {
				if( ((ServerType)types[i]).getStartTimeout() > maxStart ) {
					maxStart = ((ServerType)types[i]).getStartTimeout();
				}
				if( ((ServerType)types[i]).getStopTimeout() > maxStart ) {
					maxStop = ((ServerType)types[i]).getStopTimeout();
				}
			}
		}
		MAX_TIMEOUT = (maxStart > maxStop ? maxStart : maxStop );
	}
	
	
	public static final int NO_CLIENT_ACTION = 0;
	public static final int VERIFY_CLIENT_ACTION = 1;
	public int getModuleClientAction(IModule module[]) {
		return NO_CLIENT_ACTION;
	}
	
	
	/**
	 * Get the preferences for that plugin from preferneces.
	 * If not set, use the max.
	 */
	protected static int MAX_TIMEOUT;
	public static final String START_TIMEOUT = "_START_TIMEOUT_";
	public static final String STOP_TIMEOUT = "_STOP_TIMEOUT_";
	
	public int getStartTimeout(JBossServer server) {
		if( getPreferences().getInt(server.getServer().getId() + START_TIMEOUT) != 0 ) {
			return getPreferences().getInt(server.getServer().getId() + START_TIMEOUT);
		}
		return MAX_TIMEOUT;
	}
	
	public int getStopTimeout(JBossServer server) {
		if( getPreferences().getInt(server.getServer().getId() + STOP_TIMEOUT) != 0 ) {
			return getPreferences().getInt(server.getServer().getId() + STOP_TIMEOUT);
		}
		return MAX_TIMEOUT;
	}
	
	public Preferences getPreferences() {
		return JBossServerCorePlugin.getDefault().getPluginPreferences();
	}
}
