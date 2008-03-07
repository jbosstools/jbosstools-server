package org.jboss.ide.eclipse.as.ui.launch;

import java.util.List;
import java.util.Properties;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jboss.ide.eclipse.as.core.extensions.polling.JMXPoller;
import org.jboss.ide.eclipse.as.core.server.IPollerFailureHandler;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.ui.dialogs.RequiredCredentialsDialog;

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

	public void handle(final IServerStatePoller poller, String action, List requiredProperties) {
		Display.getDefault().asyncExec(new Runnable() { 
			public void run() {
				RequiredCredentialsDialog d = new RequiredCredentialsDialog(new Shell());
				if( d.open() == Window.OK) {
					Properties p = new Properties();
					p.put(JMXPoller.REQUIRED_USER, d.getUser());
					p.put(JMXPoller.REQUIRED_PASS, d.getPass());
					poller.failureHandled(p);
				} else {
					poller.failureHandled(null);
				}
			}
		});
	}
}
