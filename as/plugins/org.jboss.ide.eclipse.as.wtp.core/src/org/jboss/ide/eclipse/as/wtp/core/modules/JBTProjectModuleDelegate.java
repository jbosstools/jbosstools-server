/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.modules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.ArtifactEdit;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.IModuleResource;
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
	 * Let's make this clean and organized
	 * @see org.eclipse.wst.web.internal.deployables.ComponentDeployable#members()
	 */
	public IModuleResource[] members() throws CoreException {
		members.clear();
		IVirtualComponent vc = ComponentCore.createComponent(getProject());
		if (vc != null) {
			addFromRootVirtualFolder(vc);
			addConsumableReferences(vc);
			addUtilMembers(vc);
		}
		
		IModuleResource[] mr = new IModuleResource[members.size()];
		members.toArray(mr);
		return mr;
	}
	
	protected void addFromRootVirtualFolder(IVirtualComponent vc) throws CoreException {
		IVirtualFolder vFolder = vc.getRootFolder();
		IModuleResource[] mr = getMembers(vFolder, Path.EMPTY);
		int size = mr.length;
		for (int j = 0; j < size; j++) {
			members.add(mr[j]);
		}
	}
	
	
	/*
	 * This will recursively search for consumed components, and children
	 * of consumed components, and will shove them into the members area. =D 
	 */
	protected void addConsumableReferences(IVirtualComponent vc) throws CoreException {
		List consumableMembers = new ArrayList();
		IVirtualReference[] refComponents = vc.getReferences();
    	for (int i = 0; i < refComponents.length; i++) {
    		IVirtualReference reference = refComponents[i];
    		if (reference != null && reference.getDependencyType()==IVirtualReference.DEPENDENCY_TYPE_CONSUMES) {
    			IVirtualComponent consumedComponent = reference.getReferencedComponent();
    			if (consumedComponent!=null) {
    				if (consumedComponent.getRootFolder()!=null) {
    					IVirtualFolder vFolder = consumedComponent.getRootFolder();
    					IModuleResource[] mr = getMembers(vFolder, reference.getRuntimePath().makeRelative());
    					int size = mr.length;
    					for (int j = 0; j < size; j++) {
    						if (!members.contains(mr[j]))
    							members.add(mr[j]);
    					}
    					addUtilMembers(consumedComponent);
    					addConsumableReferences(consumedComponent);
    				}
    			}
    		}
    	}
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
	 * Should we meld the jar / external jar / var / reference in with the members() IModuleResource objects.
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

    /**
     * This should return the module factory which we are associated with.
     * @return
     */
    protected abstract String getFactoryId();
    
    /*
     * Get the URI for this child module relative to the parent module
     * (non-Javadoc)
     * @see org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule#getURI(org.eclipse.wst.server.core.IModule)
     */
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
