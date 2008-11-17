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
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory.SingleDeployableModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;

public class SingleFilePublisher implements IJBossServerPublisher {

	private IDeployableServer server;
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
			int publishType, IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException {

		this.server = ServerConverter.getDeployableServer(server);

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
		return status;

	}
	
	protected IStatus publish(IDeployableServer server, IModule module, boolean updateTimestamp, IProgressMonitor monitor) throws CoreException {
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployFolder());
			IPath tempDestFolder = new Path(server.getTempDeployFolder());
			File tempDestFile = tempDestFolder.append(sourcePath.lastSegment()).toFile();
			File destFile = destFolder.append(sourcePath.lastSegment()).toFile();
			FileUtilListener l = new FileUtilListener();
			FileUtil.fileSafeCopy(sourcePath.toFile(), tempDestFile, l);
			boolean success = tempDestFile.renameTo(destFile);
			if( updateTimestamp )
				destFile.setLastModified(new Date().getTime());
			if( l.errorFound || !success ) {
				publishState = IServer.PUBLISH_STATE_FULL;
				Exception e = l.e != null ? l.e : new Exception("Move from " + tempDestFile + " to " + destFile + " failed.");
				return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The module cannot be published.", e);
			}
		} else {
			// deleted module. o noes. Ignore it. We can't re-publish it, so just ignore it.
			publishState = IServer.PUBLISH_STATE_UNKNOWN;
			Status status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, "The module cannot be published because it cannot be located. (" + module.getName() + ")");
			return status;
		}
		
		Status status = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, "Module " + module.getName() + " coppied.");
		return status;
	}

	protected IStatus unpublish(IDeployableServer server, IModule module, IProgressMonitor monitor) throws CoreException {
		// delete file
		SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module.loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
		if( delegate != null ) {
			IPath sourcePath = delegate.getGlobalSourcePath();
			IPath destFolder = new Path(server.getDeployFolder());
			FileUtilListener l = new FileUtilListener();
			File destFile = destFolder.append(sourcePath.lastSegment()).toFile();
			FileUtil.safeDelete(destFile, l);
			if( l.errorFound ) {
				publishState = IServer.PUBLISH_STATE_FULL;
				return new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, "Unable to delete module " + module.getName() + " from server.", l.e);
			}
		}
		Status status = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, "Module " + module.getName() + " removed.");
		return status;
	}

	public static class FileUtilListener implements IFileUtilListener {
		protected boolean errorFound = false;
		protected Exception e;
		public void fileCoppied(File source, File dest, boolean result,Exception e) {
			if( result == false || e != null ) {
				errorFound = true;
				this.e = e;
			}
		}
		public void fileDeleted(File file, boolean result, Exception e) {
			if( result == false || e != null ) {
				errorFound = true;
				this.e = e;
			}
		}
		public void folderDeleted(File file, boolean result, Exception e) {
			if( result == false || e != null ) {
				errorFound = true;
				this.e = e;
			}
		} 
	}
}
