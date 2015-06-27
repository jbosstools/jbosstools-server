package org.jboss.tools.as.management.itests;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerServiceProxy;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils;
import org.jboss.tools.as.management.itests.utils.ParameterUtils;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(value = Parameterized.class)
public class AS7ManagementServicePreReqTest extends Assert {

	@Parameters
	public static Collection<Object[]> data() {
		ArrayList<Object[]> l = MatrixUtils.toMatrix(new Object[][]{ParameterUtils.getAS7ServerHomes()});
		return l;
	}
	
	private String homeDir;
	public AS7ManagementServicePreReqTest(String home) {
		homeDir = home;
	}
	@Test 
	public void verifyCorrectService() {
		assertNotNull(homeDir);
		assertTrue(homeDir + " does not exist.", new Path(homeDir).toFile().exists());
		String rtType = ParameterUtils.serverHomeToRuntimeType.get(homeDir);
		assertNotNull("runtime type for homedir " + homeDir + " is null", rtType);
		
		IJBoss7ManagerService service = AS7ManagerTestUtils.findService(rtType);
		assertNotNull("Management Service for runtime type " + rtType + " not found.", service);
		
		IJBoss7ManagerService service2 = JBoss7ManagerUtil.getService(rtType);
		assertNotNull("Management Service for runtime type " + rtType + " not found.", service2);
		
		assertTrue(service2 instanceof JBoss7ManagerServiceProxy);
		assertTrue(service2 instanceof ServiceTracker);
		Object o2 = ((ServiceTracker)service).getService();
		
		assertTrue(service instanceof JBoss7ManagerServiceProxy);
		assertTrue(service instanceof ServiceTracker);
		Object o = ((ServiceTracker)service).getService();

		assertNotNull("Service not found for runtime type " + rtType, o2);
		assertNotNull("Service not found for runtime type " + rtType, o);
	}
}
