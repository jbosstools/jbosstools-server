package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DelegatingJBoss7ServerBehavior;

public class JBossServerBehaviorUtils {

	public static DelegatingServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		return (DelegatingServerBehavior) server.getAdapter(DelegatingServerBehavior.class);
	}

	public static DelegatingJBoss7ServerBehavior getJBoss7ServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		return (DelegatingJBoss7ServerBehavior) server.getAdapter(DelegatingJBoss7ServerBehavior.class);
	}
}
