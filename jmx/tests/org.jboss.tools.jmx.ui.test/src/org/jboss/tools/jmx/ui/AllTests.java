package org.jboss.tools.jmx.ui;
import org.jboss.tools.jmx.ui.internal.MBeanUtilsTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MBeanUtilsTestCase.class);
		return suite;
	}
}
