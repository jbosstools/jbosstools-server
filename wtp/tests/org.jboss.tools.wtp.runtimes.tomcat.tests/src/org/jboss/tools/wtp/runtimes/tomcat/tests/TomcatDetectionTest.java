/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
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
 * @author Fred Bricon
 */
public class TomcatDetectionTest extends AbstractTomcatDetectionTest {

	private IRuntimeDetector tomcatDetector;

	@Before
	public void setUp() throws CoreException {
		super.setUp();
		tomcatDetector = RuntimeCoreActivator.getDefault().findRuntimeDetector("org.jboss.tools.wtp.runtimes.tomcat.TomcatRuntimeDetector");
		assertNotNull("Tomcat detected not found", tomcatDetector);
	}
	@After
	public void tearDown() throws CoreException {
		super.tearDown();
		tomcatDetector = null;
	}
	
	@Test
	public void testTomcatDetection() {
		RuntimeInitializerUtil.initializeRuntimesFromFolder(new File(REQUIREMENTS_DIR), new NullProgressMonitor());
		
		IRuntime[] runtimes = ServerCore.getRuntimes();
		assertEquals(2, runtimes.length);
		assertEquals(TOMCAT_6 +" Runtime", runtimes[0].getName());
		assertEquals(TOMCAT_7 +" Runtime", runtimes[1].getName());
		
		IServer[] servers = ServerCore.getServers();
		assertEquals(2, servers.length);
		assertEquals(TOMCAT_6, servers[0].getName());
		assertEquals(TOMCAT_7, servers[1].getName());
	}
}
