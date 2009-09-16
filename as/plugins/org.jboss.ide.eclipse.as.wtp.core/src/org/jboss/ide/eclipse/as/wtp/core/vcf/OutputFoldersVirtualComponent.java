package org.jboss.ide.eclipse.as.wtp.core.vcf;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
		if( project.equals(referencingComp.getProject()))
			return OutputFolderReferenceResolver.OUTPUT_FOLDER_PROTOCOL;
		return OutputFolderReferenceResolver.OUTPUT_FOLDER_PROTOCOL + Path.SEPARATOR + project.getName();
	}

	public IProject getProject() {
		return project;
	}
	public IVirtualComponent[] getReferencingComponents() {
		return referencingComp == null ? new IVirtualComponent[]{} : new IVirtualComponent[]{referencingComp};
	}

	public IVirtualFolder getRootFolder() {
		IContainer[] containers = getOutputContainers(project);
		return new LimitedContainerVirtualFolder(project, new Path("/"), containers);
	}
	
	private static IContainer[] getOutputContainers(IProject project) {
		List<IContainer> result = new ArrayList<IContainer>();
		try {
			if (!project.hasNature(JavaCore.NATURE_ID))
				return new IContainer[] {};
		} catch (Exception e) {
		}
		IPackageFragmentRoot[] sourceContainers = getSourceContainers(project);
		for (int i = 0; i < sourceContainers.length; i++) {
			IContainer outputFolder = J2EEProjectUtilities.getOutputContainer(project, sourceContainers[i]);
			if (outputFolder != null && !result.contains(outputFolder))
				result.add(outputFolder);
		}
		return result.toArray(new IContainer[result.size()]);
	}

	public static IPackageFragmentRoot[] getSourceContainers(IProject project) {
		IJavaProject jProject = JavaCore.create(project);
		if (jProject == null)
			return new IPackageFragmentRoot[0];
		List<IPackageFragmentRoot> list = new ArrayList<IPackageFragmentRoot>();
		IPackageFragmentRoot[] roots;
		try {
			roots = jProject.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind() != IPackageFragmentRoot.K_SOURCE)
					continue;
				list.add(roots[i]);
			}
		} catch( JavaModelException jme ) {
		}
		return list.toArray(new IPackageFragmentRoot[list.size()]);
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
