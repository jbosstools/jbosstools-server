/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[10];
		int i = 0;
		tabs[i] = new JavaArgumentsTabExtension();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new JavaClasspathTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new SourceLookupTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new EnvironmentTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new CommonTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);

		
		ILaunchConfigurationTab[] tabs2 = new ILaunchConfigurationTab[i];
		System.arraycopy(tabs, 0, tabs2, 0, i);
		setTabs(tabs2);
	}
	
	public class JavaArgumentsTabExtension extends JavaArgumentsTab {
		private String originalHost=null;
		private String originalConf=null;
		public void initializeFrom(ILaunchConfiguration configuration) {
			super.initializeFrom(configuration);
			try {
				String startArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
				originalHost = ArgsUtil.getValue(startArgs, "-b", "--host"); //$NON-NLS-1$ //$NON-NLS-2$
				originalConf = ArgsUtil.getValue(startArgs, "-c", "--configuration"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch( CoreException ce ) { }
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
			} catch( CoreException ce ) {}
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
}
