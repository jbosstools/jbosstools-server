package org.jboss.tools.as.test.core.polling;

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;

public class DummyYesOrNoPoller implements IServerStatePoller2 {
	private IServerStatePollerType type;
	public IServerStatePollerType getPollerType() {
		return type;
	}
	public IStatus getCurrentStateSynchronous(IServer server) {
		// TODO Auto-generated method stub
		return null;
	}
	public void setPollerType(IServerStatePollerType type) {
		this.type = type;
	}
	public void beginPolling(IServer server, boolean expectedState)
			throws PollingException {
	}
	public boolean isComplete() throws PollingException, RequiresInfoException {
		return false;
	}
	public boolean getState() throws PollingException, RequiresInfoException {
		return false;
	}
	public void cleanup() {
	}
	public void cancel(int type) {
	}
	public int getTimeoutBehavior() {
		return 0;
	}
	public List<String> getRequiredProperties() {
		return null;
	}
	public void provideCredentials(Properties credentials) {
	}
	public IServer getServer() {
		return null;
	}
}
