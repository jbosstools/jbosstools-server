package org.jboss.ide.eclipse.as.wtp.core.modules;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.internal.deployables.J2EEDeployableFactory;
import org.eclipse.wst.common.componentcore.ArtifactEdit;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.web.internal.deployables.ComponentDeployable;
import org.jboss.ide.eclipse.as.wtp.core.vcf.JBTVirtualArchiveComponent;

public abstract class JBTProjectModuleDelegate extends ComponentDeployable implements IJBTModule {

	public JBTProjectModuleDelegate(IProject project) {
		this(project,ComponentCore.createComponent(project));
	}
	
	public JBTProjectModuleDelegate(IProject project, IVirtualComponent aComponent) {
		super(project, aComponent);
	}
	
	/*
	 * This method is meant to be overridden by subclasses.  Return whether or not to add this file
	 * to the members list. If it should be filtered out, or if it will be returned as a child 
	 * module instead, return false. Otherwise return true. 
	 * 
	 * @param file
	 * @return boolean should add file?
	 */
	protected boolean shouldAddComponentFile(IFile file) {
		return true;
	}

	/**
	 * If you will need to check any xml artifacts to verify whether a file should
	 * be added as a child module or a member, return that artifact here
	 */
	protected ArtifactEdit getComponentArtifactEditForRead() {
		return null;
	}
	
	/*
	 * Should we meld the jar / external jar / var reference in with the members() IModuleResource objects.
	 * If yes, the reference will appear like any other file.
	 * If no, you are expected to handle this file as a child module and expose it yourself
	 */
	protected boolean shouldIncludeUtilityComponent(IVirtualComponent virtualComp, IVirtualReference[] components, ArtifactEdit edit) {
		// superclass just checks to make sure it's a binary component
		return super.shouldIncludeUtilityComponent(virtualComp, components, edit);
	}

	/*
	 * If you have an IVirtualReference which should *not* be included as a utility component,
	 * you should return an IModule for that object here. Excerpt is from J2EEFlexProjDeployable  
	 */
    protected IModule gatherModuleReference(IVirtualComponent component, IVirtualComponent targetComponent ) {
    	IModule module = super.gatherModuleReference(component, targetComponent);
    	// Handle binary module components
    	if (targetComponent instanceof JBTVirtualArchiveComponent) {
    		module = ServerUtil.getModule(getFactoryId()+":"+targetComponent.getName()); //$NON-NLS-1$
    	}
		return module;
    }

    protected abstract String getFactoryId();
    
	public String getURI(IModule child) {
		if( component != null && child != null ) {
	    	IVirtualReference[] components = getReferences(component);
	    	for (int i = 0; i < components.length; i++) {
				IVirtualReference reference = components[i];
				if (reference != null && reference.getDependencyType()==IVirtualReference.DEPENDENCY_TYPE_USES) {
					IVirtualComponent virtualComp = reference.getReferencedComponent();
					IModule module = gatherModuleReference(component, virtualComp);
					if( child.equals(module)) {
						if( !virtualComp.isBinary()) {
							IPath path = reference.getRuntimePath();
							return path.append(reference.getArchiveName()).toString();
						} else if( virtualComp instanceof JBTVirtualArchiveComponent ){
							JBTVirtualArchiveComponent moduleVirtualArchiveComponent = (JBTVirtualArchiveComponent)virtualComp;
			    			IPath moduleDeployPath = moduleVirtualArchiveComponent.getDeploymentPath();
			    			String moduleName = new Path(moduleVirtualArchiveComponent.getName()).lastSegment();
			    			if (moduleName.equals(moduleDeployPath.lastSegment())){
			    				return moduleDeployPath.toString();
			    			}
			    			return moduleDeployPath.append(moduleName).toString();
						}
					}
				}
			}
		}
		return child.getName();
	}
	
}
