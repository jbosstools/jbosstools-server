/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.parametized.server;

import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.server.internal.LocalServerModeDetails;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossDefaultLaunchArguments;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.rse.core.RSECorePlugin;
import org.jboss.ide.eclipse.as.rse.core.RSEServerModeDetails;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class ServerDefaultLaunchArgsTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParameters());
	}
	 
	public ServerDefaultLaunchArgsTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	

	
	
	@Test
	public void testServerDefaults() {
		forceStart(RSECorePlugin.PLUGIN_ID);
		// First try with local mode
		IServerModeDetails ret = (IServerModeDetails)
				Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class);
		assertNotNull(ret);
		assertTrue(ret instanceof LocalServerModeDetails);
		JBossExtendedProperties props1 = (JBossExtendedProperties)
				Platform.getAdapterManager().getAdapter(server, JBossExtendedProperties.class);
		boolean as7Style = props1.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS;
		JBossDefaultLaunchArguments localArgs = (JBossDefaultLaunchArguments)props1.getDefaultLaunchArguments();
		
		String progArgs = localArgs.getStartDefaultProgramArgs();
		String vmArgs = localArgs.getStartDefaultVMArgs();
		if( as7Style ) {
			// verify for as7-style servers
			assertTrue(vmArgs.matches(".*mockedServers." + serverType + ".standalone.configuration.logging.properties.*"));
		} else {
			// verify for as<7 serversnt
			boolean matches1a = vmArgs.matches(".*-Djava.endorsed.dirs=.*metadata..plugins.org.jboss.tools.as.test.core.mockedServers..*.lib.endorsed.*");
			assertTrue(matches1a);
		}
		
		
		
		// Next try with rse mode
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerProfileModel.setProfile(wc, "rse");
		wc.setAttribute(RSEUtils.RSE_BASE_DIR, "standNOTalone");
		wc.setAttribute(RSEUtils.RSE_SERVER_HOME_DIR, "/home/otherUser/jboss");
		ServerExtendedProperties props2 = (ServerExtendedProperties)
				Platform.getAdapterManager().getAdapter(server, ServerExtendedProperties.class);
		String configVal = as7Style ? "standNOTalone.xml" : "myDefConfig";
		wc.setAttribute(RSEUtils.RSE_SERVER_CONFIG, configVal);
		try {
			server = wc.save(true,  new NullProgressMonitor());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		ret = (IServerModeDetails)Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class);
		assertNotNull(ret);
		assertTrue(ret instanceof RSEServerModeDetails);
		
		try {
			ILaunchConfiguration lc22 = server.getLaunchConfiguration(true, new NullProgressMonitor());
			String startupCmd = lc22.getAttribute("org.jboss.ide.eclipse.as.rse.core.RSEJBossStartLaunchDelegate.STARTUP_COMMAND", (String)null);
			assertNotNull(startupCmd);
			
			startupCmd = startupCmd.replaceAll("\"", "");
			if (as7Style) {
				// verify for as7-style servers
				String c1 = "/home/otherUser/jboss/standNOTalone/configuration/logging.properties";
				assertTrue(startupCmd + " should contain " + c1, startupCmd.contains(c1));
				if( startupCmd.contains("-Djboss.home.dir")) {
					String c2 = "-Djboss.home.dir=/home/otherUser/jboss";
					assertTrue(startupCmd + " should contain " + c1, startupCmd.contains(c2));
				}
			} else {
				// verify for as<7 servers
				String c1 = "-Djava.endorsed.dirs=/home/otherUser/jboss/lib/endorsed";
				assertTrue(startupCmd + " should contain " + c1,  startupCmd.contains(c1));
			}

		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}
	
	
	private static boolean forceStart(String bundleId) {
		Bundle bundle = Platform.getBundle(bundleId); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.ACTIVE) {
			try {
				bundle.start();
			} catch (BundleException e) {
				// ignore
			}
		}
		return bundle.getState() == Bundle.ACTIVE;
	}
}
