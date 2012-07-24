package org.jboss.tools.as.test.core.parametized.server;

import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.filesets.Fileset;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
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
public class CreateServerCheckDefaultsTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParamterers());
	}
	 
	public CreateServerCheckDefaultsTest(String serverType) {
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
		assertNotNull("Test setup failed to create a server", server);
		assertNotNull("Created server of type " + server.getServerType().getId() + " has no runtime", server.getRuntime());
		assertFalse("Server name should not be empty", server.getName() == null || "".equals(server.getName()));
		assertFalse("Runtime name should not be empty", server.getRuntime().getName() == null || "".equals(server.getRuntime().getName()));
		
		IJBossServerRuntime rt = getJBossRuntime(server);
		assertNotNull("Created server does not adapt to IJBossServerRuntime", rt);
		assertNotNull(rt.getVM());
		assertNotNull(rt.getExecutionEnvironment());
		
		LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)server.getRuntime().loadAdapter(LocalJBoss7ServerRuntime.class, null);
		if( jb7rt != null ) {
			assertEquals("standalone.xml", jb7rt.getConfigurationFile());
		} else {
			assertEquals("default", rt.getJBossConfiguration());
		}
	}
	
	@Test
	public void testXPathsAdded() {
		JobUtils.waitForIdle();
		IServer s = server;
		XPathCategory[] cats = XPathModel.getDefault().getCategories(s);
		assertFalse("Server has no xpath categories automatically added", cats == null || cats.length == 0);
		assertNotNull("Server has no 'ports' xpath category. ", XPathModel.getDefault().getCategory(s, "Ports"));
		File xpathFile = JBossServerCorePlugin.getServerStateLocation(s).append(IJBossToolingConstants.XPATH_FILE_NAME).toFile();
		try {
			assertTrue("The XPath File has not been created. Xpaths will be lost on workspace restart. " + xpathFile.getAbsolutePath(), xpathFile.exists());
		} catch( Error t) {
			t.printStackTrace();
			throw t;
		}
	}
	
	@Test
	public void testDefaultFilesetsAdded() {
		Fileset[] fs = FilesetUtil.loadFilesets(server);
		assertNotNull("No filesets created", fs);
		assertTrue("0 filesets added for server " + server.getName(), fs.length > 0);
	}
	
	private IJBossServerRuntime getJBossRuntime(IServer s) {
		IRuntime rt = s.getRuntime();
		return (IJBossServerRuntime) rt.loadAdapter(IJBossServerRuntime.class, null);
	}
}
