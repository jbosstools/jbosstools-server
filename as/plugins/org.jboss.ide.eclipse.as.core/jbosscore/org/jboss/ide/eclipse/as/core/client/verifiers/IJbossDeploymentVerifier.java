package org.jboss.ide.eclipse.as.core.client.verifiers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public interface IJbossDeploymentVerifier {
		public boolean supportsVerify(IServer server, String launchMode);
		public IStatus verifyDeployed(JBossServer server, String launchmode, ILaunch launch);
}
