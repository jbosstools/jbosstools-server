package org.jboss.tools.as.test.core.internal.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.application.internal.operations.AddReferenceToEnterpriseApplicationDataModelProvider;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.datamodel.properties.IAddReferenceDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.DependencyType;
import org.eclipse.wst.common.componentcore.internal.operation.AddReferenceDataModelProvider;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualReference;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.jboss.ide.eclipse.archives.webtools.filesets.vcf.WorkspaceFilesetVirtualComponent;

/* Add .component file references */
public class ComponentReferenceUtils {
	
	public static void addPathArchiveComponent(IVirtualComponent component, 
			IPath variablePath, String folder, String name, boolean isEar)
			throws CoreException {
		IDataModelProvider p = isEar ? new AddReferenceToEnterpriseApplicationDataModelProvider() : new AddReferenceDataModelProvider();
		IVirtualReference ref = createPathArchiveReference(component, variablePath, folder, name);
		addReferenceToComponent(component, ref, p);
	}

	public static IVirtualReference createPathArchiveReference (IVirtualComponent component, 
			IPath variablePath, String folder, String name) throws CoreException {
		IPath resolvedPath = JavaCore.getResolvedVariablePath(variablePath);
		java.io.File file = new java.io.File(resolvedPath.toOSString());
		if (file.isFile() && file.exists()) {
			String type = VirtualArchiveComponent.VARARCHIVETYPE
					+ IPath.SEPARATOR;
			IVirtualComponent archive = ComponentCore.createArchiveComponent(
					component.getProject(), type + variablePath.toString());
			VirtualReference ref = new VirtualReference(component, archive);
			ref.setArchiveName(name);
			ref.setRuntimePath(new Path(folder));
			return ref;
		}
		return null;
	}

	
	public static void addReferenceToComponent(IVirtualComponent component, 
			IVirtualReference ref, IDataModelProvider provider)  throws CoreException {
		if( ref == null )
			return;
		IDataModel dm = DataModelFactory.createDataModel(provider);
		dm.setProperty(IAddReferenceDataModelProperties.SOURCE_COMPONENT, component);
		dm.setProperty(IAddReferenceDataModelProperties.TARGET_REFERENCE_LIST, Arrays.asList(ref));
		IStatus stat = dm.validate();
		if (!stat.isOK())
			throw new CoreException(stat);
		try {
			dm.getDefaultOperation().execute(new NullProgressMonitor(), null);
		} catch (ExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, "test", 
					e.getMessage()));
		}
	}

	public static VirtualReference createExternalJarReference(IVirtualComponent rootComponent, IPath path, String runtimeLoc, String name) {
		return createJarReference(rootComponent, path, runtimeLoc, name, false);
	}

	public static VirtualReference createWorkspaceJarReference(IVirtualComponent rootComponent, IPath path, String runtimeLoc, String name) {
		return createJarReference(rootComponent, path, runtimeLoc, name, false);
	}

	public static VirtualReference createJarReference(IVirtualComponent rootComponent, IPath path, 
			String runtimeLoc, String name, boolean makeRelative ) {
		String type = VirtualArchiveComponent.LIBARCHIVETYPE + IPath.SEPARATOR;
		String path2 = (makeRelative ? path.makeRelative() : path).toString();
		IVirtualComponent archive = ComponentCore.createArchiveComponent(
				rootComponent.getProject(), type + path2);
		VirtualReference ref = new VirtualReference(rootComponent, archive);
		ref.setArchiveName(name);
		if (runtimeLoc != null) {
			ref.setRuntimePath(new Path(runtimeLoc).makeAbsolute());
		}
		return ref;
	}

	public static VirtualReference createFilesetComponentReference(IVirtualComponent root, String workspaceRelativeFolder, String includes, String excludes, String runtimePath) {
		
		WorkspaceFilesetVirtualComponent vc = new WorkspaceFilesetVirtualComponent(
				root.getProject(), root, new Path(workspaceRelativeFolder).makeAbsolute().toString()); 
		vc.setIncludes(includes);
		vc.setExcludes(excludes);
		VirtualReference ref = new VirtualReference(root, vc);
		ref.setDependencyType(DependencyType.CONSUMES);
		ref.setRuntimePath(new Path(runtimePath)); //$NON-NLS-1$
		return ref;
	}
	
}
