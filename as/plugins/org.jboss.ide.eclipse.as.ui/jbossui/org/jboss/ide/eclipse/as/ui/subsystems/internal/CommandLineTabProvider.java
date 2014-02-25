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

import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.jboss.ide.eclipse.as.ui.launch.CommandLineLaunchTab;
import org.jboss.ide.eclipse.as.ui.subsystems.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;

public class CommandLineTabProvider extends AbstractSubsystemController implements IJBossLaunchTabProvider {
	public ILaunchConfigurationTab[] createTabs() {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new CommandLineLaunchTab(),
		};
		return tabs;
	}
}