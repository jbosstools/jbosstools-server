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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jst.common.project.facet.IJavaFacetInstallDataModelProperties;
import org.eclipse.jst.common.project.facet.JavaFacetInstallDataModelProvider;
import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

public class JBossSARFacetProjectCreationDataModelProvider extends
		FacetProjectCreationDataModelProvider {

	public void init() {
		super.init();

		FacetDataModelMap map = (FacetDataModelMap) getProperty(FACET_DM_MAP);
		IDataModel javaFacet = DataModelFactory.createDataModel(new JavaFacetInstallDataModelProvider());
		map.add(javaFacet);
		
		IDataModel sarFacet = DataModelFactory.createDataModel(new JBossSARFacetDataModelProvider());
		map.add(sarFacet);
		String sarSrc = sarFacet.getStringProperty(IJBossSARFacetDataModelProperties.SAR_SOURCE_FOLDER);
		javaFacet.setProperty(IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME, sarSrc);
	}

	@Override
	public IStatus validate(String propertyName) {
		return super.validate(propertyName);
	}
}
