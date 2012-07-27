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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.common.componentcore.internal.operation.AddReferenceDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class DeploymentAssemblyArchivePathVarNestedWarTest extends DeploymentAssemblyArchivePathVarTest {
	private static String PROJECT_PREFIX = "qEar";
	private static String PROJECT_NESTED_PREFIX = "qWar";
	private static int PROJECT_ID = 1;
	@Parameters
	public static Collection<Object[]> params() {
		return componentJarData();
	}
	public DeploymentAssemblyArchivePathVarNestedWarTest(String serverType, String zip,
			String deployLoc, String perMod, String refName, String refFolder) {
		super(serverType, zip, deployLoc, perMod, refName, refFolder);
	}

	protected String MY_NESTED_PROJECT_NAME;
	protected void createProjects() throws Exception {
		setProjectName();
		IProject p = projectCreation();
		addReferences(ResourceUtils.findProject(MY_NESTED_PROJECT_NAME));
		addModuleToServer(p);
	}

	protected void setProjectName() {
		PROJECT_ID++;
		MY_PROJECT_NAME = PROJECT_PREFIX + PROJECT_ID;
		MY_NESTED_PROJECT_NAME = PROJECT_NESTED_PREFIX + PROJECT_ID;
	}
	
	/* Return the project which will have the component added */
	protected IProject projectCreation() throws Exception {
		IDataModel dm = CreateProjectOperationsUtility.getEARDataModel(MY_PROJECT_NAME, "earContent", null, null, JavaEEFacetConstants.EAR_5, false);
		IDataModel dm2 = CreateProjectOperationsUtility.getWebDataModel(MY_NESTED_PROJECT_NAME, MY_PROJECT_NAME, null, "webContent", null, JavaEEFacetConstants.WEB_25, true);
		IProject ep = createSingleProject(dm, MY_PROJECT_NAME);
		/* IProject nested = */ createSingleProject(dm2, MY_NESTED_PROJECT_NAME);
		return ep;
	}
	
	protected void verifyFileFoundInModule(String folder, String name, int expectedFileCount) throws Exception {
		IModule secondModule = ServerUtil.getModule(ResourceUtils.findProject(MY_NESTED_PROJECT_NAME));
		verifyFileFoundInModule(folder, name, expectedFileCount, secondModule);
	}

	@Test
	public void testVariableReference() throws Exception {
		verifyFileFoundInModule(jarFolder, jarName, getExpectedFileCount());
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		IPath earPath = getLocalPublishMethodDeployRoot();
		ArrayList<IPath> toFind = new ArrayList<IPath>();
		toFind.add(earPath.append(MY_NESTED_PROJECT_NAME + ".war").append(jarFolder).append(jarName));
		verifyList(earPath, toFind, true);
	}

	protected IDataModelProvider getDefaultAddReferenceProvider() {
		return new AddReferenceDataModelProvider();
	}
	
	
	protected int getExpectedFileCount() {
		// manifest.mf, web.xml, and the junit jar
		return 3;
	}

}
