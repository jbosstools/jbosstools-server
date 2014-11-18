package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.io.File;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.systems.IPortsController;
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
			ControllableServerBehavior beh = (ControllableServerBehavior) JBossServerBehaviorUtils.getControllableBehavior(server);
			IPortsController controller = (IPortsController) beh.getController(IPortsController.SYSTEM_ID);
			return controller.findPort(IPortsController.KEY_MANAGEMENT_PORT, -1);
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
