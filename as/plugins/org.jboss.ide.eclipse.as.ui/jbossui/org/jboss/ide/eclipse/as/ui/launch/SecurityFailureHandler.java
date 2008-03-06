package org.jboss.ide.eclipse.as.ui.launch;

import java.util.List;
import java.util.Properties;

import org.jboss.ide.eclipse.as.core.extensions.polling.JMXPoller;
import org.jboss.ide.eclipse.as.core.server.IPollerFailureHandler;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;

/**
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class SecurityFailureHandler implements IPollerFailureHandler {

	public boolean accepts(IServerStatePoller poller, String action,
			List requiredProperties) {
		if( poller.getPollerType().getId().equals("org.jboss.ide.eclipse.as.core.runtime.server.JMXPoller"))
			return true;
		return false;
	}

	public void handle(IServerStatePoller poller, String action, List requiredProperties) {
//		Properties p = new Properties();
//		p.put(JMXPoller.REQUIRED_USER, "admin" );
//		p.put(JMXPoller.REQUIRED_PASS, "admin");
//		poller.failureHandled(p);
//		System.out.println("handled");
		poller.failureHandled(null);
	}
}
