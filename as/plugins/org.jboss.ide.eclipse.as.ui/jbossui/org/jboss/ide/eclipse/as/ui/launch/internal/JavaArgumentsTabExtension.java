/******************************************************************************* 
* Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.launch.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;

public class JavaArgumentsTabExtension extends JavaArgumentsTab {
	private String originalHost=null;
	private String originalConf=null;
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		try {
			String startArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
			originalHost = ArgsUtil.getValue(startArgs, "-b", "--host"); //$NON-NLS-1$ //$NON-NLS-2$
			originalConf = ArgsUtil.getValue(startArgs, "-c", "--configuration"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch( CoreException ce ) {
			// This can only happen if loading properties from a launch config is f'd, 
			// in which case it's a big eclipse issue
			JBossServerUIPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, 
					"Error loading details from launch configuration", ce));
		}
	}
	public boolean isValid(ILaunchConfiguration config) {
		if( !super.isValid(config)) 
			return false;
		try {
			String startArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
			String newHost = ArgsUtil.getValue(startArgs, "-b", "--host"); //$NON-NLS-1$ //$NON-NLS-2$
			String newConf = ArgsUtil.getValue(startArgs, "-c", "--configuration"); //$NON-NLS-1$ //$NON-NLS-2$
			if( newConf == null || !newConf.equals(originalConf)) 
				return false;
			if( newHost == null || !newHost.equals(originalHost)) 
				return false;
		} catch( CoreException ce ) {
			// This can only happen if loading properties from a launch config is f'd, 
			// in which case it's a big eclipse issue
			JBossServerUIPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, 
					"Error loading details from launch configuration", ce));
		}
		return true;
	}
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			String startArgs = getAttributeValueFrom(fPrgmArgumentsText);
			String newHost = ArgsUtil.getValue(startArgs, "-b", "--host"); //$NON-NLS-1$ //$NON-NLS-2$
			String newConf = ArgsUtil.getValue(startArgs, "-c", "--configuration"); //$NON-NLS-1$ //$NON-NLS-2$
			if( newConf == null || !newConf.equals(originalConf)) 
				return Messages.LaunchInvalidConfigChanged;
			if( newHost == null || !newHost.equals(originalHost)) 
				return Messages.LaunchInvalidHostChanged;
		}
		return m;
	}
}