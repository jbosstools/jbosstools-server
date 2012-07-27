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
package org.jboss.tools.as.test.core.parametized.server.publishing;

import java.util.Collection;

import org.eclipse.wst.common.componentcore.internal.operation.AddReferenceDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class DeploymentAssemblyArchivePathVarWarTest extends DeploymentAssemblyArchivePathVarTest {
	private static String PROJECT_PREFIX = "pWar";
	private static int PROJECT_ID = 1;
	@Parameters
	public static Collection<Object[]> params() {
		return componentJarData();
	}

	public DeploymentAssemblyArchivePathVarWarTest(String serverType, String zip,
			String deployLoc, String perMod, String refName, String refFolder) {
		super(serverType, zip, deployLoc, perMod, refName, refFolder);
	}

	protected void setProjectName() {
		PROJECT_ID++;
		MY_PROJECT_NAME = PROJECT_PREFIX + PROJECT_ID;
	}
	protected IDataModel getSingleProjectCreationDataModel() {
		return CreateProjectOperationsUtility.getWebDataModel(MY_PROJECT_NAME, 
				null, null, null, null, JavaEEFacetConstants.WEB_24, false);
	}
	protected IDataModelProvider getDefaultAddReferenceProvider() {
		return new AddReferenceDataModelProvider();
	}

	protected int getExpectedFileCount() {
		// manifest.mf, web.xml, and the junit jar
		return 3;
	}

}
