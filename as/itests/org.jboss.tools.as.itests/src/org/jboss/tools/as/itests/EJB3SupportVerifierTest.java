/*******************************************************************************
 * Copyright (c) 2007 - 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.itests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.classpath.core.ejb3.EJB30SupportVerifier;
import org.jboss.ide.eclipse.as.classpath.core.ejb3.EJB3ClasspathContainer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.TestConstants;
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
public class EJB3SupportVerifierTest extends TestCase {
	private String serverType;
	private IServer server;
	private String serverHome;
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerHomeParameters());
	}
	
	private static ArrayList<String> nonEjb3Types;
	static {
		nonEjb3Types = new ArrayList<String>();
		nonEjb3Types.add(IJBossToolingConstants.SERVER_AS_40);
		nonEjb3Types.add(IJBossToolingConstants.SERVER_AS_32);
		nonEjb3Types.add(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
	}
	
	public EJB3SupportVerifierTest(String serverHome) {
		this.serverHome = serverHome;
	}
	
	@Before
	public void setUp() throws CoreException {
		this.serverType = TestConstants.serverHomeDirToServerType().get(serverHome);
		this.server = ServerCreationTestUtils.createServerWithRuntime(this.serverType, 
				getClass().getName() + this.serverType, new File(this.serverHome));
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	} 
	
	@Test
	public void testEJB30Support() {
		boolean shouldSupportEjb3 = !nonEjb3Types.contains(serverType);
		boolean supported = EJB30SupportVerifier.verify(server.getRuntime());
		assertEquals("Error on servertype " + serverType, Boolean.valueOf(supported), Boolean.valueOf(shouldSupportEjb3));
		IPath containerPath = new Path(EJB3ClasspathContainer.CONTAINER_ID).append(server.getName());
		EJB3ClasspathContainer container = new EJB3ClasspathContainer(containerPath, null);
		IClasspathEntry[] entries = container.getClasspathEntries();
		if( supported ) {
			assertTrue("server type " + serverType + " supports ejb3 but receives no jars from this classpath container",
					entries != null && entries.length > 0);
		} else {
			assertTrue(entries != null && entries.length == 0);
		}
	}
}
