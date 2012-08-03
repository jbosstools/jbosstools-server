package org.jboss.tools.as.test.core.classpath;

import java.util.Collection;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.classpath.core.ejb3.EJB30SupportVerifier;
import org.jboss.ide.eclipse.as.classpath.core.ejb3.EJB3ClasspathContainer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
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
public class EJB3SupportVerifierTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParamterers());
	}
	
	private static HashMap<String, Boolean> expected; 
	static {
		expected = new HashMap<String, Boolean>();
		expected.put(IJBossToolingConstants.DEPLOY_ONLY_SERVER, false);
		expected.put(IJBossToolingConstants.SERVER_AS_32, false);
		expected.put(IJBossToolingConstants.SERVER_AS_40, false);
		expected.put(IJBossToolingConstants.SERVER_AS_42, true);
		expected.put(IJBossToolingConstants.SERVER_AS_50, true);
		expected.put(IJBossToolingConstants.SERVER_AS_51, true);
		expected.put(IJBossToolingConstants.SERVER_AS_60, true);
		expected.put(IJBossToolingConstants.SERVER_AS_70, true);
		expected.put(IJBossToolingConstants.SERVER_AS_71, true);
		expected.put(IJBossToolingConstants.SERVER_EAP_43, true);
		expected.put(IJBossToolingConstants.SERVER_EAP_50, true);
		expected.put(IJBossToolingConstants.SERVER_EAP_60, true);
	}
	 
	public EJB3SupportVerifierTest(String serverType) {
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
	public void testEJB30Support() {
		if( expected.get(serverType) == null )
			fail("Test needs to be updated for new server type");
		boolean supported = EJB30SupportVerifier.verify(server.getRuntime());
		assertEquals(new Boolean(supported), new Boolean(expected.get(serverType)));
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
