/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.wtp.runtimes.tomcat.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.IRuntimeDetector;
import org.jboss.tools.runtime.core.util.RuntimeInitializerUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration Test of Tomcat Detection
 * 
 * @author Fred Bricon, Nick Boldt
 */
public class TomcatDetectionTest extends AbstractTomcatDetectionTest {

	private IRuntimeDetector tomcatDetector;

	@Override
  @Before
	public void setUp() throws CoreException {
		super.setUp();
		tomcatDetector = RuntimeCoreActivator.getDefault().findRuntimeDetector("org.jboss.tools.wtp.runtimes.tomcat.TomcatRuntimeDetector");
		assertNotNull("Tomcat detector org.jboss.tools.wtp.runtimes.tomcat.TomcatRuntimeDetector not found", tomcatDetector);
	}
	@Override
  @After
	public void tearDown() throws CoreException {
		super.tearDown();
		tomcatDetector = null;
	}
	
	@Test
	public void testTomcatDetection() {
		RuntimeInitializerUtil.initializeRuntimesFromFolder(new File(REQUIREMENTS_DIR), new NullProgressMonitor());
		
		IRuntime[] runtimes = ServerCore.getRuntimes();
		Map<String, IRuntime> runtimeMap = new HashMap<String, IRuntime>(); 

		for (IRuntime iRuntime : runtimes) {
			runtimeMap.put(iRuntime.getName(),iRuntime);
		}
		
		assertNotNull(runtimeMap.get(TOMCAT_6 +" Runtime"));
		assertNotNull(runtimeMap.get(TOMCAT_7 +" Runtime"));
		assertNotNull(runtimeMap.get(TOMCAT_8 +" Runtime"));
		assertEquals(UNEXPECTED_RUNTIME_COUNT_ERROR + ": " + toString(runtimes), 3, runtimes.length);

		IServer[] servers = ServerCore.getServers();
		Map<String, IServer> serverMap = new HashMap<String, IServer>();
		
		for (IServer iServer : servers) {
			serverMap.put(iServer.getName(), iServer);
		}
		
		assertNotNull(serverMap.get(TOMCAT_6));
		assertNotNull(serverMap.get(TOMCAT_7));
		assertNotNull(serverMap.get(TOMCAT_8));
		assertEquals(3, servers.length);
	}
	
	@Test
	public void testTomcat8() throws Exception {
		String tomcat8Id = "org.eclipse.jst.server.tomcat.runtime.80";
		IRuntimeType runtimeType = ServerCore.findRuntimeType(tomcat8Id );
		assertNotNull(tomcat8Id + " isn't a runtime type", runtimeType);
		String absolutePath = new File(TOMCAT_8_PATH).getAbsolutePath();
		IRuntimeWorkingCopy runtimeWc = null; 
		IProgressMonitor monitor = new NullProgressMonitor();
		String id = absolutePath.replace(File.separatorChar,'_').replace(':','-');
		runtimeWc = runtimeType.createRuntime(id, monitor );
		runtimeWc.setName("Special Tomcat 8");
		runtimeWc.setLocation(new Path(absolutePath));
		ITomcatRuntimeWorkingCopy wc = (ITomcatRuntimeWorkingCopy) runtimeWc.loadAdapter(ITomcatRuntimeWorkingCopy.class, null);
		wc.setVMInstall(JavaRuntime.getDefaultVMInstall());
		IStatus status = runtimeWc.validate(monitor);
		assertTrue(status.getMessage(), status.getSeverity() != IStatus.ERROR);
	}
}
