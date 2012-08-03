package org.jboss.tools.as.test.core.utiltests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.Test;

public class UnitedServerListenerTest extends TestCase {
	@Test
	public void testExistingServerGetsUpdates() throws CoreException {
		final boolean[] changes = new boolean[]{false,false};
		IServer s1 = ServerCreationTestUtils.createServerWithRuntime(IJBossToolingConstants.DEPLOY_ONLY_SERVER, "name1");
		UnitedServerListener l = new UnitedServerListener() {
			public boolean canHandleServer(IServer server) {
				return true;
			}
			public void serverChanged(IServer server) {
				changes[0] = true;
			}
			public void serverChanged(ServerEvent event) {
				changes[1] = true;
			}
		};
		UnitedServerListenerManager.getDefault().addListener(l);
		
		IServerWorkingCopy wc = s1.createWorkingCopy();
		wc.setAttribute("test1", "test2");
		s1 = wc.save(true, null);
		assertTrue(changes[0]);
		s1.publish(IServer.PUBLISH_FULL, null);
		JobUtils.waitForIdle(1000);
		assertTrue(changes[1]);
	}
	
	@After
	public void cleanup() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
}
