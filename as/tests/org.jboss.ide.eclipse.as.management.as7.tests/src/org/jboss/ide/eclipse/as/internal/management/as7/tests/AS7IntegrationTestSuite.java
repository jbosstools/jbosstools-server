/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.internal.management.as7.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Andre Dietisheim
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AS7ManagementServicePreReqTest.class,
	AS7ManagementServiceStartupShutdownTest.class,
	AS7ManagerIntegrationTest.class,
	JBossManagementServiceTest.class
})
public class AS7IntegrationTestSuite {

}
