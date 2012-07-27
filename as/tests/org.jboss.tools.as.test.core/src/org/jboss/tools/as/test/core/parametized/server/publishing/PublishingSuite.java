package org.jboss.tools.as.test.core.parametized.server.publishing;

import org.jboss.tools.as.test.core.parametized.server.publishing.defect.RepublishDefectTest;
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
	RepublishDefectTest.class,
	SingleDeployableFileTest.class,
	SingleDeployableFolderTest.class
})
public class PublishingSuite {
}
