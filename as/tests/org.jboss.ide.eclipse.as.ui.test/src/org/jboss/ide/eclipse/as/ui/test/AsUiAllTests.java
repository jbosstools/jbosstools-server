package org.jboss.ide.eclipse.as.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ide.eclipse.as.ui.wizards.test.NewServerWizardTest;

public class AsUiAllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite(AsUiAllTests.class.getName());
		suite.addTestSuite(NewServerWizardTest.class);
//		suite.addTestSuite(ASPerspectiveTest.class);
		return suite;
	}
	
}
