package org.jboss.ide.eclipse.as.ssh.server;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.archives.webtools.modules.PackageModuleFactory;
import org.jboss.ide.eclipse.archives.webtools.modules.PackageModuleFactory.PackagedModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ssh.server.SSHPublisher.SSHCopyCallback;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerBehaviourDelegate.SSHPublishMethod;

public class SSHPackagesPublisher implements IJBossServerPublisher {

	protected IDeployableServer server;
	protected IModuleResourceDelta[] delta;
	protected ArrayList<IStatus> statuses = new ArrayList<IStatus>();
	protected SSHPublishMethod method;
	public SSHPackagesPublisher() {
	}

	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}

	public boolean accepts(String method, IServer server, IModule[] module) {
		if( SSHPublishMethod.SSH_PUBLISH_METHOD.equals(method) && module != null && module.length > 0
				&& PackageModuleFactory.MODULE_TYPE.equals(module[0].getModuleType().getId()))
			return true;
		return false;
	}
	public IStatus publishModule(
			IJBossServerPublishMethod method, 
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor)
			throws CoreException {
		this.method = (SSHPublishMethod)method;
		this.server = ServerConverter.getDeployableServer(server);
		this.delta = delta;
		IModule module2 = module[0];

		try {
	    	// if it's being removed
	    	if( publishType == REMOVE_PUBLISH ) {
	    		statuses.addAll(Arrays.asList(removeModule(module2, monitor)));
	    	} else if( publishType == FULL_PUBLISH ) {
	    		statuses.addAll(Arrays.asList(publishModule(module2, false, monitor)));
	    	} else if( publishType == INCREMENTAL_PUBLISH ) {
	    		statuses.addAll(Arrays.asList(publishModule(module2, true, monitor)));
	    	}
		}catch(Exception e) {
			IStatus status = new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, 
					NLS.bind(Messages.ErrorDuringPublish, module2.getName()), e);
			return status;
		}
		
		if( statuses.size() > 0 ) {
			MultiStatus ms = new MultiStatus(IntegrationPlugin.PLUGIN_ID, IStatus.ERROR, 
					NLS.bind(Messages.ErrorDuringPublish, module2.getName()), null);
			for( int i = 0; i < statuses.size(); i++ ) {
				ms.add(statuses.get(i));
			}
			return ms;
		}
		
		IStatus ret = new Status(IStatus.OK, IntegrationPlugin.PLUGIN_ID, 
				NLS.bind(Messages.PublishSuccessful, module2.getName()));
		return ret;
	}

	protected IStatus[] removeModule(IModule module, IProgressMonitor monitor) {
		IArchive pack = getPackage(module);
		// remove all of the deployed items
		if( pack != null ) {
			IPath sourcePath = pack.getArchiveFilePath();
			String deployFolder = getRemoteDeployFolder(server.getServer());
			String deployFile = new Path(deployFolder).append(sourcePath.lastSegment()).toString();
			try {
				SSHZippedJSTPublisher.launchRemoveCommand(method.getSession(), deployFile, monitor);
			} catch( CoreException ce ) {
				return new IStatus[] { ce.getStatus() };
			}
		}
		return new IStatus[] { }; // nothing to report
	}



	protected IStatus[] publishModule(IModule module, boolean incremental, IProgressMonitor monitor) {
		IArchive pack = getPackage(module);
		IPath sourcePath = pack.getArchiveFilePath();
		String remoteContainer = getRemoteDeployFolder(server.getServer());
		IPath remoteRoot = new Path(remoteContainer).append(sourcePath.lastSegment());

		try {
			if( incremental ) {
				IModuleResource[] members = PublishUtil.getResources(module);
				SSHCopyCallback callback = new SSHCopyCallback(remoteRoot, method);
				PublishCopyUtil util = new PublishCopyUtil(callback);
				return util.publishDelta(delta, monitor);
			} else {
				if( !pack.isExploded() ) {
					// copy the output file
					SSHZippedJSTPublisher.launchCopyCommand(method.getSession(), sourcePath.toOSString(), remoteRoot.toString(), monitor);
				} else {
					IModuleResource[] members = PublishUtil.getResources(module);
					SSHCopyCallback callback = new SSHCopyCallback(remoteRoot, method);
					PublishCopyUtil util = new PublishCopyUtil(callback);
					return util.publishFull(members, monitor);
				}
			} 
		} catch( CoreException ce ) {
			return new IStatus[] { ce.getStatus() };
		}
		return new IStatus[] { };
	}

	protected IArchive getPackage(IModule module) {
		PackagedModuleDelegate delegate = (PackagedModuleDelegate)module.loadAdapter(PackagedModuleDelegate.class, new NullProgressMonitor());
		return delegate == null ? null : delegate.getPackage();
	}
	protected String getRemoteDeployFolder(IServer server) {
		return ((Server)server).getAttribute(ISSHDeploymentConstants.DEPLOY_DIRECTORY, (String)null);
	}
}