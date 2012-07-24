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

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.jboss.tools.as.test.core.internal.utils.BundleUtils;
import org.jboss.tools.as.test.core.internal.utils.ComponentReferenceUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.test.util.JobUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class DeploymentAssemblyWorkspaceArchiveVarNestedWarTest extends DeploymentAssemblyArchivePathVarNestedWarTest {
	private static String PROJECT_NESTED_PREFIX = "q35War";
	private static String PROJECT_PREFIX = "q35Ear";
	private static int PROJECT_ID = 1;
	@Parameters
	public static Collection<Object[]> params() {
		return componentJarData();
	}
	public DeploymentAssemblyWorkspaceArchiveVarNestedWarTest(String serverType, String zip,
			String deployLoc, String perMod, String refName, String refFolder) {
		super(serverType, zip, deployLoc, perMod, refName, refFolder);
	}
	
	protected void setProjectName() {
		PROJECT_ID++;
		MY_PROJECT_NAME = PROJECT_PREFIX + PROJECT_ID;
		MY_NESTED_PROJECT_NAME = PROJECT_NESTED_PREFIX + PROJECT_ID;
	}
	
	protected IVirtualReference createArchiveReference(IVirtualComponent root) throws Exception {
		String tmpName = PROJECT_PREFIX + "tmp" + PROJECT_ID;
		IDataModel dm = CreateProjectOperationsUtility.getEARDataModel(tmpName, "ourContent", 
				null, null, JavaEEFacetConstants.EAR_5, false);
		IProject p = createSingleProject(dm, tmpName);
		File twiddleLoc = BundleUtils.getFileLocation("serverMock/3.2.8.mf.twiddle.jar");
		IFile f = p.getFile("inner.jar");
		f.create(new FileInputStream(twiddleLoc), true, new NullProgressMonitor());
		JobUtils.waitForIdle(300);
		IPath path = new Path(f.getProject().getName()).append(f.getProjectRelativePath());
		IVirtualReference ref = ComponentReferenceUtils.createWorkspaceJarReference(root, path,  "/" + jarFolder, jarName);
		return ref;
	}
}
