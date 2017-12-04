package org.jboss.tools.as.management.itests;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
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
public class AS7ManagementServiceResolutionTest extends Assert {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> l = MatrixUtils.toMatrix(new Object[][]{IJBoss7ManagerService.ALL_SERVICE_VERSIONS});
		return l;
	}
	
	private String serviceId;
	public AS7ManagementServiceResolutionTest(String serviceId) {
		this.serviceId = serviceId;
	}
	@Test 
	public void verifyCorrectService() {
		assertNotNull(serviceId);
		IJBoss7ManagerService service = JBoss7ManagerUtil.getManagerService(serviceId);
		assertNotNull("Management Service with version " + serviceId + " not found.", service);
		
		assertTrue(service instanceof JBoss7ManagerServiceProxy);
		assertTrue(service instanceof ServiceTracker);
		Object o = ((ServiceTracker)service).getService();

		assertNotNull("Service not found for version " + serviceId, o);
	}
}
