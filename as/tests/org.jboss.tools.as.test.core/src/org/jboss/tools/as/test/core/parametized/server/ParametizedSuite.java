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
package org.jboss.tools.as.test.core.parametized.server;

import org.jboss.tools.as.test.core.launch.DeploymentScannerAdditionsTest;
import org.jboss.tools.as.test.core.launch.MockArgsTests;
import org.jboss.tools.as.test.core.parametized.server.publishing.PublishingSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ServerHomeTest.class,
	ServerBeanLoader3Test.class,
	CreateServerCheckDefaultsTest.class,
	ServerModeRuntimeDetailsTest.class,
	ProjectRuntimeClasspathTest.class,
	RuntimeJarUtilityTest.class, 
	CreateRuntimeTwiceTest.class,
	XPathModelTest.class,
	MockArgsTests.class, 
	DeploymentScannerAdditionsTest.class,
	PublishingSuite.class,
	ServerBeanRuntimeMatcherTest.class,
	DownloadRuntimeMatcherTest.class
})
public class ParametizedSuite {
}
