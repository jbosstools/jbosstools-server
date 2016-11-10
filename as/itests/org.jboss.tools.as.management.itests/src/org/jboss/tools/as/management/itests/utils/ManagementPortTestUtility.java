package org.jboss.tools.as.management.itests.utils;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.tools.as.test.core.internal.utils.JREUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;

/**
 * Discover the management port for a given server
 * @author rob
 *
 */
public class ManagementPortTestUtility {
	public static int getManagementPort(String serverHome, String javaHome) {
		try {
			String serverType = ParameterUtils.getServerType(serverHome);
			IVMInstall jre = JREUtils.findOrCreateJRE(new Path(javaHome));
			IServer server = ServerCreationTestUtils.createServerWithRuntime(serverType, serverType, new File(serverHome), jre);
			JBoss7Server jb7s = (JBoss7Server)server.loadAdapter(JBoss7Server.class, new NullProgressMonitor());
			int mgmtPort = jb7s.getManagementPort();
			return mgmtPort;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ServerCreationTestUtils.deleteAllServersAndRuntimes();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
}
