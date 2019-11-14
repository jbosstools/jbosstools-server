  /*******************************************************************************
 * Copyright (c) 2007-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests;

import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.jboss.tools.as.ui.bot.itests.archives.DeployingArchiveTest;
import org.jboss.tools.as.ui.bot.itests.archives.VariousProjectsArchiving;
import org.jboss.tools.as.ui.bot.itests.download.InvalidCredentialProductDownloadTest;
import org.jboss.tools.as.ui.bot.itests.parametized.server.RuntimeDetectionDuplicatesTest;
import org.jboss.tools.as.ui.bot.itests.parametized.server.ServerAdaptersTest;
import org.jboss.tools.as.ui.bot.itests.parametized.server.ServerRuntimesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(RedDeerSuite.class)
@Suite.SuiteClasses({ 
	InvalidCredentialProductDownloadTest.class, 
	ServerRuntimesTest.class,
	VariousProjectsArchiving.class,
	DeployingArchiveTest.class,
	RuntimeDetectionDuplicatesTest.class,
	ServerAdaptersTest.class
})
public class AllTestsSuite {

}
