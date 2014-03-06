/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.RecentlyUpdatedServerLaunches;
import org.jboss.ide.eclipse.as.core.server.launch.CommandLineLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchConfigProperties;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.launch.CommandLineLaunchTab;
import org.jboss.ide.eclipse.as.ui.subsystems.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.tools.as.core.server.controllable.systems.ICommandLineShutdownController;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 *
 */
public class RSELaunchTabProvider extends AbstractSubsystemController implements IJBossLaunchTabProvider {

	public ILaunchConfigurationTab[] createTabs() {
		return new ILaunchConfigurationTab[]{
				new RSERemoteLaunchTab()
		};
	}
	
	public class RSERemoteLaunchTab extends CommandLineLaunchTab {
		@Override
		public String getName() {
			return RSEUIMessages.RSE_REMOTE_LAUNCH;
		}
		@Override
		protected boolean canAutoDetectCommand() {
			return true;
		}

		protected int getStartCommandHeightHint() {
			return 120;
		}
		protected int getStopCommandHeightHint() {
			return 75;
		}
		
		@Override
		protected CommandLineLaunchConfigProperties getPropertyUtility() {
			if( propertyUtil == null )
				propertyUtil = new RSELaunchConfigProperties();
			return propertyUtil;
		}
	}
}
