package org.jboss.tools.as.test.core.polling;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.extensions.polling.ProcessTerminatedPoller;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.IPollResultListener;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller.PollingException;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller.RequiresInfoException;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagerServicePoller;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.Test;

public class PollThreadTest extends TestCase {

	@After
	public void cleanup() throws Exception {
		ASMatrixTests.cleanup();
	}
	
	@Test
	public void testPollersFound() {
		assertNotNull(PollThreadUtils.getPoller(WebPortPoller.WEB_POLLER_ID));
		assertNotNull(PollThreadUtils.getPoller(ProcessTerminatedPoller.POLLER_ID));
		assertNotNull(PollThreadUtils.getPoller("org.jboss.ide.eclipse.as.core.runtime.server.JMXPoller"));
		assertNotNull(PollThreadUtils.getPoller(JBoss7ManagerServicePoller.POLLER_ID));
	}
	
	private IServer createServer(String addendum) throws CoreException {
		return ServerCreationTestUtils.createServerWithRuntime(IJBossToolingConstants.SERVER_AS_32, 
				IJBossToolingConstants.SERVER_AS_32 + addendum);
	}
		
	@Test
	public void testPollerStringAttributes() throws CoreException {
		IServer s = createServer("a");
		IServerWorkingCopy wc = s.createWorkingCopy();
		wc.setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, "startupPoller");
		wc.setAttribute(IJBossToolingConstants.SHUTDOWN_POLLER_KEY, "shutdownPoller");
		s = wc.save(true, null);
		JobUtils.waitForIdle();
		String startupPoller = PollThreadUtils.getPollerId(IServerStatePoller.SERVER_UP, s);
		String shutdownPoller = PollThreadUtils.getPollerId(IServerStatePoller.SERVER_DOWN, s);
		assertEquals("startupPoller", startupPoller);
		assertEquals("shutdownPoller", shutdownPoller);
	}

	@Test
	public void testPollerResolution() throws CoreException {
		IServer s = createServer("b");
		IServerWorkingCopy wc = s.createWorkingCopy();
		wc.setAttribute(IJBossToolingConstants.STARTUP_POLLER_KEY, TestInternalPoller.POLLER_ID);
		wc.setAttribute(IJBossToolingConstants.SHUTDOWN_POLLER_KEY, TestInternalPoller.POLLER_ID);
		s = wc.save(true, null);
		JobUtils.waitForIdle();
		String startupPoller = PollThreadUtils.getPollerId(IServerStatePoller.SERVER_UP, s);
		String shutdownPoller = PollThreadUtils.getPollerId(IServerStatePoller.SERVER_DOWN, s);
		assertEquals(TestInternalPoller.POLLER_ID, startupPoller);
		assertEquals(TestInternalPoller.POLLER_ID, shutdownPoller);
		IServerStatePoller testPoller = PollThreadUtils.getPoller(TestInternalPoller.POLLER_ID);
		assertNotNull(testPoller);
		assertEquals(testPoller.getClass(), TestInternalPoller.class);
	}

	private static class ServerProvider implements IServerProvider {
		private IServer server;
		public ServerProvider(IServer server) {
			this.server = server;
		}
		public IServer getServer() {
			return server;
		}
	}
	
	private List<String> asList(String[] a) {
		return Arrays.asList(a);
	}
	
	@Test
	public void testInternalRequiresMoreInfoHandlerResolved() throws Exception {
		IServer s = createServer("c");
		IServerProvider pro = new ServerProvider(s);
		String[] atts = new String[]{"a","b"};
		IProvideCredentials provider = ExtensionManager.getDefault().getFirstCredentialProvider(pro,asList(atts));
		assertNotNull(provider);
		assertFalse(provider instanceof PollerFailureHandler);
		atts = new String[]{"a","b","c"};
		provider = ExtensionManager.getDefault().getFirstCredentialProvider(pro,asList(atts));
		assertNotNull(provider);
		assertTrue(provider instanceof PollerFailureHandler);
		atts = new String[]{"a","b","c","d"};
		provider = ExtensionManager.getDefault().getFirstCredentialProvider(pro,asList(atts));
		assertNull(provider);
	}

	@Test
	public void testSynchronousResults() throws Exception {
		IServer s = createServer("d");
		IServerStatePoller currentlyUp = new AbstractTestInternalPoller() {
			public IStatus getCurrentStateSynchronous(IServer server) {
				return Status.OK_STATUS;
			}
		};
		IStatus retStat = PollThreadUtils.isServerStarted(s, currentlyUp);
		assertTrue(retStat.isOK());

		IServerStatePoller currentlyDown = new AbstractTestInternalPoller() {
			public IStatus getCurrentStateSynchronous(IServer server) {
				return new Status(IStatus.ERROR, ASMatrixTests.PLUGIN_ID, "failure");
			}
		};
		retStat = PollThreadUtils.isServerStarted(s, currentlyDown);
		assertFalse(retStat.isOK());
	}

	@Test
	public void testCancelReceivedDuringStartupPoll() throws Exception {
		IServer s = createServer("d");
		((Server)s).setServerState(IServer.STATE_STARTING);
		final int[] canceledHow = new int[]{-1};
		final boolean[] cleanedUp = new boolean[]{false};
		IServerStatePoller doNothingForever = new AbstractTestInternalPoller() {
			public void cancel(int type) {
				canceledHow[0] = type;
			}
			public void cleanup() {
				cleanedUp[0] = true;
			}
		};
		PollerListener listener = new PollerListener();
		PollThread pt = new PollThread(IServerStatePoller.SERVER_UP, doNothingForever, listener, s);
		pt.start();
		try {
			Thread.sleep(500);
		} catch(InterruptedException ie) {}
		assertFalse(listener.anythingAsserted);
		assertNull(listener.assertedWhat);
		PollThreadUtils.cancelPolling("Some Message", pt);
		assertEquals(canceledHow[0], IServerStatePoller.CANCEL);
		try {
			Thread.sleep(200);
		} catch(InterruptedException ie){}
		assertFalse(listener.anythingAsserted);
		assertEquals(null, listener.assertedWhat);
		assertTrue(cleanedUp[0]);
	}

	@Test
	public void testCancelReceivedDuringShutdownPoll() throws Exception {
		IServer s = createServer("e");
		((Server)s).setServerState(IServer.STATE_STOPPING);
		final int[] canceledHow = new int[]{-1};
		final boolean[] cleanedUp = new boolean[]{false};
		IServerStatePoller doNothingForever = new AbstractTestInternalPoller() {
			public boolean getState() throws PollingException, RequiresInfoException {
				return IServerStatePoller.SERVER_UP;
			}
			public void cancel(int type) {
				canceledHow[0] = type;
			}
			public void cleanup() {
				cleanedUp[0] = true;
			}
		};
		PollerListener listener = new PollerListener();
		PollThread pt = new PollThread(IServerStatePoller.SERVER_DOWN, doNothingForever, listener, s);
		pt.start();
		try {
			Thread.sleep(500);
		} catch(InterruptedException ie) {}
		assertFalse(listener.anythingAsserted);
		assertNull(listener.assertedWhat);
		PollThreadUtils.cancelPolling("Some Message", pt);
		assertEquals(canceledHow[0], IServerStatePoller.CANCEL);
		try {
			Thread.sleep(200);
		} catch(InterruptedException ie){}
		assertFalse(listener.anythingAsserted);
		assertEquals(null, listener.assertedWhat);
		assertTrue(cleanedUp[0]);
	}

	private static class PollerListener implements IPollResultListener {

		private boolean anythingAsserted = false;
		private boolean[] assertedWhat = null;
		public void stateAsserted(boolean expectedState, boolean currentState) {
			anythingAsserted = true;
			assertedWhat = new boolean[]{expectedState, currentState};
		}

		@Override
		public void stateNotAsserted(boolean expectedState, boolean currentState) {
			anythingAsserted = true;
			assertedWhat = new boolean[]{expectedState, currentState};
		}
	}

	@Test
	public void testPollerFailureCancelsThread() throws Exception {
		IServer s = createServer("g");
		((Server)s).setServerState(IServer.STATE_STARTING);
		final int[] canceledHow = new int[]{-1};
		final boolean[] cleanedUp = new boolean[]{false};
		IServerStatePoller failPoller = new AbstractTestInternalPoller() {
			public boolean isComplete() throws PollingException, RequiresInfoException {
				throw new PollingException("Automatic failure");
			}
			public void cancel(int type) {
				canceledHow[0] = type;
			}
			public void cleanup() {
				cleanedUp[0] = true;
			}
		};
		PollerListener listener = new PollerListener();
		PollThread pt = new PollThread(IServerStatePoller.SERVER_UP, failPoller, listener, s);
		pt.start();
		try {
			Thread.sleep(500);
		} catch(InterruptedException ie) {}
		assertTrue(listener.anythingAsserted);
		assertTrue(listener.assertedWhat[0]);
		assertFalse(listener.assertedWhat[1]);
		assertTrue(cleanedUp[0]);
		assertEquals(canceledHow[0], IServerStatePoller.CANCEL);
		assertFalse(pt.isAlive());
	}
	
}
