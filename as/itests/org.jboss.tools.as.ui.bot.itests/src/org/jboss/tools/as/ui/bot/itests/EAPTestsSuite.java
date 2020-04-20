/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests;

import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.jboss.tools.as.ui.bot.itests.server.SingleServerAdaptersTest;
import org.jboss.tools.as.ui.bot.itests.server.SingleServerDirectoryStructureTest;
import org.jboss.tools.as.ui.bot.itests.server.SingleServerRuntimeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(RedDeerSuite.class)
@Suite.SuiteClasses({ 
	SingleServerRuntimeTest.class,
	SingleServerDirectoryStructureTest.class,
	SingleServerAdaptersTest.class,
})
public class EAPTestsSuite {

}
