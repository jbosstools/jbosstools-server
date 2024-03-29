/******************************************************************************* 
 * Copyright (c) 2023 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.jmx.ui.bot.itests;

import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.jboss.tools.as.jmx.ui.bot.itests.server.connection.JMXServerConnectionAutoDeployDisableTest;
import org.jboss.tools.as.jmx.ui.bot.itests.server.connection.JMXServerConnectionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(RedDeerSuite.class)
@SuiteClasses({
	JMXServerConnectionTest.class,
	JMXServerConnectionAutoDeployDisableTest.class
})
public class JMXSmokeSuiteTests {
}