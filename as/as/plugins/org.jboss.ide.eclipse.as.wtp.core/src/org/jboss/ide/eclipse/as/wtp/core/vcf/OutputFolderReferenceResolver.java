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

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.ComponentcorePackage;
import org.eclipse.wst.common.componentcore.internal.DependencyType;
import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
import org.eclipse.wst.common.componentcore.internal.impl.PlatformURLModuleConnection;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;

public class OutputFolderReferenceResolver implements IReferenceResolver {
	public static final String OUTPUT_FOLDER_SEGMENT = "org.jboss.ide.eclipse.as.wtp.core.vcf.outputFolders";
	public static final String OUTPUT_FOLDER_PROTOCOL = PlatformURLModuleConnection.MODULE_PROTOCOL
								+IPath.SEPARATOR+ OUTPUT_FOLDER_SEGMENT + IPath.SEPARATOR;
	public OutputFolderReferenceResolver() {
	}

	public boolean canResolve(IVirtualComponent context,
			ReferencedComponent referencedComponent) {
		URI handle = referencedComponent.getHandle();
		String s = handle.toString();
		if(s.startsWith(OUTPUT_FOLDER_PROTOCOL))
			return true;
		return false;
	}

	public boolean canResolve(IVirtualReference reference) {
		if( reference.getReferencedComponent() instanceof OutputFoldersVirtualComponent )
			return true;
		return false;
	}

	public IVirtualReference resolve(IVirtualComponent context,
			ReferencedComponent referencedComponent) {
		String project = referencedComponent.getHandle().segment(1);
		IProject p = null;
		if( !project.equals("")) 
			p = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		else
			p = context.getProject();
		IVirtualComponent comp = new OutputFoldersVirtualComponent(p, context);
		IVirtualReference ref = ComponentCore.createReference(context, comp);
		ref.setArchiveName(referencedComponent.getArchiveName());
		ref.setRuntimePath(referencedComponent.getRuntimePath());
		ref.setDependencyType(referencedComponent.getDependencyType().getValue());
		return ref;
	}

	public ReferencedComponent resolve(IVirtualReference reference) {
		if( reference.getReferencedComponent() instanceof OutputFoldersVirtualComponent ) {
			OutputFoldersVirtualComponent comp = (OutputFoldersVirtualComponent)reference.getReferencedComponent();
			IProject p = reference.getReferencedComponent().getProject();
			ReferencedComponent rc = ComponentcorePackage.eINSTANCE.getComponentcoreFactory().createReferencedComponent();
			rc.setArchiveName(reference.getArchiveName());
			rc.setRuntimePath(reference.getRuntimePath());
			rc.setHandle(URI.createURI(comp.getId()));
			rc.setDependencyType(DependencyType.CONSUMES_LITERAL);
			return rc;
		}
		return null;
	}

}
