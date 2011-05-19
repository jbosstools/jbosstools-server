package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

public class JBoss7ManagerServicePoller implements IServerStatePoller {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.server.JBoss7ManagerServicePoller"; //$NON-NLS-1$
	private IServer server;
	private ServerStatePollerType type;
	private boolean expectedState;

	public void beginPolling(IServer server, boolean expectedState, PollThread pollTread) {
		this.server = server;
		this.expectedState = expectedState;
	}

	public ServerStatePollerType getPollerType() {
		return type;
	}

	public void setPollerType(ServerStatePollerType type) {
		this.type = type;
	}

	public IServer getServer() {
		return server;
	}

	public boolean isComplete() throws PollingException, RequiresInfoException {
		IJBoss7ManagerService service = null;
		try {
			service = JBoss7ManagerUtil.getService(server);
			if (expectedState == SERVER_DOWN) {
				return awaitShutdown(service);
			} else {
				return awaitRunning(service);
			}
		} catch (Exception e) {
			throw new PollingException(e.getMessage());
		} finally {
			disposeService(service);
		}
	}

	private Boolean awaitRunning(IJBoss7ManagerService service) {
		try {
			JBoss7ServerState serverState = null;
			do {
				serverState = service.getServerState(getServer().getHost());
				System.err.println("state = " + serverState); //$NON-NLS-1$
			} while (serverState == JBoss7ServerState.STARTING);
			return serverState == JBoss7ServerState.RUNNING;
		} catch (Exception e) {
			return false;
		}
	}

	private Boolean awaitShutdown(IJBoss7ManagerService service) {
		try {
			JBoss7ServerState serverState = null;
			do {
				serverState = service.getServerState(getServer().getHost());
				System.err.println("state = " + serverState); //$NON-NLS-1$
			} while (serverState == JBoss7ServerState.RUNNING);
			return false;
		} catch (JBoss7ManangerConnectException e) {
			System.err.println("connection closed!"); //$NON-NLS-1$
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void disposeService(IJBoss7ManagerService service) {
		if (service != null) {
			service.dispose();
		}
	}

	public boolean getState() throws PollingException, RequiresInfoException {
		IJBoss7ManagerService service = null;
		try {
			service = JBoss7ManagerUtil.getService(getServer());
			JBoss7ServerState serverState = service.getServerState(getServer().getHost());
			return serverState == JBoss7ServerState.RUNNING
					|| serverState == JBoss7ServerState.RESTART_REQUIRED;
		} catch (Exception e) {
			throw new PollingException(e.getMessage());
		} finally {
			disposeService(service);
		}
	}

	public void cleanup() {
	}

	public List<String> getRequiredProperties() {
		return new ArrayList<String>();
	}

	public void failureHandled(Properties properties) {
	}

	public void cancel(int type) {
	}

	public int getTimeoutBehavior() {
		return TIMEOUT_BEHAVIOR_FAIL;
	}
}
