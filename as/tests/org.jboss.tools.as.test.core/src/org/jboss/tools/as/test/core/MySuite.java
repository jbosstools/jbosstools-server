package org.jboss.tools.as.test.core;

import org.jboss.tools.as.test.core.parametized.server.ParametizedSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ParametizedSuite.class
})
public class MySuite {
}
