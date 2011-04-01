package org.jboss.ide.eclipse.as.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class DeployOnlyLaunchConfigGroup extends
		AbstractLaunchConfigurationTabGroup {

	public DeployOnlyLaunchConfigGroup() {
		super();
	}

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs(new ILaunchConfigurationTab[]{});
	}
}
