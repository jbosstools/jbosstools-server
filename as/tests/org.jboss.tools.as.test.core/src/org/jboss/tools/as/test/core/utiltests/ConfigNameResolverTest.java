package org.jboss.tools.as.test.core.utiltests;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
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
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParamterers());
	}
	
	public ConfigNameResolverTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() throws CoreException {
		server = ServerCreationTestUtils.createServerWithRuntime(serverType, getClass().getName() + serverType);
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
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
				testConfig(var, result, ServerUtil.isJBoss7(server));
			else if( vars[i].equals(ConfigNameResolver.JBOSS_CONFIG_DIR))
				testConfigDir(var, result, ServerUtil.isJBoss7(server));
			else if( vars[i].equals(ConfigNameResolver.JBOSS_AS7_CONFIG_FILE)) 
				testAS7ConfigFile(var, result, ServerUtil.isJBoss7(server));
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
	private void testAS7ConfigFile(String var, String result, boolean isAS7 ) {
		if( isAS7 ) 
			assertEquals(result, "standalone.xml");
		else 
			assertEquals(result, "");
	}
	
}
