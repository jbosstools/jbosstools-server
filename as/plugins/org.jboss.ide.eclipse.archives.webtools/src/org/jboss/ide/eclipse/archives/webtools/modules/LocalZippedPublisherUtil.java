package org.jboss.ide.eclipse.archives.webtools.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;

import de.schlichtherle.io.ArchiveDetector;

public class LocalZippedPublisherUtil extends PublishUtil {
	
	private IServer server;
	private String deployRoot;
	private IModule[] module;
	private int publishType;
	private IModuleResourceDelta[] delta;
	
	public IStatus publishModule(IServer server, String deployRoot, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		this.server = server;
		this.deployRoot = deployRoot;
		this.module = module;
		this.publishType = publishType;
		this.delta = delta;
		
		IStatus[] returnStatus;
		
		
		// Am I a removal? If yes, remove me, and return
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH)
			returnStatus = removeModule(server, deployRoot, module);
		
		// Am I a full publish? If yes, full publish me, and return
		else if( publishType == IJBossServerPublisher.FULL_PUBLISH)
			returnStatus = fullPublish(server, deployRoot, module); 
		
		else {
			// Am I changed? If yes, handle my changes
			ArrayList<IStatus> results = new ArrayList<IStatus>(); 
			if( countChanges(delta) > 0) {
				results.addAll(Arrays.asList(publishChanges(server, deployRoot, module)));
			}
			
			IModule[] children = server.getChildModules(module, new NullProgressMonitor());
			results.addAll(Arrays.asList(handleChildrenDeltas(server, deployRoot, module, children)));
			
			// Handle the removed children that are not returned by getChildModules
			List<IModule[]> removed = getBehaviour(server).getRemovedModules();
			Iterator<IModule[]> i = removed.iterator();
			while(i.hasNext()) {
				IModule[] removedArray = i.next();
				if( removedArray[0].getId().equals(module[0].getId())) {
					results.addAll(Arrays.asList(removeModule(server, deployRoot, removedArray)));
				}
			}
			returnStatus = (IStatus[]) results.toArray(new IStatus[results.size()]);
		}
		TrueZipUtil.umount();
		
		IStatus finalStatus;
		if( returnStatus.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					"Publish Failed for module " + module[0].getName(), null); //$NON-NLS-1$
			for( int i = 0; i < returnStatus.length; i++ )
				ms.add(returnStatus[i]);
			finalStatus = ms;
		} else
			finalStatus = Status.OK_STATUS;

		return finalStatus;
	}
	

	protected IDeployableServer getDeployableServer(IServer server) {
		return (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
	}
	protected DeployableServerBehavior getBehaviour(IServer server) {
		return (DeployableServerBehavior)server.loadAdapter(DeployableServerBehavior.class, new NullProgressMonitor());
	}
	
	protected List<IModule[]> getRemovedModules(IServer s) {
		return getBehaviour(s).getRemovedModules();
	}
	
	protected IStatus[] handleChildrenDeltas(IServer server, String deployRoot, IModule[] module, IModule[] children) {
		// For each child:
		ArrayList<IStatus> results = new ArrayList<IStatus>(); 
		for( int i = 0; i < children.length; i++ ) {
			IModule[] combinedChild = combine(module, children[i]);
			// if it's new, full publish it
			if( !getBehaviour(server).hasBeenPublished(combinedChild)) {
				results.addAll(Arrays.asList(fullPublish(server, deployRoot, combinedChild)));
			}
			// else if it's removed, full remove it
			else if( isRemoved(server, combinedChild)) {
				results.addAll(Arrays.asList(removeModule(server, deployRoot, combinedChild)));
			}
			// else 
			else {
				// handle changed resources
				results.addAll(Arrays.asList(publishChanges(server, deployRoot, combinedChild)));

				// recurse
				IModule[] children2 = server.getChildModules(combinedChild, new NullProgressMonitor());
				results.addAll(Arrays.asList(handleChildrenDeltas(server, deployRoot, children, children2)));
			}
		}
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}
	
	protected boolean isRemoved(IServer server, IModule[] child) {
		List<IModule[]> removed = getBehaviour(server).getRemovedModules();
		Iterator<IModule[]> i = removed.iterator();
		while(i.hasNext()) {
			IModule[] next = i.next();
			if( next.length == child.length) {
				for( int j = 0; j < next.length; j++ ) {
					if( !next[j].getId().equals(child[j].getId())) {
						continue;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	protected IStatus[] removeModule(IServer server, String deployRoot, IModule[] module) {
		IPath deployPath = getOutputFilePath(module);
        final ArrayList<IStatus> status = new ArrayList<IStatus>();
		IFileUtilListener listener = new IFileUtilListener() {
			public void fileCopied(File source, File dest, boolean result,Exception e) {}
			public void fileDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL, 
							"Attempt to delete " + file.getAbsolutePath() + " failed",e)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			public void folderDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL,
							"Attempt to delete " + file.getAbsolutePath() + " failed",e)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} 
		};
		FileUtil.safeDelete(deployPath.toFile(), listener);
		return (IStatus[]) status.toArray(new IStatus[status.size()]);
	}
	
	protected IStatus[] fullPublish(IServer server, String deployRoot, IModule[] module) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		try {
			IPath path = getOutputFilePath(module);
			// Get rid of the old
			FileUtil.safeDelete(path.toFile(), null);
			
			TrueZipUtil.createArchive(path);
			de.schlichtherle.io.File root = TrueZipUtil.getFile(path, TrueZipUtil.getJarArchiveDetector());
			IModuleResource[] resources = getResources(module);
			results.addAll(Arrays.asList(copy(root, resources)));
			
			IModule[] children = server.getChildModules(module, new NullProgressMonitor());
			for( int i = 0; i < children.length; i++ ) 
				results.addAll(Arrays.asList(fullPublish(server, deployRoot, combine(module, children[i]))));
			TrueZipUtil.umount();
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		} catch( CoreException ce) {
			results.add(generateCoreExceptionStatus(ce));
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		}
	}
	
	protected IStatus[] publishChanges(IServer server, String deployRoot, IModule[] module) {
		IPath path = getOutputFilePath(module);
		de.schlichtherle.io.File root = TrueZipUtil.getFile(path, TrueZipUtil.getJarArchiveDetector());
		IModuleResourceDelta[] deltas = ((Server)server).getPublishedResourceDelta(module);
		return publishChanges(server, deltas, root);
	}
	
	protected IStatus[] publishChanges(IServer server, IModuleResourceDelta[] deltas, de.schlichtherle.io.File root) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		if( deltas == null || deltas.length == 0 )
			return new IStatus[]{};
		int dKind;
		IModuleResource resource;
		for( int i = 0; i < deltas.length; i++ ) {
			dKind = deltas[i].getKind();
			resource = deltas[i].getModuleResource();
			if( dKind == IModuleResourceDelta.ADDED ) {
				results.addAll(Arrays.asList(copy(root, new IModuleResource[]{resource})));
			} else if( dKind == IModuleResourceDelta.CHANGED ) {
				if( resource instanceof IModuleFile ) 
					results.addAll(Arrays.asList(copy(root, new IModuleResource[]{resource})));
				results.addAll(Arrays.asList(publishChanges(server, deltas[i].getAffectedChildren(), root)));
			} else if( dKind == IModuleResourceDelta.REMOVED) {
				de.schlichtherle.io.File f = getFileInArchive(root, 
						resource.getModuleRelativePath().append(
								resource.getName()));
				boolean b = f.deleteAll();
				if( !b )
					results.add(generateDeleteFailedStatus(f));
			} else if( dKind == IModuleResourceDelta.NO_CHANGE  ) {
				results.addAll(Arrays.asList(publishChanges(server, deltas[i].getAffectedChildren(), root)));
			}
		}
		
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}

	
	protected IStatus[] copy(de.schlichtherle.io.File root, IModuleResource[] children) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i] instanceof IModuleFile ) {
				IModuleFile mf = (IModuleFile)children[i];
				java.io.File source = getFile(mf);
				de.schlichtherle.io.File destination = getFileInArchive(root, mf.getModuleRelativePath().append(mf.getName()));
				boolean b = new de.schlichtherle.io.File(source).copyAllTo(destination);
				if( !b )
					results.add(generateCopyFailStatus(source, destination));
			} else if( children[i] instanceof IModuleFolder ) {
				de.schlichtherle.io.File destination = getFileInArchive(root, children[i].getModuleRelativePath().append(children[i].getName()));
				destination.mkdirs();
				IModuleFolder mf = (IModuleFolder)children[i];
				results.addAll(Arrays.asList(copy(root, mf.members())));
			}
		}
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}
	
	protected IStatus generateDeleteFailedStatus(java.io.File file) {
		return new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, "Could not delete file " + file); //$NON-NLS-1$
	}
	protected IStatus generateCoreExceptionStatus(CoreException ce) {
		return new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, ce.getMessage(), ce);
	}
	protected IStatus generateCopyFailStatus(java.io.File source, java.io.File destination) {
		return new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, "Copy of " + source + " to " + destination + " has failed");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
	
	protected de.schlichtherle.io.File getFileInArchive(de.schlichtherle.io.File root, IPath relative) {
		while(relative.segmentCount() > 0 ) {
			root = new de.schlichtherle.io.File(root, 
					relative.segment(0), ArchiveDetector.DEFAULT);
			relative = relative.removeFirstSegments(1);
		}
		return root;
	}

	private IModule[] combine(IModule[] module, IModule newMod) {
		IModule[] retval = new IModule[module.length + 1];
		for( int i = 0; i < module.length; i++ )
			retval[i]=module[i];
		retval[retval.length-1] = newMod;
		return retval;
	}
	
	public IPath getOutputFilePath(IModule[] module) {
		return getDeployPath(module, deployRoot);
	}
}
