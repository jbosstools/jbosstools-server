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
package org.jboss.tools.as.test.core;

import org.jboss.tools.as.test.core.catalog.CatalogMissingEntriesTest;
import org.jboss.tools.as.test.core.catalog.CatalogValidationTest;
import org.jboss.tools.as.test.core.classpath.ClasspathSuite;
import org.jboss.tools.as.test.core.parametized.server.ParametizedSuite;
import org.jboss.tools.as.test.core.polling.PollThreadTest;
import org.jboss.tools.as.test.core.portal.LaunchProjectOnJPP6Test;
import org.jboss.tools.as.test.core.runtime.RuntimeSuite;
import org.jboss.tools.as.test.core.runtimedetect.RuntimeDetectionTest;
import org.jboss.tools.as.test.core.subsystems.SubsystemSuite;
import org.jboss.tools.as.test.core.utiltests.UtilsSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	SubsystemSuite.class,
	ParametizedSuite.class,
	RuntimeSuite.class,
	PollThreadTest.class,
	ClasspathSuite.class,
	RuntimeDetectionTest.class,
	CatalogMissingEntriesTest.class,
	CatalogValidationTest.class,
	UtilsSuite.class,
	LaunchProjectOnJPP6Test.class,
})
public class ASToolsUnitTestSuite {
}
