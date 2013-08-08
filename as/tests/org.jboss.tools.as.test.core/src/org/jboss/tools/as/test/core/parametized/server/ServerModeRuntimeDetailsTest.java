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

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.server.internal.LocalServerModeDetails;
import org.jboss.ide.eclipse.as.rse.core.RSECorePlugin;
import org.jboss.ide.eclipse.as.rse.core.RSEServerModeDetails;
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
public class ServerModeRuntimeDetailsTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParameters());
	}
	 
	public ServerModeRuntimeDetailsTest(String serverType) {
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
		
		// Next try with rse mode
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.SERVER_MODE, "rse");
		try {
			server = wc.save(true,  new NullProgressMonitor());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		ret = (IServerModeDetails)Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class);
		assertNotNull(ret);
		assertTrue(ret instanceof RSEServerModeDetails);
		
		
		// Now try with a garbage mode
		wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.SERVER_MODE, "garbage222");
		try {
			server = wc.save(true,  new NullProgressMonitor());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		ret = (IServerModeDetails) Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class);
		assertNull(ret);
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
