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
	public SingleFilePublisher() {
	}
	
	public int getPublishState() {
		return publishState;
	}
	
	public boolean accepts(IServer server, IModule[] module) {
		if( module != null && module.length > 0 
				&& module[module.length-1] != null  
				&& module[module.length-1].getModuleType().getId().equals("jboss.singlefile"))
			return true;
		return false;
	}

	public IStatus publishModule(IServer server, IModule[] module, 
			int publishType, IModuleResourceDelta[] delta, 
			EventLogTreeItem log, IProgressMonitor monitor) throws CoreException {

		this.server = ServerConverter.getDeployableServer(server);
		this.root = log;

		IModule module2 = module[0];
		
		IStatus status = null;
		if(publishType == REMOVE_PUBLISH){
        	status = unpublish(this.server, module2, monitor);
        } else if( publishType == FULL_PUBLISH ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	status = publish(this.server, module2, true, monitor);
        } else if( publishType == INCREMENTAL_PUBLISH ) {
        	status = publish(this.server, module2, false, monitor);
        }
		root.setProperty(PublisherEventLogger.CHANGED_RESOURCE_COUNT, new Integer(1));
		return status;

	}
	
	protected IStatus publish(IDeployableServer server, IModule module, boolean updateTimestamp, IProgressMonitor monitor) throws CoreException {
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployFolder());
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

	protected IStatus unpublish(IDeployableServer server, IModule module, IProgressMonitor monitor) throws CoreException {
		// delete file
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployFolder());
			FileUtilListener l = new FileUtilListener(root);
			FileUtil.safeDelete(destFolder.append(sourcePath.lastSegment()).toFile(), l);
			if( l.errorFound ) {
				publishState = IServer.PUBLISH_STATE_FULL;
				throw new CoreException(new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, "Unable to delete module from server.", new Exception("Some files were not removed from the server")));
			}
		}
		
		return null;
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
	}
}
