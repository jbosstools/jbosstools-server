/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.mbeans.project;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jst.j2ee.project.facet.J2EEModuleFacetInstallDataModelProvider;

public class JBossSARFacetDataModelProvider extends J2EEModuleFacetInstallDataModelProvider 
	implements IJBossSARFacetDataModelProperties {

	private static final String JBOSS_SAR_PROJECT_FACET = IJBossSARFacetDataModelProperties.SAR_PROJECT_FACET;

	public Set getPropertyNames() {
		Set names = super.getPropertyNames();
		names.add(IJBossSARFacetDataModelProperties.SAR_SOURCE_FOLDER);
		names.add(IJBossSARFacetDataModelProperties.SAR_CONTENT_FOLDER);
		return names;
	}

	public Object getDefaultProperty(String propertyName) {
		if (propertyName.equals(FACET_ID)) {
			return JBOSS_SAR_PROJECT_FACET;
		}
		else if(IJBossSARFacetDataModelProperties.SAR_CONTENT_FOLDER.equals(propertyName)){
			return IJBossSARFacetDataModelProperties.DEFAULT_SAR_CONFIG_RESOURCE_FOLDER;
		}
		else if(IJBossSARFacetDataModelProperties.SAR_SOURCE_FOLDER.equals(propertyName)){
			return IJBossSARFacetDataModelProperties.DEFAULT_SAR_SOURCE_FOLDER;
		}
		return super.getDefaultProperty(propertyName);
	}

	// Superclass will take over this method
//	protected int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version) {
//		return J2EEVersionConstants.J2EE_1_4_ID;
//	}
	public IStatus validate(String propertyName) {
		return OK_STATUS;
	}
}
