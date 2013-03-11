package org.jboss.ide.eclipse.as.ui.test;

import org.jboss.ide.eclipse.as.ui.wizards.test.NewServerWizardTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
	ServerModeUIExtensionTest.class,
	NewServerWizardTest.class
})
public class AsUiAllTests {
}
