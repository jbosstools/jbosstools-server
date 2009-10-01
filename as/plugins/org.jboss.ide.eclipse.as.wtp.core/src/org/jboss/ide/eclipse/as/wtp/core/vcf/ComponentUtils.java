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

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class ComponentUtils {
	private static HashMap<String,String> facetToExtension = null;
	static {
		facetToExtension = new HashMap<String, String>();
		facetToExtension.put(IModuleConstants.JST_WEB_MODULE, ".war");
		facetToExtension.put(IModuleConstants.JST_EJB_MODULE, ".jar");
		facetToExtension.put(IModuleConstants.WST_WEB_MODULE, ".war");
		facetToExtension.put(IModuleConstants.JST_APPCLIENT_MODULE, ".jar");
		facetToExtension.put(IModuleConstants.JST_CONNECTOR_MODULE, ".rar");
		facetToExtension.put(IModuleConstants.JST_EAR_MODULE, ".ear");
	}
	
	public static void addMapping(String facet, String extension) {
		facetToExtension.put(facet, extension);
	}
	
	public static String getDefaultProjectExtension(IVirtualComponent component) {
		if( !component.isBinary()) {
			IFacetedProject fp = getFacetedProject(component.getProject());
			if( fp != null ) {
				Iterator i = facetToExtension.keySet().iterator();
				String facet = null;
				while(i.hasNext()) {
					facet = (String)i.next();
					if( isProjectOfType(fp, facet))
						return facetToExtension.get(facet);
				}
			}
		}
		return ".jar";
	}
	
	private static IFacetedProject getFacetedProject(IProject project) {
		IFacetedProject facetedProject = null;
		if (null != project && project.isAccessible()) {
			try {
				facetedProject = ProjectFacetsManager.create(project);
			} catch (CoreException e) {
			}
		}
		return facetedProject;
	}
	
	private static boolean isProjectOfType(IFacetedProject facetedProject, String typeID) {
		if (facetedProject != null && ProjectFacetsManager.isProjectFacetDefined(typeID)) {
			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(typeID);
			return projectFacet != null && facetedProject.hasProjectFacet(projectFacet);
		}
		return false;
	}
}
