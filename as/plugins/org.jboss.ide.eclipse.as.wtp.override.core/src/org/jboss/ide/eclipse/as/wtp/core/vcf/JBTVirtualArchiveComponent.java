package org.jboss.ide.eclipse.as.wtp.core.vcf;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;

public class JBTVirtualArchiveComponent 
	extends VirtualArchiveComponent implements IJBTComponent {

	private IPath deploymentPath;
	public JBTVirtualArchiveComponent(IProject aComponentProject,
			String archiveLocation, IPath aRuntimePath) {
		super(aComponentProject, archiveLocation, aRuntimePath);
	}
	
	public IPath getDeploymentPath() {
		return deploymentPath;
	}
	public void setDeploymentPath(IPath path) {
		this.deploymentPath = path;
	}

}
