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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.as.test.core.internal.utils.JREUtils;
import org.jboss.tools.runtime.core.util.RuntimeInitializerUtil;
import org.jboss.tools.wtp.runtimes.tomcat.internal.detection.ServerUtils;
import org.junit.Test;


public class ServerUtilTest extends AbstractTomcatDetectionTest {

	@Test
	public void testUniqueServerName() {
		assertNotNull(TomcatDetectionTest.JRE_7_HOME);
		assertTrue("JRE7 home " + TomcatDetectionTest.JRE_7_HOME + " does not exist", new File(TomcatDetectionTest.JRE_7_HOME).exists());
		IVMInstall foundOrCreated = JREUtils.findOrCreateJRE(new Path(TomcatDetectionTest.JRE_7_HOME));
		assertNotNull(foundOrCreated);
		
		
		RuntimeInitializerUtil.initializeRuntimesFromFolder(new File(REQUIREMENTS_DIR), new NullProgressMonitor());
		IServer[] servers = ServerCore.getServers();
		assertEquals("foo", ServerUtils.getUniqueServerName("foo")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(TOMCAT_6 + " (2)", ServerUtils.getUniqueServerName(TOMCAT_6)); //$NON-NLS-1$
		assertEquals(TOMCAT_7 + " (2)", ServerUtils.getUniqueServerName(TOMCAT_7)); //$NON-NLS-1$
		assertEquals(TOMCAT_8 + " (2)", ServerUtils.getUniqueServerName(TOMCAT_8)); //$NON-NLS-1$
    	assertEquals(UNEXPECTED_RUNTIME_COUNT_ERROR + " : " + toString(servers), 3, servers.length); //$NON-NLS-1$
	}
}
