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

import static org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.JBossLaunchConfigProperties.isServerHomeSet;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.AbstractStartLaunchConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.launch.configuration.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

/**
 * @author André Dietisheim
 */
public class LaunchConfigTests extends TestCase {

	private IServer mockServer;

	@Override
	protected void setUp() throws Exception {
		this.mockServer = ServerRuntimeUtils.createMockJBoss7Server();
	}

	public void testDoesNotConfigureTwice() throws CoreException {
		ILaunchConfigurationWorkingCopy launchConfig = new MockLaunchConfigWorkingCopy();

		final String attributeKey = "configurationKey";
		final String attributeValue = "configurationValue";
		
		ILaunchConfigConfigurator mockConfigurator = new MockConfigurator(mockServer) {
			
			@Override
			protected void doConfigure(ILaunchConfigurationWorkingCopy launchConfig, JBossServer jbossServer, IJBossServerRuntime jbossRuntime) throws CoreException {
				/**
				 * should only be called once
				 */
				launchConfig.setAttribute(attributeKey, attributeValue);
			}
		};		
		
		assertEquals("defaultValue", launchConfig.getAttribute(attributeKey, "defaultValue"));
		launchConfig.setAttribute(attributeKey, "dummyValue");
		// overrides "dummyValue" by "configurationValue"
		mockConfigurator.configure(launchConfig);
		assertEquals(attributeValue, launchConfig.getAttribute(attributeKey, "defaultValue"));
		launchConfig.setAttribute(attributeKey, "dummyValue");
		// configure 2nd time: should not override existing value
		mockConfigurator.configure(launchConfig);
		assertEquals("dummyValue", launchConfig.getAttribute(attributeKey, "defaultValue"));
	}

	private class MockConfigurator extends AbstractStartLaunchConfigurator {

		private MockConfigurator(IServer server) throws CoreException {
			super(server);
		}

		@Override
		protected String getWorkingDirectory(JBossServer server, IJBossServerRuntime jbossRuntime) throws CoreException {
			return null;
		}

		@Override
		protected String getServerHome(IJBossServerRuntime runtime) {
			return null;
		}

		@Override
		protected String getMainType() {
			return null;
		}

		@Override
		protected String getDefaultProgramArguments(JBossServer server, IJBossServerRuntime runtime) {
			return null;
		}

		@Override
		protected String getHost(JBossServer server, IJBossServerRuntime runtime) {
			return null;
		}

		@Override
		protected List<String> getClasspath(JBossServer server, IJBossServerRuntime runtime,
				List<String> currentClasspath)
				throws CoreException {
			return null;
		}

		@Override
		protected String getServerConfig(IJBossServerRuntime runtime) {
			return null;
		}

		@Override
		protected String getEndorsedDir(IJBossServerRuntime runtime) {
			return null;
		}

		@Override
		protected String getDefaultVMArguments(IJBossServerRuntime runtime) {
			return null;
		}
	};

	public void testServerHomeIsNotSetIfNotCustomConfigLocation() throws CoreException {
		ILaunchConfigurationWorkingCopy launchConfig = LaunchConfigConfiguratorFactory.createNonCustomConfigLocationLaunchConfig(mockServer);
		
		assertFalse(isServerHomeSet(launchConfig));
	}

	public void testServerHomeIsSetIfCustomConfigLocation() throws CoreException {
		ILaunchConfigurationWorkingCopy launchConfig = LaunchConfigConfiguratorFactory.createCustomConfigLocationLaunchConfig(mockServer);
		
		assertTrue(isServerHomeSet(launchConfig));
	}
}
