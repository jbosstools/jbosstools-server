package org.jboss.ide.eclipse.as.ui.mbeans.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.internal.JavaServerPlugin;
import org.eclipse.jst.server.core.internal.RuntimeClasspathContainer;
import org.eclipse.jst.server.core.internal.RuntimeClasspathProviderWrapper;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.IRuntimeChangedEvent;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.wtp.core.vcf.VCFClasspathCommand;

public class SarProjectRuntimeChangedDelegate implements IDelegate {

	@Override
	public void execute(IProject project, IProjectFacetVersion fv,
			Object config, IProgressMonitor monitor) throws CoreException {
    	IRuntimeChangedEvent event = (IRuntimeChangedEvent)config;
    	IRuntime oldRT = event.getOldRuntime();
    	IRuntime newRT = event.getNewRuntime();
    	
    	if( oldRT != null )
    		VCFClasspathCommand.removeContainerClasspathEntry(project, getContainerPath(oldRT));
    	if( newRT != null )
    		VCFClasspathCommand.addContainerClasspathEntry(project, getContainerPath(newRT));
    	
	}
	
	public static IPath getContainerPath(IRuntime runtime) {
		org.eclipse.wst.server.core.IRuntime serverRuntime = ServerCore.findRuntime(runtime.getName());
		RuntimeClasspathProviderWrapper rcpw = JavaServerPlugin.findRuntimeClasspathProvider(serverRuntime.getRuntimeType());
		IPath serverContainerPath = new Path(RuntimeClasspathContainer.SERVER_CONTAINER)
			.append(rcpw.getId()).append(serverRuntime.getId());
		return serverContainerPath;
	}
}
