package org.jboss.tools.jmx.ui.test;
import org.jboss.tools.jmx.ui.internal.test.MBeanUtilsTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;


public class JMXUIAllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MBeanUtilsTestCase.class);
		return suite;
	}
}
