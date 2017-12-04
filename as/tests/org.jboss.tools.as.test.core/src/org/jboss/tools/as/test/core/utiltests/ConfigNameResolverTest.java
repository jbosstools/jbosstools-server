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
package org.jboss.tools.as.test.core.utiltests;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.parametized.server.ServerParameterUtils;
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
public class ConfigNameResolverTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParameters());
	}
	
	public ConfigNameResolverTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() throws CoreException {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	} 
	private static boolean isJBoss7Style(IServer server) {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(server);
		boolean as7Style = sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS; 
		return as7Style;
	}
	@Test
	public void testConfigNameResolver() {
		String[] vars = ConfigNameResolver.ALL_VARIABLES;
		for( int i = 0; i < vars.length; i++ ) {
			String var = ConfigNameResolver.getVariablePattern(vars[i]);
			String result = new ConfigNameResolver().performSubstitutions(var, server.getName());
			IJBossServer jbs = ServerConverter.getJBossServer(server);
			if( jbs == null ) {
				assertEquals("", result);
			} else  if( vars[i].equals(ConfigNameResolver.JBOSS_CONFIG))
				testConfig(var, result, isJBoss7Style(server));
			else if( vars[i].equals(ConfigNameResolver.JBOSS_CONFIG_DIR))
				testConfigDir(var, result, isJBoss7Style(server));
			else if( vars[i].equals(ConfigNameResolver.JBOSS_AS7_CONFIG_FILE)) 
				testAS7ConfigFile(var, result, isJBoss7Style(server));
			else if( vars[i].equals(ConfigNameResolver.JBOSS_SERVER_HOME)) 
				testServerHome(var, result, jbs.getServer().getRuntime());
			else
				fail("Variable " + vars[i] + " not tested");
		}
	}
	
	private void testConfig(String var, String result, boolean isAS7 ) {
		if( !isAS7) 
			assertEquals(result, "default");
		else 
			assertEquals(result, "");
	}
	private void testConfigDir(String var, String result, boolean isAS7 ) {
		if( !isAS7 ) {
			assertNotSame(result, var);
			assertNotSame(result, "");
		} else 
			// This is more documenting current behaviour than what it *should* be. 
			// Oh well :( 
			assertEquals(result, server.getRuntime().getLocation().append("standalone").append("configuration").toString());
	}
	private void testServerHome(String var, String result, IRuntime rt) {
		assertNotNull(rt);
		assertEquals(result, rt.getLocation().toOSString());
	}
	private void testAS7ConfigFile(String var, String result, boolean isAS7 ) {
		if( isAS7 ) 
			assertEquals(result, "standalone.xml");
		else 
			assertEquals(result, "");
	}
	
}
