package org.jboss.tools.as.test.core.subsystems;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel.SubsystemType;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.jboss.tools.as.test.core.subsystems.ServerSubsystemTest1.ModelSubclass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class DeploymentSettingsControllerTest extends TestCase {
	private static final String SYSTEM = "deploy.settings.rootpath";
	private static final String LOCAL_SUBSYSTEM = "deploy.settings.rootpath.local";
	private static final String RSE_SUBSYSTEM = "deploy.settings.rootpath.remote";
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParamterers());
	}
	 
	public DeploymentSettingsControllerTest(String serverType) {
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
	public void testResolution() throws Exception {
		ModelSubclass c = new ModelSubclass();
		String system = SYSTEM;
		
		SubsystemType[] types = c.getSubsystemTypes(serverType, system);
		assertTrue(types != null );
		assertTrue(types.length == 2);
		
		boolean foundLocal, foundRemote;
		foundLocal = foundRemote = false;
		for( int j = 0; j < types.length; j++ ) {
			ISubsystemController controller = null;
			try {
				controller = c.createControllerForSubsystem(server, system, types[j].getId());
			}catch(CoreException ce) {
				fail("Error creating controller for " + system + ", " + serverType + ", " + types[j].getId());
			}
			assertNotNull(controller);
			assertTrue(controller.getSystemId().equals(system));
			assertTrue(controller instanceof IDeploymentOptionsController);
			if( controller.getSubsystemId().equals(LOCAL_SUBSYSTEM))
				foundLocal = true;
			if( controller.getSubsystemId().equals(RSE_SUBSYSTEM))
				foundRemote = true;
		}
		if( !foundLocal || !foundRemote )
			fail("Must find both local and remote implementations for servertype " + serverType);
	}
	
	@Test
	public void testResolutionWithProperty() throws Exception {
		ModelSubclass c = new ModelSubclass();
		String system = SYSTEM;
		
		ISubsystemController controller = c.createSubsystemController(server, system,
				new ControllerEnvironment().addRequiredProperty(system, "target", "rse").getMap());
		assertNotNull(controller);
		assertEquals(controller.getSystemId(),system);
		assertTrue(controller instanceof IDeploymentOptionsController);
		assertEquals(controller.getSubsystemId(), RSE_SUBSYSTEM);
		assertEquals(controller.getClass().getSimpleName(),"RSEDeploymentOptionsController");
		

		controller = c.createSubsystemController(server, system,
				new ControllerEnvironment().addRequiredProperty(system, "target", "local").getMap());
		assertNotNull(controller);
		assertEquals(controller.getSystemId(),system);
		assertTrue(controller instanceof IDeploymentOptionsController);
		assertEquals(controller.getSubsystemId(), LOCAL_SUBSYSTEM);
		assertEquals(controller.getClass().getSimpleName(), ("LocalDeploymentOptionsController"));

	}
	
	@Test
	public void testGetAndSetZip() throws Exception {
		IDeploymentOptionsController controller = createController("rse", null);
		assertFalse(controller.prefersZippedDeployments());
		try {
			controller.setPrefersZippedDeployments(true);
			fail();
		} catch(IllegalStateException ise) {}
		
		IServerWorkingCopy wc = server.createWorkingCopy();
		controller = createController("rse", wc);
		assertFalse(controller.prefersZippedDeployments());
		controller.setPrefersZippedDeployments(true);
		assertTrue(wc.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false) == true);
		assertTrue(server.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false) == false);
		server = wc.save(false, new NullProgressMonitor() );
		assertTrue(server.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false) == true);

		
		controller = createController("local", null);
		assertTrue(controller.prefersZippedDeployments());
		try {
			controller.setPrefersZippedDeployments(false);
			fail();
		} catch(IllegalStateException ise) {}
		
		wc = server.createWorkingCopy();
		controller = createController("local", wc);
		assertTrue(controller.prefersZippedDeployments());
		controller.setPrefersZippedDeployments(false);
		assertTrue(wc.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false) == false);
		assertTrue(server.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false) == true);
		server = wc.save(false, new NullProgressMonitor() );
		assertTrue(server.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false) == false);
	}
	
	private void testLocalDeploymentServer_deployonly(String expectedDir, String expectedTmp) throws Exception {
		testLocalDeploymentServer_deployonly(expectedDir, expectedDir, expectedTmp, expectedTmp);
	}
	
	private void testLocalDeploymentServer_deployonly(String expectedDir, String expectedRelDir,
			String expectedTmp, String expectedRelTmp) throws Exception {
		testDeploymentServer_deployonly(expectedDir, expectedRelDir, expectedTmp, expectedRelTmp, "local", null);
	}
	
	//Only to be used when forcing a sep char
	private void testDeploymentServer_deployonly(String expectedDir, String expectedRelDir,
			String expectedTmp, String expectedRelTmp, String controllerFlag, boolean win) throws Exception {
		Character c = win ? '\\' : '/';
		testDeploymentServer_deployonly(expectedDir, expectedRelDir, expectedTmp, expectedRelTmp, controllerFlag, c);
	}
	
	private void testDeploymentServer_deployonly(String expectedDir, String expectedRelDir,
			String expectedTmp, String expectedRelTmp, String controllerFlag, Character sep) throws Exception {
		//The deploy only server is very different and must be tested separately
		// It will not accept any other deploy-location-types other than custom
		
		IDeploymentOptionsController c = createController(controllerFlag,null, sep);
		assertEquals(c.getCurrentDeploymentLocationType(), IDeployableServer.DEPLOY_CUSTOM);
		// verify the setter fails
		try {
			c.setCurrentDeploymentLocationType(IDeployableServer.DEPLOY_METADATA);
			fail();
		} catch(IllegalStateException ise) {
		}
		assertEquals(c.getCurrentDeploymentLocationType(), IDeployableServer.DEPLOY_CUSTOM);
		
		String sAbsolute = c.getDeploymentsRootFolder(true);
		String sRelative = c.getDeploymentsRootFolder(false);
		assertEquals(sAbsolute, expectedDir);
		assertEquals(sRelative, expectedRelDir); // deploy-only has nothing to be relative to. 

		String tAbsolute = c.getDeploymentsTemporaryFolder(true);
		String tRelative = c.getDeploymentsTemporaryFolder(false);
		assertEquals(tAbsolute, expectedTmp);
		assertEquals(tRelative, expectedRelTmp); // deploy-only has nothing to be relative to. 
	}
	
	private void initServer(String dirType, String dir, String tmp) throws Exception {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, dirType);
		wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, dir);
		wc.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, tmp);
		server = wc.save(false, null);
	}
	
	private IDeploymentOptionsController verifyDepType(String controllerFlag, String type, String otherType ) throws Exception {

		IDeploymentOptionsController c = createController(controllerFlag,null);
		assertEquals(c.getCurrentDeploymentLocationType(), type);
		
		// verify the setter fails
		try {
			c.setCurrentDeploymentLocationType(otherType);
			fail();
		} catch(IllegalStateException ise) {
		}
		assertEquals(c.getCurrentDeploymentLocationType(),type);
		return c;
	}
	
	@Test
	public void testLocalDeployment_Server() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "/home/user/d/deploy";
		String depdirTmp = "/home/user/d/tmp";
		testLocalDeployment_Server_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_SERVER);
	}
	@Test
	public void testLocalDeployment_Server_relative() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d/deploy";
		String depdirTmp = "d/tmp";
		testLocalDeployment_Server_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_SERVER);
	}

	public void testLocalDeployment_Server_internal(String depdir, String depdirTmp, String initialMode) throws Exception {
		initServer(initialMode, depdir, depdirTmp);
		if( server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			// Deploy only server has no sense of relative paths
			String depdir2 = new Path(depdir).makeAbsolute().toOSString();
			String depdirTmp2 = new Path(depdirTmp).makeAbsolute().toOSString();
			testLocalDeploymentServer_deployonly(depdir2, depdirTmp2);
			return;
		}
			
		IDeploymentOptionsController c = verifyDepType("local", initialMode, IDeployableServer.DEPLOY_CUSTOM); 

		String sAbsolute = c.getDeploymentsRootFolder(true);
		String sRelative = c.getDeploymentsRootFolder(false);
		String tAbsolute = c.getDeploymentsTemporaryFolder(true);
		String tRelative = c.getDeploymentsTemporaryFolder(false);

		JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, new NullProgressMonitor());
		int structure = props.getFileStructure();
		if( structure == JBossExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
			// as7
			IPath home = server.getRuntime().getLocation();
			IPath depTail = new Path("standalone").append("deployments");
			IPath tmpTail = new Path("standalone").append("tmp");
			assertEquals(home.append(depTail).toOSString(), sAbsolute);
			assertEquals(depTail.toOSString(), sRelative);
			assertEquals(home.append(tmpTail).toOSString(), tAbsolute);
			assertEquals(tmpTail.toOSString(), tRelative);
		} else {
			// as<7
			IPath home = server.getRuntime().getLocation();
			IPath depTail = new Path("server").append("default").append("deploy");
			IPath tmpTail = new Path("server").append("default").append("tmp").append("jbosstoolsTemp");
			assertEquals(home.append(depTail).toOSString(), sAbsolute);
			assertEquals(depTail.toOSString(), sRelative);
			assertEquals(home.append(tmpTail).toOSString(), tAbsolute);
			assertEquals(tmpTail.toOSString(), tRelative);
		}
	}

	@Test
	public void testLocalDeployment_Metadata() throws Exception {
		// Server is deploying to metadata deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "/home/user/d/deploy";
		String depdirTmp = "/home/user/d/tmp";
		testLocalDeployment_Metadata_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_METADATA);
	}
	@Test
	public void testLocalDeployment_Metadata_relative() throws Exception {
		// Server is deploying to metadata deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d/deploy";
		String depdirTmp = "d/tmp";
		testLocalDeployment_Metadata_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_METADATA);
	}
	
	public void testLocalDeployment_Metadata_internal(String depdir, String depdirTmp, String initialMode) throws Exception {
		initServer(initialMode, depdir, depdirTmp);
		if( server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			// Deploy only server has no sense of relative paths
			String depdir2 = new Path(depdir).makeAbsolute().toOSString();
			String depdirTmp2 = new Path(depdirTmp).makeAbsolute().toOSString();
			testLocalDeploymentServer_deployonly(depdir2, depdirTmp2);
			return;
		}
			
		IDeploymentOptionsController c = verifyDepType("local", initialMode, IDeployableServer.DEPLOY_CUSTOM); 

		String sAbsolute = c.getDeploymentsRootFolder(true);
		String sRelative = c.getDeploymentsRootFolder(false);
		String tAbsolute = c.getDeploymentsTemporaryFolder(true);
		String tRelative = c.getDeploymentsTemporaryFolder(false);
		IPath stateLocation = JBossServerCorePlugin.getServerStateLocation(server);
		assertTrue(stateLocation.isPrefixOf(new Path(sAbsolute)));
		assertTrue(stateLocation.isPrefixOf(new Path(sRelative)));
		assertTrue(stateLocation.isPrefixOf(new Path(tAbsolute)));
		assertTrue(stateLocation.isPrefixOf(new Path(tRelative)));
	}

	
	
	@Test
	public void testLocalDeployment_Custom() throws Exception {
		// Server is deploying to metadata deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "/home/user/d/deploy";
		String depdirTmp = "/home/user/d/tmp";
		initServer(IDeployableServer.DEPLOY_CUSTOM, depdir, depdirTmp);
		testLocalDeploymentServer_deployonly(depdir, depdirTmp);
	}
	@Test
	public void testLocalDeployment_custom_relative() throws Exception {
		// Server is deploying to metadata deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d/deploy";
		String depdirTmp = "d/tmp";
		initServer(IDeployableServer.DEPLOY_CUSTOM, depdir, depdirTmp);
		IPath serverHome = (server.getRuntime() == null ? null : server.getRuntime().getLocation());
		if( serverHome == null ) { 
			// Deploy only server has no sense of relative paths
			String depdir2 = new Path(depdir).makeAbsolute().toOSString();
			String depdirTmp2 = new Path(depdirTmp).makeAbsolute().toOSString();
			testLocalDeploymentServer_deployonly(depdir2, depdirTmp2);
		} else {
			testLocalDeploymentServer_deployonly(serverHome.append(depdir).toOSString(), depdir, 
					serverHome.append(depdirTmp).toOSString(),depdirTmp);
		}
	}

	
	/*
	 * Remote tests begin below here
	 */
	
	
	@Test
	public void testRemoteDeployment_Server() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "/home/user/d/deploy";
		String depdirTmp = "/home/user/d/tmp";
		initServer(IDeployableServer.DEPLOY_SERVER, depdir, depdirTmp);
		initRSE(false);
		testRemoteDeployment_Server_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_SERVER, false);
	}
	@Test
	public void testRemoteDeployment_Server_relative() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d/deploy";
		String depdirTmp = "d/tmp";
		initServer(IDeployableServer.DEPLOY_SERVER, depdir, depdirTmp);
		initRSE(false);
		testRemoteDeployment_Server_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_SERVER, false);
	}

	@Test
	public void testRemoteWindowsDeployment_Server() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "C:\\home\\user\\d\\deploy";
		String depdirTmp = "C:\\home\\user\\d\\tmp";
		initServer(IDeployableServer.DEPLOY_SERVER, depdir, depdirTmp);
		initRSE(true);
		testRemoteDeployment_Server_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_SERVER, true);
	}
	@Test
	public void testRemoteWindowsDeployment_Server_relative() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d\\deploy";
		String depdirTmp = "d\\tmp";
		initServer(IDeployableServer.DEPLOY_SERVER, depdir, depdirTmp);
		initRSE(true);
		testRemoteDeployment_Server_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_SERVER, true);
	}
	
	private static final String RSE_TEST_WIN_SERVER_HOME = "C:\\path\\to\\jboss\\windows";
	private static final String RSE_TEST_LINUX_SERVER_HOME = "/path/to/linux/jboss";
	
	private void initRSE(boolean windows) throws Exception {
		JBossExtendedProperties jep = (JBossExtendedProperties) server.loadAdapter(JBossExtendedProperties.class, null);
		String home, basedir, config;
		home = basedir = config = null;
		if( windows) {
			home = RSE_TEST_WIN_SERVER_HOME;
		} else {
			home = RSE_TEST_LINUX_SERVER_HOME;
		}
		if( jep != null && jep.getFileStructure() == JBossExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY ) {
			config = "default2";
		} else {
			config = "standalone2.xml";
			basedir = "standalone2";
		}
		initRSE(home, basedir, config);
	}
	private void initRSE(String home, String basedir, String config) throws Exception {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(RSEUtils.RSE_BASE_DIR, basedir);
		wc.setAttribute(RSEUtils.RSE_SERVER_HOME_DIR, home);
		wc.setAttribute(RSEUtils.RSE_SERVER_CONFIG, config);
		server = wc.save(false, null);
	}
	
	public void testRemoteDeployment_Server_internal(String depdir, String depdirTmp, String initialMode, boolean windows) throws Exception {
		char sep = windows ? '\\' : '/';
		if( server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			// deploy-only server will respect the hard-codedvals for depdir 
			// Does not understand server-relative, has no runtime, etc
			// REMOTE does not use the temporary deploy folder on a remote system, so it returns the actual deploy dir
			String depdir2 = new RemotePath(depdir,sep).makeAbsolute().toOSString();
			testDeploymentServer_deployonly(depdir2, depdir2, depdir2, depdir2, "rse", windows);
			return;
		}
		
		IDeploymentOptionsController c = createController("rse",null, sep);

		String sAbsolute = c.getDeploymentsRootFolder(true);
		String sRelative = c.getDeploymentsRootFolder(false);
		String tAbsolute = c.getDeploymentsTemporaryFolder(true);
		String tRelative = c.getDeploymentsTemporaryFolder(false);
		String serverHome = windows ? RSE_TEST_WIN_SERVER_HOME: RSE_TEST_LINUX_SERVER_HOME;
		JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, new NullProgressMonitor());
		int structure = props.getFileStructure();
		if( structure == JBossExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
			// as7/wf
			IPath tail = new RemotePath("standalone2", sep).append("deployments").makeRelative();
			String deployAbs = new RemotePath(serverHome,sep).append(tail).toOSString();
			assertEquals(sAbsolute, deployAbs);
			assertEquals(sRelative, tail.toOSString());
			assertEquals(tAbsolute, deployAbs);
			assertEquals(tRelative, tail.toOSString());
		} else {
			// as < 7
			IPath tail = new RemotePath("server",sep).append("default2").append("deploy").makeRelative();
			String deployAbs = new RemotePath(serverHome,sep).append(tail).toOSString();
			assertEquals(sAbsolute, deployAbs);
			assertEquals(sRelative, tail.toOSString());
			assertEquals(tAbsolute, deployAbs);
			assertEquals(tRelative, tail.toOSString());
		}
	}


	
	// metadata tests, also invalid for remote. All servers should default to server-based deployments
	@Test
	public void testRemoteDeployment_Metadata() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "/home/user/d/deploy";
		String depdirTmp = "/home/user/d/tmp";
		initServer(IDeployableServer.DEPLOY_METADATA, depdir, depdirTmp);
		initRSE(false);
		testRemoteDeployment_Server_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_METADATA, false);
	}
	@Test
	public void testRemoteDeployment_Metadata_relative() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d/deploy";
		String depdirTmp = "d/tmp";
		initServer(IDeployableServer.DEPLOY_METADATA, depdir, depdirTmp);
		initRSE(false);
		testRemoteDeployment_Server_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_METADATA, false);
	}

	@Test
	public void testRemoteWindowsDeployment_Metadata() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "C:\\home\\user\\d\\deploy";
		String depdirTmp = "C:\\home\\user\\d\\tmp";
		initServer(IDeployableServer.DEPLOY_METADATA, depdir, depdirTmp);
		initRSE(true);
		testRemoteDeployment_Server_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_METADATA, true);
	}
	@Test
	public void testRemoteWindowsDeployment_Metadata_relative() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d\\deploy";
		String depdirTmp = "d\\tmp";
		initServer(IDeployableServer.DEPLOY_METADATA, depdir, depdirTmp);
		initRSE(true);
		testRemoteDeployment_Server_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_METADATA, true);
	}

	
	// Custom
	@Test
	public void testRemoteDeployment_Custom() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "/home/user/d/deploy";
		String depdirTmp = "/home/user/d/tmp";
		initServer(IDeployableServer.DEPLOY_CUSTOM, depdir, depdirTmp);
		initRSE(false);
		testRemoteDeployment_Custom_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_CUSTOM, false);
	}
	@Test
	public void testRemoteDeployment_Custom_relative() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d/deploy";
		String depdirTmp = "d/tmp";
		initServer(IDeployableServer.DEPLOY_CUSTOM, depdir, depdirTmp);
		initRSE(false);
		testRemoteDeployment_Custom_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_CUSTOM, false);
	}

	@Test
	public void testRemoteWindowsDeployment_Custom() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "C:\\home\\user\\d\\deploy";
		String depdirTmp = "C:\\home\\user\\d\\tmp";
		initServer(IDeployableServer.DEPLOY_CUSTOM, depdir, depdirTmp);
		initRSE(true);
		testRemoteDeployment_Custom_internal(depdir, depdirTmp,  IDeployableServer.DEPLOY_CUSTOM, true);
	}
	@Test
	public void testRemoteWindowsDeployment_Custom_relative() throws Exception {
		// Server is deploying to server's standard deploy location, so 
		// depdir and depdirTmp are 100% ignored
		String depdir = "d\\deploy";
		String depdirTmp = "d\\tmp";
		initServer(IDeployableServer.DEPLOY_CUSTOM, depdir, depdirTmp);
		initRSE(true);
		testRemoteDeployment_Custom_internal(depdir, depdirTmp, IDeployableServer.DEPLOY_CUSTOM, true);
	}

	public void testRemoteDeployment_Custom_internal(String depdir, String depdirTmp, String initialMode, boolean windows) throws Exception {
		char sep = windows ? '\\' : '/';
		if( server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			// deploy-only server will respect the hard-codedvals for depdir 
			// Does not understand server-relative, has no runtime, etc
			// REMOTE does not use the temporary deploy folder on a remote system, so it returns the actual deploy dir
			String depdir2 = new RemotePath(depdir,sep).makeAbsolute().toOSString();
			testDeploymentServer_deployonly(depdir2, depdir2, depdir2, depdir2, "rse", windows);
			return;
		}
		
		IDeploymentOptionsController c = createController("rse",null, sep);

		String sAbsolute = c.getDeploymentsRootFolder(true);
		String sRelative = c.getDeploymentsRootFolder(false);
		String tAbsolute = c.getDeploymentsTemporaryFolder(true);
		String tRelative = c.getDeploymentsTemporaryFolder(false);
		boolean storedAbsolute = new RemotePath(depdir, sep).isAbsolute();
		if( storedAbsolute) {
			// tmpdir is ignored, all should match the absolute custom folder
			assertEquals(sAbsolute, new RemotePath(depdir, sep).toOSString());
			assertEquals(sRelative, new RemotePath(depdir, sep).toOSString());
			assertEquals(tRelative, new RemotePath(depdir, sep).toOSString());
			assertEquals(tAbsolute, new RemotePath(depdir, sep).toOSString());
		} else {
			assertEquals(sRelative, new RemotePath(depdir, sep).toOSString());
			assertEquals(tRelative, new RemotePath(depdir, sep).toOSString());
			String serverHome = windows ? RSE_TEST_WIN_SERVER_HOME: RSE_TEST_LINUX_SERVER_HOME;
			assertEquals(sAbsolute, new RemotePath(new RemotePath(serverHome, sep).append(depdir).toString(), sep).toOSString());
			assertEquals(tAbsolute, new RemotePath(new RemotePath(serverHome, sep).append(depdir).toString(), sep).toOSString());
		}
	}	
	
	// TODO   remote custom (absolute, relative)
	
	
	
	private IDeploymentOptionsController createController(String targetVal, IServerWorkingCopy wc) throws Exception {
		return createController(targetVal, wc, null);
	}
	private IDeploymentOptionsController createController(String targetVal, IServerWorkingCopy wc, Character cha) throws Exception {
		ModelSubclass c = new ModelSubclass();
		ControllerEnvironment env = new ControllerEnvironment().addRequiredProperty(SYSTEM, "target", targetVal);
		env.addProperty(IDeploymentOptionsController.ENV_TARGET_OS_SEPARATOR, cha);
		
		IServerAttributes toUse = (wc == null ? server : wc);
		IDeploymentOptionsController controller = (IDeploymentOptionsController)c.createSubsystemController(toUse, SYSTEM,
				env.getMap());
		return controller;
	}
	
}