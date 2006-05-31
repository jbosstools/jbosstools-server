package org.jboss.ide.eclipse.as.core.client.verifiers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.client.ICopyAsLaunch;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ASDebug;


/**
 * A deployable object
 * @author rstryker
 *
 */
public class AopDeploymentVerifier implements ICopyAsLaunch, IJbossDeploymentVerifier{

	private JBossModuleDelegate delegate;
	public AopDeploymentVerifier(JBossModuleDelegate delegate) {
		this.delegate = delegate;
	}
	
	
	public boolean supportsDeploy(IServer server, String launchMode) {
		if( JBossServerCore.getServer(server) == null ) {
			return false;
		}
		return true;
	}

	public boolean supportsVerify(IServer server, String launchMode) {
		if( JBossServerCore.getServer(server) == null ) {
			return false;
		}
		return true;
	}

	public IStatus verifyDeployed(JBossServer server, String launchMode, ILaunch launch) {
		// TODO Auto-generated method stub

		return null;
	}
	
}
