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
package org.jboss.tools.as.test.core.classpath;

import org.jboss.tools.as.test.core.classpath.modules.JBossModulesClasspathTest;
import org.jboss.tools.as.test.core.classpath.modules.LayeredModulePathFactoryTest;
import org.jboss.tools.as.test.core.classpath.modules.ModuleSlotStackOverflowDefectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	JEEClasspathContainerTest.class,
	LayeredModulePathFactoryTest.class,
	JBossModulesClasspathTest.class,
	ModuleSlotStackOverflowDefectTest.class
})
public class ClasspathSuite {
}
