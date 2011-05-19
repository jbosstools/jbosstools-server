package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ServerBehavior;

public class JBossServerBehaviorUtils {

	public static JBossServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		return (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
	}

	public static JBoss7ServerBehavior getJBoss7ServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		return (JBoss7ServerBehavior) server.getAdapter(JBoss7ServerBehavior.class);
	}
}
