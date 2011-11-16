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
package org.jboss.ide.eclipse.as.ui.launch;

import java.util.ArrayList;

import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBoss7LaunchConfigurationTabGroup extends JBossLaunchConfigurationTabGroup {

	public static ArrayList<IJBossLaunchTabProvider> providers7 = 
			new ArrayList<IJBossLaunchTabProvider>();
	static {
		providers7.add(new JBoss7StandardTabProvider());
	}
	public static void addTabProvider(IJBossLaunchTabProvider provider) {
		providers7.add(provider);
	}
	
	public static class JBoss7StandardTabProvider implements IJBossLaunchTabProvider {
		public ILaunchConfigurationTab[] createTabs() {
			ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
					new CustomJBossArgsTab(),
					new JavaMainTabExtension(),
					new JavaClasspathTab(),
					new SourceLookupTab(),
					new EnvironmentTab(),
					new CommonTab()
			};
			return tabs;
		}
	}
	
	public ArrayList<IJBossLaunchTabProvider> getProviderList() {
		return providers7;
	}
}
