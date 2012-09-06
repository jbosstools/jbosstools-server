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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.junit.buildpath.BuildPathSupport;
import org.eclipse.jst.j2ee.application.internal.operations.AddReferenceToEnterpriseApplicationDataModelProvider;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.tools.as.test.core.internal.utils.ComponentReferenceUtils;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.classpath.ASToolsInternalVariableInitializer;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.framework.Bundle;

@RunWith(value = Parameterized.class)
public class DeploymentAssemblyArchivePathVarTest extends AbstractComponentPublishingTest {

	private static String PROJECT_PREFIX = "pEar";
	private static int PROJECT_ID = 1;
	@Parameters
	public static Collection<Object[]> params() {
		return fullComponentJarData();
	}
	public DeploymentAssemblyArchivePathVarTest(String serverType, String zip,
			String deployLoc, String perMod, String refName, String refFolder) {
		super(serverType, zip, deployLoc, perMod, refName, refFolder);
	}
	
	protected String MY_PROJECT_NAME;
	protected void createProjects() throws Exception {
		setProjectName();
		IProject p = projectCreation();
		addReferences(p);
		addModuleToServer(p);
		
	}
	
	protected void setProjectName() {
		PROJECT_ID++;
		MY_PROJECT_NAME = PROJECT_PREFIX + PROJECT_ID;
	}
	
	/* Return the project which will have the component added */
	protected IProject projectCreation() throws Exception {
		return createSingleProject(getSingleProjectCreationDataModel(), MY_PROJECT_NAME);
	}
	
	protected IProject createSingleProject(IDataModel dm, String name) throws Exception {
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourceUtils.findProject(name);
		if(!p.exists())
			fail();
		return p;
	}
	
	protected IDataModel getSingleProjectCreationDataModel() {
		return CreateProjectOperationsUtility.getEARDataModel(MY_PROJECT_NAME, "ourContent", 
				null, null, JavaEEFacetConstants.EAR_5, false);
	}
	
	protected void addReferences(IProject p) throws Exception  {
		IVirtualComponent vc = ComponentCore.createComponent(p);
		IVirtualReference ref = createArchiveReference(vc);
		ComponentReferenceUtils.addReferenceToComponent(vc, ref, getDefaultAddReferenceProvider());
	}
	
	protected IVirtualReference createArchiveReference(IVirtualComponent vc) throws Exception {
		ASToolsInternalVariableInitializer.ensureFoldersCreated();
		IPath path = new Path(ASToolsInternalVariableInitializer.ASTOOLS_TEST_HOME_VAR + "/junit.jar"); //$NON-NLS-1$
		IVirtualReference ref = ComponentReferenceUtils.createPathArchiveReference(vc, path, jarFolder, jarName);
		return ref;
	}
	
	protected IDataModelProvider getDefaultAddReferenceProvider() {
		return new AddReferenceToEnterpriseApplicationDataModelProvider();
	}
	

	protected void addModuleToServer(IProject p)  throws CoreException {
		IModule module = ServerUtil.getModule(p);
		addModuleToServer(module);
	}
	
	//wtp305306_patchBuildTest
	@Test
	public void testVariableReference() throws Exception {
		verifyFileFoundInModule(jarFolder, jarName, getExpectedFileCount());
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		IPath earPath = getLocalPublishMethodDeployRoot();
		ArrayList<IPath> toFind = new ArrayList<IPath>();
		toFind.add(earPath.append(jarFolder).append(jarName));
		verifyList(earPath, toFind, true);
	}
	
	protected int getExpectedFileCount() {
		return 1;
	}
}
