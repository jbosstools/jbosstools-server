/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.modules;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.patterns.ModuleDirectoryScannerPathFilter;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

public class ResourceModuleResourceUtil extends ModuleResourceUtil {
	/**
	 * @since 2.3
	 */
	public static final String COMPONENT_INCLUSIONS_PATTERN = "component.inclusion.patterns"; //$NON-NLS-1$
	/**
	 * @since 2.3
	 */
	public static final String COMPONENT_EXCLUSIONS_PATTERN = "component.exclusion.patterns"; //$NON-NLS-1$
	
	private static final String ALL_RESOURCES_PATTERN = "**"; //$NON-NLS-1$

	/**
	 * Utility method for just quickly discovering the filtered member list
	 * @since 2.3
	 */
	public static IModuleResource[] getFilteredMembers(IModule module, String inc, String exc) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, null);
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(md.members(), inc, exc);
		return filter.getFilteredMembers();
	}
	
	/**
	 * Get a proper includes / excludes filter for this project if it exists
	 * or null
	 * @since 2.3
	 */
	public static IModulePathFilter findDefaultModuleFilter(IModule module) {
		if( ServerModelUtilities.isBinaryModule(module) )
			return null;
		String[] incExc = getProjectIncludesExcludes(module);
		String inclusionPatterns = incExc[0];
		String exclusionPatterns = incExc[1];
		if (exclusionPatterns == null 
		    && (inclusionPatterns == null || ALL_RESOURCES_PATTERN.equals(inclusionPatterns))) {
		  //No filtering necessary, everything is included. That way we avoid unnecessary scans
		  return null;
		}
		try {
			ModuleDirectoryScannerPathFilter filter = 
					new ModuleDirectoryScannerPathFilter(getMembers(module), 
					    inclusionPatterns, exclusionPatterns);
			return filter;
		} catch( CoreException ce ) {
			JBossServerCorePlugin.getDefault().getLog().log(ce.getStatus());
		}
		return null;
	}
	
	/**
	 * Does this project have the proper settings that call for 
	 * include and exclude patterns in the virtual component metadata?
	 * 
	 * Return the includes / excludes pattern if yes.
	 * If no, return a two-length array of null objects
	 * 
	 * @param module
	 * @return
	 */
	private static String[] getProjectIncludesExcludes(IModule module) {
		IProject p = module.getProject();
		if( p != null ) {
			IVirtualComponent vc = ComponentCore.createComponent(p);
			if( vc != null ) {
				Properties props = vc.getMetaProperties();
				String exclusionPatterns = getPatternValue(props, COMPONENT_EXCLUSIONS_PATTERN);
				String inclusionPatterns = getPatternValue(props, COMPONENT_INCLUSIONS_PATTERN);
				return new String[]{  inclusionPatterns, exclusionPatterns }; 
			}
		}
		return new String[]{null, null};
	}

	/**
	 * Returns the trimmed property value if it exists and is not empty, or null otherwise
	 */
  private static String getPatternValue(Properties props,  String propertyName) {
    String value = props.getProperty(propertyName);
    if (value != null) {
      value = value.trim();
      if (value.length() == 0) {
        value = null;
      }
    }
    return value;
  }
}
