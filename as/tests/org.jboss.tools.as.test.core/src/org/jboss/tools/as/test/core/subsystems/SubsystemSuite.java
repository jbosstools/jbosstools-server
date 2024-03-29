/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AllSubsystemResolutionTest.class,
	DeploymentOptionsSubsystemResolutionTest.class,
	BrowseBehaviorSubsystemResolutionTest.class,
	ExploreBehaviorSubsystemResolutionTest.class,
	PublishSubsystemResolutionTest.class,
	TabGroupSubsystemResolutionTest.class,
	XPathPortDiscoveryResolutionTest.class,
	ServerSubsystemTest1.class,
	ModulePathFilterUtilityTest.class,
	ModuleRestartBehaviorControllerTest.class,
	DeploymentSettingsControllerTest.class,
	LocalFilesystemSubsystemTest.class,
	ModuleDeployPathControllerTest.class,
	//RSEFilesystemSubsystemTest.class,
	PublishRunnerTest.class,
	ZippedPublishRunnerTest.class,
	StandardFilesystemPublishControllerTest.class,
	ServerProfileTest.class,
})
public class SubsystemSuite {
}
