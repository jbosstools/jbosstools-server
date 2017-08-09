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
package org.jboss.tools.as.itests;

import org.jboss.tools.as.itests.archives.ProjectArchivesBuildDeployTest;
import org.jboss.tools.as.itests.server.publishing.PublishingSuite;
import org.jboss.tools.as.itests.server.publishing.RepublishDefectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	// First run specific tests
	ServerHomeTest.class, 
	CreateServerCheckDefaultsTest.class,
	ProjectRuntimeClasspathTest.class,
	ServerBeanLoaderIntegrationTest.class,
	RepublishDefectTest.class,
	RuntimeJarUtilityTest.class,
	EJB3SupportVerifierTest.class,
	ProjectArchivesBuildDeployTest.class,
	
	// Then run extensive suites
	PublishingSuite.class,
})
public class ASToolsIntegrationTestSuite {
}
