package org.jboss.ide.eclipse.as.core.runtime.server.polling;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;

/**
 * Essentially does nothing because the process already has a listener
 * on it that sets the server state to stopped once the process dies. 
 * 
 * It's here to make the shutdown include no polling though, thus
 * more efficient.
 * @author rob
 *
 */
public class ProcessTerminatedPoller implements IServerStatePoller {

	private JBossServerBehavior server;
	public void beginPolling(IServer server, boolean expectedState,
			PollThread pt) {
		this.server = (JBossServerBehavior)server.loadAdapter(JBossServerBehavior.class, new NullProgressMonitor());
	}

	public void cancel(int type) {
	}

	public void cleanup() {
	}

	public boolean getState() throws PollingException {
		return !isComplete();
	}

	public boolean isComplete() throws PollingException {
		return server.getProcess() == null || server.getProcess().isTerminated();
	}

}
