package org.jboss.tools.as.management.itests;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerServiceProxy;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils;
import org.jboss.tools.as.management.itests.utils.ParameterUtils;
import org.jboss.tools.as.management.itests.utils.StartupUtility;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(value = Parameterized.class)
public class AS7ManagementServiceStartupShutdownTest extends Assert {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> l = MatrixUtils.toMatrix(new Object[][]{
			ParameterUtils.getServerHomes()});
		return l;
	}
	
	private static StartupUtility util;
	@BeforeClass 
	public static void init() {
		util = new StartupUtility();
	}
	@AfterClass 
	public static void cleanup() {
		util.dispose();
	}
	
	private String homeDir;
	public AS7ManagementServiceStartupShutdownTest(String home) {
		homeDir = home;
		System.out.println(getClass().getName() + ":  " + homeDir);
	}

	@Before
	public void before() {
		assertNotNull(homeDir);
		assertTrue(new Path(homeDir).toFile().exists());
		String rtType = ParameterUtils.serverHomeToRuntimeType.get(homeDir);
		assertNotNull(rtType);
		IJBoss7ManagerService service = AS7ManagerTestUtils.findService(rtType);
		assertNotNull("Management Service for runtime type " + rtType + " not found.", service);
		assertTrue(service instanceof JBoss7ManagerServiceProxy);
		assertTrue(service instanceof ServiceTracker);
		Object o = ((ServiceTracker)service).getService();
		assertNotNull(o);

		String hd2 = util.getHomeDir();
		if( !homeDir.equals(hd2)) {
			util.setHomeDir(homeDir);
			util.start(true);
		}
	}
	
	@After
	public void after() {
		util.dispose();
	}
	
	@Test
	public void dummyTest() {
		// DO nothing. This class only tests startup 
		// and shutdown of the server, which are already
		// handled above. 
	}
}
