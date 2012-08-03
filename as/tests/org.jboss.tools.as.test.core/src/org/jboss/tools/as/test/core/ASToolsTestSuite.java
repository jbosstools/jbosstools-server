package org.jboss.tools.as.test.core;

import org.jboss.tools.as.test.core.classpath.ClasspathSuite;
import org.jboss.tools.as.test.core.parametized.server.ParametizedSuite;
import org.jboss.tools.as.test.core.parametized.server.publishing.defect.PublishWeb2DeletesWeb1LibsTest;
import org.jboss.tools.as.test.core.polling.PollThreadTest;
import org.jboss.tools.as.test.core.utiltests.UtilsSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ParametizedSuite.class,
	UtilsSuite.class,
	PollThreadTest.class,
	ClasspathSuite.class
})
public class ASToolsTestSuite {
}
