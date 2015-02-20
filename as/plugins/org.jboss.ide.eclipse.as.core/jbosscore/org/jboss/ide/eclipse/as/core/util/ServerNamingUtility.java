package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.jboss.ide.eclipse.as.core.Messages;

public class ServerNamingUtility {
	public static String getNextShortServerName(IServerType type) {
		if( type.getId().startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX)) {
			String name = type.getName();
			String base = name.replace("JBoss Enterprise Application Platform", "JBoss EAP");  //$NON-NLS-1$//$NON-NLS-2$
			return getDefaultServerName(base);
		}
		return getDefaultServerName(type.getName());
	}
	
	
	public static String getDefaultServerName(IRuntime rt) {
		String runtimeName = rt.getName();
		String base = null;
		if( runtimeName == null || runtimeName.equals("")) { //$NON-NLS-1$
			IRuntimeType rtt = rt.getRuntimeType();
			base = NLS.bind(Messages.serverVersionName, rtt == null ? null : rtt.getVersion());
		} else 
			base = NLS.bind(Messages.serverName, runtimeName);
		
		return getDefaultServerName( base);
	}
	
	public static String getDefaultServerName( String base) {
		if( ServerUtil.findServer(base) == null ) return base;
		int i = 1;
		while( ServerUtil.findServer(
				NLS.bind(Messages.serverCountName, base, i)) != null )
			i++;
		return NLS.bind(Messages.serverCountName, base, i);
	}
}
