package org.jboss.ide.eclipse.as.wtp.core.vcf;

import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.jboss.ide.eclipse.as.wtp.core.util.LimitedContainerVirtualFolder;

public class OutputFoldersVirtualComponent implements IVirtualComponent {

	private IProject project;
	private IVirtualComponent referencingComp;
	public OutputFoldersVirtualComponent(IProject p, IVirtualComponent referencingComponent) {
		this.project = p;
		this.referencingComp = referencingComponent;
	}
	
	public void create(int updateFlags, IProgressMonitor aMonitor)
			throws CoreException {
		// Ignore
	}

	public boolean exists() {
		return true;
	}

	public IVirtualComponent getComponent() {
		return this;
	}

	public String getName() {
		return getId();
	}

	public String getDeployedName() {
		return getName();
	}
	
	protected String getId() {
		return OutputFolderReferenceResolver.OUTPUT_FOLDER_SEGMENT + Path.SEPARATOR + project.getName();
	}

	public IProject getProject() {
		return project;
	}
	public IVirtualComponent[] getReferencingComponents() {
		return referencingComp == null ? new IVirtualComponent[]{} : new IVirtualComponent[]{referencingComp};
	}

	public IVirtualFolder getRootFolder() {
		IContainer[] containers = J2EEProjectUtilities.getOutputContainers(project);
		return new LimitedContainerVirtualFolder(project, new Path("/"), containers);
	}

	public Properties getMetaProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath[] getMetaResources() {
		// TODO Auto-generated method stub
		return null;
	}

	public IVirtualReference getReference(String aComponentName) {
		// Ignore
		return null;
	}

	public IVirtualReference[] getReferences() {
		// Ignore; no children
		return new IVirtualReference[]{};
	}

	public boolean isBinary() {
		return false;
	}

	public void setMetaProperties(Properties properties) {
		// Ignore
	}

	public void setMetaProperty(String name, String value) {
		// Ignore
	}

	public void setMetaResources(IPath[] theMetaResourcePaths) {
		// Ignore
	}

	public void setReferences(IVirtualReference[] theReferences) {
		// Ignore
	}

	public Object getAdapter(Class adapter) {
		// Ignore
		return null;
	}

	public void addReferences(IVirtualReference[] references) {
		// Ignore
	}

}
