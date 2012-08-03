package org.jboss.tools.as.test.core.utiltests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConfigNameResolverTest.class,
	ArgsUtilTest.class,
	ExpressionResolverUtilTest.class,
	UnitedServerListenerTest.class
})
public class UtilsSuite {
}
