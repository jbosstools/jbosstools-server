package org.jboss.ide.eclipse.as.jmx.integration;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunnable;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunner;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JBossServerJMXRunner implements IServerJMXRunner {

	public void run(IServer server, final IServerJMXRunnable runnable) throws CoreException {
		IJMXRunnable runnable2 = new IJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				runnable.run(connection);
			}
		};
		try {
			JBossServerConnectionProvider.run(server, runnable2);
		} catch(JMXException jmxe) {
			// TODO wrap and log
		}
	}

	public void beginTransaction(IServer server, Object lock) {
		JMXClassLoaderRepository.getDefault().addConcerned(server, lock);
	}

	public void endTransaction(IServer server, Object lock) {
		JMXClassLoaderRepository.getDefault().removeConcerned(server, lock);
	}
}
