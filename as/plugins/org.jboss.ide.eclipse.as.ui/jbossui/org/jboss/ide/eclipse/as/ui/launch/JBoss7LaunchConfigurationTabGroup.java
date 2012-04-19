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
import java.util.HashMap;

import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBoss7LaunchConfigurationTabGroup extends JBossLaunchConfigurationTabGroup {

	public static HashMap<String, ArrayList<IJBossLaunchTabProvider>> providers7 = 
			new HashMap<String, ArrayList<IJBossLaunchTabProvider>>();
	static {
		ArrayList<IJBossLaunchTabProvider> l = new ArrayList<IJBossLaunchTabProvider>();
		l.add(new JBoss7StandardTabProvider());
		providers7.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, l);
	}
	
	public static void addTabProvider(String behavior, IJBossLaunchTabProvider provider) {
		ArrayList<IJBossLaunchTabProvider> l = providers7.get(behavior);
		if( l == null ) {
			l = l == null ? new ArrayList<IJBossLaunchTabProvider>() : l;
			providers7.put(behavior, l);
		}
		l.add(provider);
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
	
	public ArrayList<IJBossLaunchTabProvider> getProvider(String type) {
		return providers7.get(type) == null ? new ArrayList<IJBossLaunchTabProvider>() : providers7.get(type);
	}
}
