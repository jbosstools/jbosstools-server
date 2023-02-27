/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.itests.server.mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetUtil;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.Fileset;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties.GetWelcomePageURLException;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.TestCase;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class CreateServerCheckDefaultsTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParameters());
	}
	 
	public CreateServerCheckDefaultsTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		try {
			server = ServerCreationTestUtils.createServerWithRuntime(serverType, getClass().getName() + serverType);
		} catch(CoreException ce) {
			ce.printStackTrace();
			fail();
		}
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	@Test
	public void testServerDefaults() {
		assertNotNull("Test setup failed to create a server", server);
		assertNotNull("Created server of type " + server.getServerType().getId() + " has no runtime", server.getRuntime());
		assertFalse("Server name should not be empty", server.getName() == null || "".equals(server.getName()));
		assertFalse("Runtime name should not be empty", server.getRuntime().getName() == null || "".equals(server.getRuntime().getName()));
		
		String typeId = server.getServerType().getId();
		if( typeId.equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			return;
		}

		
		IJBossServerRuntime rt = getJBossRuntime(server);
		assertNotNull("Created server of type " + server.getServerType().getId() + " does not adapt to IJBossServerRuntime", rt);
		assertNotNull(rt.getVM());
		assertNotNull(rt.getExecutionEnvironment());
		
		LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)server.getRuntime().loadAdapter(LocalJBoss7ServerRuntime.class, null);
		if( jb7rt != null ) {
			assertEquals("standalone.xml", jb7rt.getConfigurationFile());
		} else {
			assertEquals("default", rt.getJBossConfiguration());
		}
		
		ServerExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		assertNotNull(props);
		assertTrue("Server creation structure fails a verify call for server type " + serverType, props.verifyServerStructure().isOK());
		if( props.canVerifyRemoteModuleState())
			assertNotNull(props.getModuleStateVerifier());
		if( props.hasWelcomePage()) {
			try {
				String s = props.getWelcomePageUrl();
				URL url = new URL(s);
			} catch(MalformedURLException murle) {
				fail(serverType + " has an invalid welcome page url: " + murle.getMessage());
			} catch (GetWelcomePageURLException e) {
				fail("GetWelcomePageURLException is not expected here: " + e.getMessage());
			}
		}
	}
	
	@Test
	public void testDefaultFilesetsAdded() {
		Fileset[] fs = FilesetUtil.loadFilesets(server);
		String typeId = server.getServerType().getId();
		if( typeId.equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			assertTrue("Deploy-only Servers should have no default filesets.", 
					fs == null || fs.length == 0);
			return;
		}

		assertNotNull("No filesets created", fs);
		assertTrue("0 filesets added for server type " + serverType, fs.length > 0);
	}
	
	private IJBossServerRuntime getJBossRuntime(IServer s) {
		return RuntimeUtils.getJBossServerRuntime(s.getRuntime());
	}
}
