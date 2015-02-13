package org.jboss.tools.as.management.itests.utils;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;

/**
 * Discover the management port for a given server
 * @author rob
 *
 */
public class ManagementPortTestUtility {
	public static int getManagementPort(String serverHome) {
		try {
			String serverType = ParameterUtils.getServerType(serverHome);
			IServer server = ServerCreationTestUtils.createServerWithRuntime(serverType, serverType, new File(serverHome));
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
