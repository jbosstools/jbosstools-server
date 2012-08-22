package org.jboss.tools.as.test.core.parametized.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.webtools.filesets.Fileset;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
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
			String s = props.getWelcomePageUrl();
			try {
				URL url = new URL(s);
			} catch(MalformedURLException murle) {
				fail(serverType + " has an invalid welcome page url: " + murle.getMessage());
			}
		}
	}
	
	@Test
	public void testXPathsAdded() {
		JobUtils.waitForIdle();
		IServer s = server;
		XPathCategory[] cats = XPathModel.getDefault().getCategories(s);
		String typeId = server.getServerType().getId();
		if( typeId.equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			assertTrue("Deploy-only Servers should have no default xpaths.", cats == null || cats.length == 0);
			return;
		}
		assertFalse("Server has no xpath categories automatically added", cats == null || cats.length == 0);
		assertNotNull("Server has no 'ports' xpath category. ", XPathModel.getDefault().getCategory(s, XPathModel.PORTS_CATEGORY_NAME));
		File xpathFile = JBossServerCorePlugin.getServerStateLocation(s).append(IJBossToolingConstants.XPATH_FILE_NAME).toFile();
		try {
			assertTrue("The XPath File has not been created. Xpaths will be lost on workspace restart. " + xpathFile.getAbsolutePath(), xpathFile.exists());
		} catch( Error t) {
			t.printStackTrace();
			throw t;
		}
		
		// Take this chance to test some xpath model
		// Can maybe be moved out to their own test
		assertNull(XPathModel.getDefault().getQuery(null, null));
		assertNull(XPathModel.getDefault().getQuery(s, null));
		assertNull(XPathModel.getDefault().getQuery(s, new Path(XPathModel.PORTS_CATEGORY_NAME)));
		XPathCategory cat = XPathModel.getDefault().getCategory(s, XPathModel.PORTS_CATEGORY_NAME);
		XPathQuery[] qs = cat.getQueries();
		assertFalse(qs == null || qs.length == 0);
		String name = qs[0].getName();
		assertNotNull(name);
		XPathQuery q = XPathModel.getDefault().getQuery(s, new Path(XPathModel.PORTS_CATEGORY_NAME).append(name));
		assertNotNull(q);
		q = XPathModel.getDefault().getQuery(s, new Path("GaRbAgE").append(name));
		assertNull(q);
		assertTrue(XPathModel.getDefault().containsCategory(s, XPathModel.PORTS_CATEGORY_NAME));
		assertFalse(XPathModel.getDefault().containsCategory(s, "GaRbAgE"));
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
