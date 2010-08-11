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
package org.jboss.ide.eclipse.as.wtp.core.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsDataModelProvider;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IGroup;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public class VCFUtil {
	public static void addReference(IVirtualComponent component, 
			IVirtualComponent rootComponent, 
			String path, String archiveName)
			throws CoreException {
		IDataModelProvider provider = new CreateReferenceComponentsDataModelProvider();
		IDataModel dm = DataModelFactory.createDataModel(provider);
		
		dm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, rootComponent);
		dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, Arrays.asList(component));
		
		//[Bug 238264] the uri map needs to be manually set correctly
		Map<IVirtualComponent, String> uriMap = new HashMap<IVirtualComponent, String>();
		uriMap.put(component, archiveName);
		dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP, uriMap);
        dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, path);

		IStatus stat = dm.validateProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
		Throwable t = stat == null ? null : stat.getException();
		if (stat == null || stat.isOK()) {
			try {
				dm.getDefaultOperation().execute(new NullProgressMonitor(), null);
				return;
			} catch (ExecutionException e) {
				t = e;
			}	
		}
		if( t != null ) {
			IStatus status = new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, t.getMessage(), t);
			throw new CoreException(status);
		}
	}
	
	/**
	 * Finds the one facet which is in the "modules" group
	 * @param project
	 * @return
	 */
	public static IProjectFacet getModuleFacet(IProject proj) {
		if( ModuleCoreNature.isFlexibleProject(proj)) {
			try {
				IGroup group = ProjectFacetsManager.getGroup("modules");
				if( group != null ) {
					Set<IProjectFacetVersion> set = group.getMembers();
					Iterator<IProjectFacetVersion> i = set.iterator();
					while(i.hasNext()) {
						IProjectFacet facet = i.next().getProjectFacet();
						if( FacetedProjectFramework.hasProjectFacet(proj, facet.getId())) {
							return facet;
						}
					}
				}
			} catch( CoreException ce ) {
			}
		}
		return null;
	}
	
	// TODO can increase efficiency by using a pre-loaded map
	public static String getModuleFacetExtension(IProject project) {
		IProjectFacet facet = getModuleFacet(project);
		if( facet != null ) {
			if( facet.getId().equals(IModuleConstants.JST_WEB_MODULE)) return ".war";
			if( facet.getId().equals(IModuleConstants.JST_EJB_MODULE)) return ".jar";
			if( facet.getId().equals(IModuleConstants.JST_UTILITY_MODULE)) return ".jar";
			if( facet.getId().equals(IModuleConstants.JST_EAR_MODULE)) return ".ear";
			if( facet.getId().equals(IModuleConstants.WST_WEB_MODULE)) return ".war";
			if( facet.getId().equals(IModuleConstants.JST_APPCLIENT_MODULE)) return ".jar";
			if( facet.getId().equals(IModuleConstants.JST_CONNECTOR_MODULE)) return ".rar";
			if( facet.getId().equals("jst.jboss.esb")) return ".esb";
			if( facet.getId().equals("jbt.bpel.facet.core")) return ".bpel";
			// TODO add our extensions
		}
		return null;
	}
	
	
}
