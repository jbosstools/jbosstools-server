package org.jboss.ide.eclipse.archives.webtools.modules;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class WTPZippedPublisher extends PublishUtil implements IJBossServerPublisher {
	private int moduleState = IServer.PUBLISH_STATE_NONE;
	public boolean accepts(String method, IServer server, IModule[] module) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		IModule lastMod = (module == null || module.length == 0 ) ? null : module[module.length -1];
		if( LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(method) && lastMod == null)
			return true;
		return LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(method) 
			&& ModuleCoreNature.isFlexibleProject(lastMod.getProject())
			&& ds != null && ds.zipsWTPDeployments();
	}

	public int getPublishState() {
		return moduleState;
	}
	
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		if( module.length > 1 ) { 
			return Status.OK_STATUS;
		}

		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		String deployRoot = PublishUtil.getDeployRootFolder(
								module, ds, ds.getDeployFolder(), 
								IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
		
		LocalZippedPublisherUtil util = new LocalZippedPublisherUtil();
		return util.publishModule(server, deployRoot, module, publishType, delta, monitor);
	}
}
