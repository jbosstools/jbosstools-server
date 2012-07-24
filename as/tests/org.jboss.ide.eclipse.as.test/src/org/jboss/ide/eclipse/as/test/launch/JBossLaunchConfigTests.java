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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.LocalJBossStartLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7LaunchConfigProperties;
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
		ILaunchConfigurationWorkingCopy launchConfig = createCustomConfigLocationLaunchConfig(mockServer);
		JBossLaunchConfigProperties props = new JBossLaunchConfigProperties();
		assertTrue(props.areProgramArgumentsSet(launchConfig));
		assertTrue(props.areVMArgumentsSet(launchConfig));
		assertTrue(props.isHostSet(launchConfig));
		assertTrue(props.isMainTypeSet(launchConfig));
		assertTrue(props.isWorkingDirectorySet(launchConfig));
		assertTrue(props.areEnvironmentVariablesSet(launchConfig));
		assertTrue(props.isClasspathSet(launchConfig));
		assertTrue(props.isClasspathProviderSet(launchConfig));
		assertTrue(props.isUseDefaultClasspathSet(launchConfig));
		assertTrue(props.isJreContainerSet(launchConfig));
		assertTrue(props.isConfigSet(launchConfig));
		assertTrue(props.isServerHomeSet(launchConfig));
		assertTrue(props.isEndorsedDirSet(launchConfig));
		assertTrue(props.isServerIdSet(launchConfig));
	}


	public static ILaunchConfigurationWorkingCopy createCustomConfigLocationLaunchConfig(IServer mockServer) throws CoreException {
		MockLaunchConfigWorkingCopy launchConfig = new MockLaunchConfigWorkingCopy();
		LocalJBossStartLaunchConfigurator configurator = new LocalJBossStartLaunchConfigurator(mockServer) {
			@Override
			protected boolean isCustomConfigLocation(IJBossServerRuntime runtime) {
				return false;
			}
		};
		configurator.configure(launchConfig);
		return launchConfig;
	}

	public static  MockLaunchConfigWorkingCopy createNonCustomConfigLocationLaunchConfig(IServer server) throws CoreException {
		MockLaunchConfigWorkingCopy launchConfig = new MockLaunchConfigWorkingCopy();
		new LocalJBossStartLaunchConfigurator(server).configure(launchConfig);
		return launchConfig;
	}

}
