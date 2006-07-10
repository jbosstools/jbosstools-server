package org.jboss.ide.eclipse.as.core.client.verifiers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class ArchiveVerifier implements IJbossDeploymentVerifier {
	
	
	public ArchiveVerifier(JBossModuleDelegate delegate) {
	}

	public boolean supportsVerify(IServer server, String launchMode) {
		return true;
	}

	public IStatus verifyDeployed(JBossServer server, String launchmode,
			ILaunch launch) {
		
		// Delegate to some other verifier.
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 0, "Well Done", null);
	}

}
