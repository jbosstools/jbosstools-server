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
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.classpath.core.runtime.RuntimeJarUtility;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class RuntimeJarUtilityTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParameters());
	}
	 
	public RuntimeJarUtilityTest(String serverType) {
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
	public void testRuntimeJarUtility() {
		RuntimeJarUtility util = new RuntimeJarUtility();
		IRuntime rt = server.getRuntime();
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put(ConfigNameResolver.JBOSS_CONFIG_DIR, getConfigDir());
		
		IPath[] result1 = util.getJarsForRuntime(rt, util.CLASSPATH_JARS);
		assertNotNull(result1);
		assertTrue(result1.length > 0);
		checkAllValid(result1);
		
		IPath[] result2 = util.getJarsForRuntimeHome(rt.getLocation().toString(), util.CLASSPATH_JARS, replacements, false);
		assertNotNull(result2);
		assertTrue(result2.length > 0);
		checkAllValid(result2);
		
		replacements.put(ConfigNameResolver.JBOSS_CONFIG_DIR, "/home/garbage/does/not/exist");
		IPath[] result3 = util.getJarsForRuntimeHome("/home/garbage/does/not/exist", util.CLASSPATH_JARS, replacements, false);
		assertNull(result3);
		
	}
	
	private static void checkAllValid(IPath[] paths) {
		for( int i = 0; i < paths.length; i++ ) {
			assertTrue(paths[i].isAbsolute());
			assertTrue(paths[i].toFile().exists());
		}
	}
	
	protected String getConfigDir() {
		IJBossServerRuntime ajbsrt = getJBossRuntime(server);
		if( ajbsrt != null ) {
			String config = null;
			if( ajbsrt != null ) 
				config = ajbsrt.getConfigLocationFullPath().append(ajbsrt.getJBossConfiguration()).toString();
			if( config != null )
				return config;
		}
		return null;
	}

	private IJBossServerRuntime getJBossRuntime(IServer s) {
		return RuntimeUtils.getJBossServerRuntime(s.getRuntime());
	}
}
