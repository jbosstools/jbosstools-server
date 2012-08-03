package org.jboss.tools.as.test.core.classpath;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	JEEClasspathContainerTest.class,
	EJB3SupportVerifierTest.class
})
public class ClasspathSuite {
}
