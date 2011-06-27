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
package org.jboss.ide.eclipse.as.test.launch;

import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.areEnvironmentVariablesSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.areProgramArgumentsSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.areVMArgumentsSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isClasspathProviderSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isClasspathSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isConfigSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isDefaultClasspathSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isEndorsedDirSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isHostSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isJreContainerSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isMainTypeSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isServerHomeSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isServerIdSet;
import static org.jboss.ide.eclipse.as.core.server.internal.launch.JBossRuntimeLaunchConfigUtils.isWorkingDirectorySet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

/**
 * @author André Dietisheim
 */
public class JBossLaunchConfigTests extends LaunchConfigTests {
	
	private IServer mockServer;

	@Override
	protected void setUp() throws Exception {
		this.mockServer = ServerRuntimeUtils.createMockJBoss7Server();
	}

	public void testConfiguration() throws CoreException {
		ILaunchConfigurationWorkingCopy launchConfig = LaunchConfigConfiguratorFactory.createCustomConfigLocationLaunchConfig(mockServer);
		
		assertTrue(areProgramArgumentsSet(launchConfig));
		assertTrue(areVMArgumentsSet(launchConfig));
		assertTrue(isHostSet(launchConfig));
		assertTrue(isMainTypeSet(launchConfig));
		assertTrue(isWorkingDirectorySet(launchConfig));
		assertTrue(areEnvironmentVariablesSet(launchConfig));
		assertTrue(isClasspathSet(launchConfig));
		assertTrue(isClasspathProviderSet(launchConfig));
		assertTrue(isDefaultClasspathSet(launchConfig));
		assertTrue(isJreContainerSet(launchConfig));
		assertTrue(isConfigSet(launchConfig));
		assertTrue(isServerHomeSet(launchConfig));
		assertTrue(isEndorsedDirSet(launchConfig));
		assertTrue(isServerIdSet(launchConfig));
	}


}
