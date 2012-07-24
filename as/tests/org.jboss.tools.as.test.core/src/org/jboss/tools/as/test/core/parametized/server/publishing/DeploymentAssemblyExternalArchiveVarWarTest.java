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
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.jboss.tools.as.test.core.internal.utils.BundleUtils;
import org.jboss.tools.as.test.core.internal.utils.ComponentReferenceUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class DeploymentAssemblyExternalArchiveVarWarTest extends DeploymentAssemblyArchivePathVarWarTest {

	private static String PROJECT_PREFIX = "qWar";
	private static int PROJECT_ID = 1;
	@Parameters
	public static Collection<Object[]> params() {
		return componentJarData();
	}
	public DeploymentAssemblyExternalArchiveVarWarTest(String serverType, String zip,
			String deployLoc, String perMod, String refName, String refFolder) {
		super(serverType, zip, deployLoc, perMod, refName, refFolder);
	}
	
	protected void setProjectName() {
		PROJECT_ID++;
		MY_PROJECT_NAME = PROJECT_PREFIX + PROJECT_ID;
	}

	protected IVirtualReference createArchiveReference(IVirtualComponent root) throws CoreException {
		File twiddleLoc = BundleUtils.getFileLocation("serverMock/3.2.8.mf.twiddle.jar");
		IPath path = new Path(twiddleLoc.getAbsolutePath());
		IVirtualReference ref = ComponentReferenceUtils.createExternalJarReference(root, path,  "/" + jarFolder, jarName);
		return ref;
	}
}
