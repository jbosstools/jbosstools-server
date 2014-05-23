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

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.runtime.core.util.RuntimeInitializerUtil;
import org.jboss.tools.wtp.runtimes.tomcat.internal.detection.ServerUtils;
import org.junit.Test;


public class ServerUtilTest extends AbstractTomcatDetectionTest {

	@Test
	public void testUniqueServerName() {
		RuntimeInitializerUtil.initializeRuntimesFromFolder(new File(REQUIREMENTS_DIR), new NullProgressMonitor());
		assertEquals("Incorrect number of servers detected in requirements folder", 3, ServerCore.getServers().length);
		assertEquals("foo", ServerUtils.getUniqueServerName("foo"));
		assertEquals(TOMCAT_7+ " (2)", ServerUtils.getUniqueServerName(TOMCAT_7));
	}

}
