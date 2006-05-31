package org.jboss.ide.eclipse.as.core.client;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ClientDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.verifiers.IJbossDeploymentVerifier;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class VerifyPublishClient extends ClientDelegate {

	public VerifyPublishClient() {
		super();
	}

	public boolean supports(IServer server, Object launchable, String launchMode) {
		if( launchable instanceof IJbossDeploymentVerifier) {
			return ((IJbossDeploymentVerifier)launchable).supportsVerify(server, launchMode);
		}
		return false;
	}

	public IStatus launch(IServer server, Object launchable, 
			String launchMode, ILaunch launch) {
		
		if( launchable instanceof IJbossDeploymentVerifier) {
			IJbossDeploymentVerifier verifier = ((IJbossDeploymentVerifier)launchable);
			return verifier.verifyDeployed(JBossServerCore.getServer(server), launchMode, launch); 
		}
		
		
		return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, "Could not verify deployment", 
				new Exception("Could not verify deployment in " + getClass().getName()));
	}

}
