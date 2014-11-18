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
package org.jboss.ide.eclipse.as.integration.tests;

import org.jboss.ide.eclipse.as.integration.tests.server.publishing.PublishingSuite;
import org.jboss.ide.eclipse.as.integration.tests.server.publishing.RepublishDefectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ServerHomeTest.class, 
	CreateServerCheckDefaultsTest.class,
	ProjectRuntimeClasspathTest.class,
	ServerBeanLoaderIntegrationTest.class,
	PublishingSuite.class,
	RepublishDefectTest.class,
	RuntimeJarUtilityTest.class,
	EJB3SupportVerifierTest.class,
})
public class ASToolsIntegrationTestSuite {
}
