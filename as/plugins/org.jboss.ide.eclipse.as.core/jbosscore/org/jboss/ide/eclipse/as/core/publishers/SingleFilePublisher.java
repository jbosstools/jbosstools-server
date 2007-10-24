package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory.SingleDeployableModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.CoppiedEvent;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.DeletedEvent;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;

public class SingleFilePublisher implements IJBossServerPublisher {

	private IDeployableServer server;
	private EventLogTreeItem root;
	private int publishState = IServer.PUBLISH_STATE_NONE;
	public SingleFilePublisher(IServer server, EventLogTreeItem root) {
		this.server = ServerConverter.getDeployableServer(server);
		this.root = root;
	}
	
	public int getPublishState() {
		return publishState;
	}

	public IStatus publishModule(int kind, int deltaKind,
			int modulePublishState, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		
		IModule module2 = module[0];
		
		IStatus status = null;
		if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	status = unpublish(server, module2, kind, deltaKind, modulePublishState, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind || kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	status = publish(server, module2, kind, deltaKind, modulePublishState, true, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind && kind == IServer.PUBLISH_INCREMENTAL ){
        	status = publish(server, module2, kind, deltaKind, modulePublishState, false, monitor);
        }
		return status;

	}
	
	protected IStatus publish(IDeployableServer server, IModule module, int kind, int deltaKind, 
								int modulePublishState, boolean updateTimestamp, IProgressMonitor monitor) throws CoreException {
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployDirectory());
			File destFile = destFolder.append(sourcePath.lastSegment()).toFile();
			FileUtilListener l = new FileUtilListener(root);
			FileUtil.fileSafeCopy(sourcePath.toFile(), destFile, l);
			if( updateTimestamp )
				destFile.setLastModified(new Date().getTime());
			if( l.errorFound ) {
				publishState = IServer.PUBLISH_STATE_FULL;				
			}
		} else {
			// deleted module. o noes. Ignore it. We can't re-publish it, so just ignore it.
			publishState = IServer.PUBLISH_STATE_UNKNOWN;
			Status status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, "The module cannot be published because it cannot be located. (" + module.getName() + ")");			
			throw new CoreException(status);			
		}
		
		return null;
	}

	protected IStatus unpublish(IDeployableServer server, IModule module, int kind, int deltaKind, int modulePublishState, IProgressMonitor monitor) throws CoreException {
		// delete file
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployDirectory());
			FileUtilListener l = new FileUtilListener(root);
			FileUtil.safeDelete(destFolder.append(sourcePath.lastSegment()).toFile(), l);
			if( l.errorFound ) {
				publishState = IServer.PUBLISH_STATE_FULL;
				throw new CoreException(new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, "Unable to delete module from server.", new Exception("Some files were not removed from the server")));
			}
		}
		
		return null;
	}
	public void setDelta(IModuleResourceDelta[] delta) {
		// ignore delta
	}

	public static class FileUtilListener implements IFileUtilListener {
		protected EventLogTreeItem root;
		protected boolean errorFound = false;
		public FileUtilListener(EventLogTreeItem root) { 
			this.root = root;
		}
		public void fileCoppied(File source, File dest, boolean result,Exception e) {
			if( result == false || e != null ) {
				errorFound = true;
				new CoppiedEvent(root, source, dest, result, e);
				EventLogModel.markChanged(root);
			}
		}
		public void fileDeleted(File file, boolean result, Exception e) {
			if( result == false || e != null ) {
				errorFound = true;
				new DeletedEvent(root, file, result, e);
				EventLogModel.markChanged(root);
			}
		}
		public void folderDeleted(File file, boolean result, Exception e) {
			if( result == false || e != null ) {
				errorFound = true;
				new DeletedEvent(root, file, result, e);
				EventLogModel.markChanged(root);
			}
		} 
	};

}
