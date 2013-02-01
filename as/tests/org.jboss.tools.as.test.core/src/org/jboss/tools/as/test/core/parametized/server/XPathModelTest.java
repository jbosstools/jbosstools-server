package org.jboss.tools.as.test.core.parametized.server;

import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.test.util.JobUtils;
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
public class XPathModelTest extends TestCase {
	public static int serverCount = 0;
	static {
		JobUtils.waitForIdle(5000);
	}
	
	private String serverType;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParamterers());
	}
	@Before
	public void setUp() {
		JobUtils.waitForIdle();
	}
	
	 
	public XPathModelTest(String serverType) {
		this.serverType = serverType;
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			ASMatrixTests.cleanup();
		} catch(Exception ce ) {
			// ignore
		}
	}
	
	@Test
	public void serverTestImpl() {
		serverCount++;
		IServer server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, "server" + serverCount);
		File xpathFile = JBossServerCorePlugin.getServerStateLocation(server).append(IJBossToolingConstants.XPATH_FILE_NAME).toFile();
		int i = 0;
		boolean found = false;
		while(!found && i < 10) {
			i++;
			JobUtils.waitForIdle(500);
			found = xpathFile.exists();
		} 
		if( !found)
			fail("The XPath File has not been created. Xpaths will be lost on workspace restart");
	}

}
