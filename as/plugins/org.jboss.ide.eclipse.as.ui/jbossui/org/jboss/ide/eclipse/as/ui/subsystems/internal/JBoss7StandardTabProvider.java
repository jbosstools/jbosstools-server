/******************************************************************************* 
* Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.subsystems.internal;

import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.jboss.ide.eclipse.as.ui.launch.internal.CustomJBossArgsTab;
import org.jboss.ide.eclipse.as.ui.launch.internal.JavaMainTabExtension;
import org.jboss.ide.eclipse.as.ui.subsystems.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;

public class JBoss7StandardTabProvider extends AbstractSubsystemController  implements IJBossLaunchTabProvider {
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