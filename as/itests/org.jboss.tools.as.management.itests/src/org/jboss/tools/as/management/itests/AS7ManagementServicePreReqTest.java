/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.management.itests;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
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

import org.junit.Assert;

@RunWith(value = Parameterized.class)
public class AS7ManagementServicePreReqTest extends Assert {

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> l = MatrixUtils.toMatrix(new Object[][]{
			ParameterUtils.getServerHomes()});
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
		String serverType = ParameterUtils.getServerType(homeDir);
		assertNotNull("runtime type for homedir " + homeDir + " is null", serverType);
		
		IServerType st = ServerCore.findServerType(serverType);
		assertNotNull("server type for homedir " + homeDir + " is null", st);
		
		IRuntimeType rtt = st.getRuntimeType();
		assertNotNull("runtime type for homedir " + homeDir + " is null", rtt);
		
		String rtType = rtt.getId();
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
