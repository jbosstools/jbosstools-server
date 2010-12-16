package org.jboss.ide.eclipse.as.test.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class JBossServerAPITest extends ServerRuntimeUtils {
	protected IServer currentServer;
	public void setUp() {
	}
	
	public void tearDown() {
		try {
			if( currentServer != null )
				currentServer.delete();
		} catch( CoreException ce ) {
			// report
		}
	}
	public void testJBossServerGetConfigDirectory() {
		try {
			currentServer = create42Server();
			JBossServer jbs = (JBossServer)currentServer.getAdapter(JBossServer.class);
			IRuntime rt = currentServer.getRuntime();
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			String configName = jbsrt.getJBossConfiguration();
			assertTrue(jbs.getConfigDirectory().endsWith(configName));
		} catch( CoreException ce ) {
			fail("Failed during setUp for " + getName() + ": " + ce.getMessage());
		}
	}

}
