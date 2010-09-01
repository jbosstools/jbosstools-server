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
package org.jboss.ide.eclipse.as.wtp.core.vcf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.jee.application.ICommonModule;
import org.eclipse.wst.common.componentcore.internal.ComponentcoreFactory;
import org.eclipse.wst.common.componentcore.internal.ComponentcorePackage;
import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
import org.eclipse.wst.common.componentcore.internal.StructureEdit;
import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
import org.eclipse.wst.common.componentcore.internal.builder.DependencyGraphManager;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
import org.eclipse.wst.common.componentcore.internal.util.IComponentImplFactory;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;

public class JBTVirtualComponent 
	extends VirtualComponent implements IJBTComponent, IComponentImplFactory {

	private IVirtualReference[] cachedReferences;
	private long depGraphModStamp;

	public JBTVirtualComponent() {
		super();
	}
	
	public JBTVirtualComponent(IProject aProject, IPath aRuntimePath) {
		super(aProject, aRuntimePath);
	}

	/*
	 * These methods allow this component to make new elements
	 */
	public IVirtualComponent createComponent(IProject aProject) {
		return new JBTVirtualComponent(aProject, new Path("/")); //$NON-NLS-1$
	}

	public IVirtualComponent createArchiveComponent(IProject aProject, String archiveLocation, IPath aRuntimePath) {
		return new JBTVirtualArchiveComponent(aProject, archiveLocation, aRuntimePath);
	}
	
	public IVirtualFolder createFolder(IProject aProject, IPath aRuntimePath) {
		return new JBTVirtualFolder(aProject, aRuntimePath, this);
	}
	

	/*
	 * The following group of methods was 
	 * stolen / adapted from EarVirtualComponent. 
	 */
	
	public IVirtualReference[] getReferences() {
//		IVirtualReference[] cached = getCachedReferences();
//		if (cached != null)
//			return cached;
		cachedReferences = getHardReferences(this);
		return cachedReferences;
	}
	
	// Returns cache if still valid or null
	public IVirtualReference[] getCachedReferences() {
		if (cachedReferences != null && checkIfStillValid())
			return cachedReferences;
		else
			depGraphModStamp = DependencyGraphManager.getInstance().getModStamp();
		return null;
	}

	private boolean checkIfStillValid() {
		return DependencyGraphManager.getInstance().checkIfStillValid(depGraphModStamp);
	}
	
	private static IVirtualReference[] getHardReferences(IVirtualComponent component) {
		StructureEdit core = null;
		List hardReferences = new ArrayList();
		try {
			core = StructureEdit.getStructureEditForRead(component.getProject());
			if (core != null && core.getComponent() != null) {
				WorkbenchComponent component2 = core.getComponent();
				if (component != null) {
					List referencedComponents = component2.getReferencedComponents();
					for (Iterator iter = referencedComponents.iterator(); iter.hasNext();) {
						ReferencedComponent referencedComponent = (ReferencedComponent) iter.next();
						if (referencedComponent == null)
							continue;
						IVirtualReference vReference = StructureEdit.createVirtualReference(component, referencedComponent);
						if (vReference != null) {
							IVirtualComponent referencedIVirtualComponent = vReference.getReferencedComponent();
							if (referencedIVirtualComponent != null && referencedIVirtualComponent.exists()) {
								if( vReference.getDependencyType() == IVirtualReference.DEPENDENCY_TYPE_USES ) {
									String archiveName = getArchiveName(referencedIVirtualComponent, referencedComponent);
									vReference.setArchiveName(archiveName);
								}
								hardReferences.add(vReference);
							}
						}
					}
				}
			}
		} finally {
			if (core != null)
				core.dispose();
		}
		return (IVirtualReference[]) hardReferences.toArray(new IVirtualReference[hardReferences.size()]);
	}
	
	protected static String getArchiveName(IVirtualComponent referencedIVirtualComponent, ReferencedComponent referencedComponent) {
		String archiveName = null;
		if (referencedComponent.getDependentObject() != null) {
			/*
			 * The getDependentObject part of this code needs explanation.
			 * The IVirtualComponent allows you to set some "dependent object" 
			 * if you wish. WTP projects often do in the case that there's a 
			 * deployment descriptor of some sort. In those cases, WTP's 
			 * "dependent object" is of type ICommonModule and is an EObject.
			 * This allows virtual component to override the default archive 
			 * name with one from a deployment descriptor. 
			 */
			archiveName = ((ICommonModule) referencedComponent.getDependentObject()).getUri();
		} else {
			if( referencedIVirtualComponent instanceof IJBTComponent ) {
				archiveName = getJBTComponentArchiveName(referencedIVirtualComponent, referencedComponent);
			} else {
				archiveName = legacy_getWTPComponentArchiveName(referencedIVirtualComponent, referencedComponent);
			}
			
		}
		return archiveName;
	}
	
	protected static String getJBTComponentArchiveName(IVirtualComponent moduleComp, ReferencedComponent ref) {
		return ref.getArchiveName();
	}

	/*
	 * Legacy WTP implementation
	 */
	
	protected static String legacy_getWTPComponentArchiveName(IVirtualComponent moduleComp, ReferencedComponent ref) {
		String archiveName = null;
		if (moduleComp.isBinary()) {
			String uri = legacy_getJarURI(ref, moduleComp);
		} else if(ref.getArchiveName() != null){
			archiveName = ref.getArchiveName();
		} else {
			IProject referencedProject = moduleComp.getProject();
			if (JavaEEProjectUtilities.isDynamicWebProject(referencedProject) || J2EEProjectUtilities.isStaticWebProject(referencedProject)) {
				archiveName = moduleComp.getName() + IJ2EEModuleConstants.WAR_EXT;
			} else if (JavaEEProjectUtilities.isJCAProject(referencedProject)) {
				archiveName = moduleComp.getName() + IJ2EEModuleConstants.RAR_EXT;
			} else if (JavaEEProjectUtilities.isUtilityProject(referencedProject)) {
				archiveName = legacy_getJarURI(ref, moduleComp);
			} else {
				archiveName = moduleComp.getName() + IJ2EEModuleConstants.JAR_EXT;
			}
		}
		return archiveName;
	}
	
	protected static String legacy_getJarURI(ReferencedComponent ref, IVirtualComponent moduleComp) {
		String uri = null;
		if (uri == null || uri.length() < 0) {
			if(moduleComp.isBinary()){
				uri = new Path(moduleComp.getName()).lastSegment();
			} else {
				uri = moduleComp.getName() + IJ2EEModuleConstants.JAR_EXT;
			}
		} else {
			String prefix = ref.getRuntimePath().makeRelative().toString();
			if (prefix.length() > 0) {
				uri = prefix + "/" + uri; //$NON-NLS-1$
			}
		}
		return uri;
	}
}
