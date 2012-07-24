package org.jboss.tools.as.test.core.parametized.server;

import org.jboss.tools.as.test.core.parametized.server.publishing.PublishingSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
//	ServerHomeTest.class,
//	ServerBeanLoader3Test.class,
//	CreateServerCheckDefaultsTest.class,
//	ProjectRuntimeClasspathTest.class,
//	CreateRuntimeTwiceTest.class,
	PublishingSuite.class
})
public class ParametizedSuite {
}
