package org.jboss.ide.eclipse.as.ssh.server;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory.SingleDeployableModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerBehaviourDelegate.SSHPublishMethod;

public class SSHSingleFilePublisher implements IJBossServerPublisher {

	public SSHSingleFilePublisher() {
	}

	public boolean accepts(String method, IServer server, IModule[] module) {
		if( !method.equals(SSHPublishMethod.SSH_PUBLISH_METHOD))
			return false;
		if( module == null )
			return true;
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		boolean shouldAccept = ds != null 
				&& module.length == 1 
				&& module[0].getModuleType().getId().equals(SingleDeployableFactory.MODULE_TYPE);
		return shouldAccept;
	}

	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}

	private IDeployableServer server;
	private SSHPublishMethod publishMethod;
	private int publishState = IServer.PUBLISH_STATE_NONE;
	public IStatus publishModule(IJBossServerPublishMethod method,
			IServer server, IModule[] module, int publishType,
			IModuleResourceDelta[] delta, IProgressMonitor monitor)
			throws CoreException {
		this.server = ServerConverter.getDeployableServer(server);
		this.publishMethod = (SSHPublishMethod)method;
		IModule module2 = module[0];
		
		IStatus status = null;
		if(publishType == REMOVE_PUBLISH){
        	status = unpublish(this.server, module2, monitor);
        } else if( publishType == FULL_PUBLISH || publishType == INCREMENTAL_PUBLISH){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	status = publish(this.server, module2, monitor);
        }
		return status;
	}

	protected IStatus publish(IDeployableServer server, IModule module, IProgressMonitor monitor) {
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			String destFolder = SSHPublisher.getRemoteDeployFolder(server.getServer());
			IPath destFile = new Path(destFolder).append(sourcePath.lastSegment());
			try {
				SSHPublisher.mkdirAndCopy(publishMethod.getSession(), sourcePath.toOSString(), destFile.toString());
			} catch( CoreException ce ) {
				return ce.getStatus();
			}
		} else {
			// error can't do nuffin
			publishState = IServer.PUBLISH_STATE_UNKNOWN;
		}
		return Status.OK_STATUS;
	}
	
	protected IStatus unpublish(IDeployableServer server, IModule module, IProgressMonitor monitor) throws CoreException {
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			String destFolder = SSHPublisher.getRemoteDeployFolder(server.getServer());
			IPath destFile = new Path(destFolder).append(sourcePath.lastSegment());
			SSHZippedJSTPublisher.launchCommand(publishMethod.getSession(), "rm -rf " + destFile.toString());
		} else {
			// deleted module. o noes. Ignore it. 
			publishState = IServer.PUBLISH_STATE_UNKNOWN;
			Status status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.SINGLE_FILE_UNPUBLISH_MNF, 
					NLS.bind(Messages.DeleteModuleFail, module.getName()), null);
			return status;
		}
		Status status = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.SINGLE_FILE_UNPUBLISH_SUCCESS,
				NLS.bind(Messages.ModuleDeleted, module.getName()), null);
		return status;
	}

	
}
