package org.jboss.tools.as.ui.bot.itests;

import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.jboss.tools.as.ui.bot.itests.server.SingleServerDirectoryStructureTest;
import org.jboss.tools.as.ui.bot.itests.server.SingleServerRuntimeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(RedDeerSuite.class)
@Suite.SuiteClasses({ 
	SingleServerRuntimeTest.class,
	SingleServerDirectoryStructureTest.class
})
public class EAPTestsSuite {

}
