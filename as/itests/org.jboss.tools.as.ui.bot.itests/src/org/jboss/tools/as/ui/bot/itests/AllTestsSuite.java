package org.jboss.tools.as.ui.bot.itests;

import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.jboss.tools.as.ui.bot.itests.archives.DeployingArchiveTest;
import org.jboss.tools.as.ui.bot.itests.archives.VariousProjectsArchiving;
import org.jboss.tools.as.ui.bot.itests.download.InvalidCredentialProductDownloadTest;
import org.jboss.tools.as.ui.bot.itests.parametized.server.RuntimeDetectionDuplicatesTest;
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
})
public class AllTestsSuite {

}
