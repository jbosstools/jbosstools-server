package org.jboss.ide.eclipse.archives.webtools.modules;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.modules.OSGiModuleFactory;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class OSGiPublisher extends AltMethodZippedJSTPublisher {
	public boolean accepts(String method, IServer server, IModule[] module) {
		if( module[module.length-1].getModuleType().getId().equals(OSGiModuleFactory.MODULE_TYPE))
			return true;
		return false;
	}

	@Override
	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}
	
	protected IStatus handleLocalZipAndRemotePublish(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		
		IDeployableServer server2 = ServerConverter.getDeployableServer(server);
		IPath destination = server2.getDeploymentLocation(module, true);
		String remoteTempDeployRoot = getDeployRoot(module, ServerConverter.getDeployableServer(server));
		if( new Path(remoteTempDeployRoot).toFile().exists())
			new Path(remoteTempDeployRoot).toFile().mkdirs();
		
		IPath presumedSourcePath = PublishUtil.getModuleNestedDeployPath(module, remoteTempDeployRoot, server2);
		String name = presumedSourcePath.lastSegment();
		IPath realSourcePathFolder = presumedSourcePath.removeLastSegments(1).append("plugins"); //$NON-NLS-1$
		IProject project = module[module.length-1].getProject();
		// Run the export job
		IStatus result = scheduleExportJob(project, remoteTempDeployRoot, name, monitor);
		if( result.isOK() ) {
			String[] plugins = realSourcePathFolder.toFile().list();
			if( plugins.length > 0 ) {
				IPath src = realSourcePathFolder.append(plugins[0]);
				result = remoteFullPublish(src, destination.removeLastSegments(1), name, 
						AbstractServerToolsPublisher.getSubMon(monitor, 150));
				// Then cleanup this plugins folder >=[
				src.toFile().delete();
			}
		}
		return result;
	}

	protected IStatus scheduleExportJob(IProject project, String destFolder, String name, IProgressMonitor monitor) {
		final FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = true;
		info.useJarFormat = true;
		info.exportSource = false;
		info.exportSourceBundle = true;
		info.allowBinaryCycles = true;
		info.useWorkspaceCompiledClasses = false;
		info.destinationDirectory = destFolder;
		info.zipFileName = name;
		info.items = new Object[]{PluginRegistry.findModel(project)};
		info.signingInfo = null; //fPage.useJARFormat() ? fPage.getSigningInfo() : null;
		info.qualifier = null; //fPage.getQualifier();

		final CustomPluginExportOperation job = new CustomPluginExportOperation(info, "Exporting Plugin: " + project.getName()); //$NON-NLS-1$
		
		IStatus s = job.run2(monitor);
		return s;
	}
	
	private class CustomPluginExportOperation extends PluginExportOperation {
		public CustomPluginExportOperation(FeatureExportInfo info, String name) {
			super(info, name);
		}
		
		public IStatus run2(IProgressMonitor monitor) {
			return super.run(monitor);
		}
	}

	
}
