package org.jboss.ide.eclipse.as.ui.test;

import org.jboss.ide.eclipse.as.ui.wizards.test.NewServerWizardTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllAsUiTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite(AllAsUiTests.class.getName());
		suite.addTestSuite(NewServerWizardTest.class);
		return suite;
	}
	
}
