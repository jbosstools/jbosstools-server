/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.parametized.server.publishing;

import org.jboss.tools.as.test.core.parametized.server.publishing.defect.PublishDefectSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DeploymentAssemblyArchivePathVarTest.class,
 	DeploymentAssemblyArchivePathVarWarTest.class,
	DeploymentAssemblyArchivePathVarNestedWarTest.class,
	DeploymentAssemblyExternalArchiveVarTest.class,
	DeploymentAssemblyExternalArchiveVarWarTest.class,
	DeploymentAssemblyExternalArchiveVarNestedWarTest.class,
	DeploymentAssemblyWorkspaceArchiveVarTest.class,
	DeploymentAssemblyWorkspaceArchiveVarWarTest.class,
	DeploymentAssemblyWorkspaceArchiveVarNestedWarTest.class,
	DeploymentAssemblyFilesetReferenceTest.class,
	SingleDeployableFileTest.class,
	SingleDeployableFolderTest.class,
	PublishDefectSuite.class
})
public class PublishingSuite {
}
