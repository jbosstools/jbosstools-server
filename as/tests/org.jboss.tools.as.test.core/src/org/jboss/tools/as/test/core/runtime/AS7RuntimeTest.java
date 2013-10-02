/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.runtime;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.After;
import org.junit.Test;

/**
 * This test tests logic for calculating 
 * stored values on as7-style runtimes.
 * This does not need to be tested via a parametized suite
 * 
 */
public class AS7RuntimeTest extends TestCase {
	private String serverType = IJBossToolingConstants.SERVER_AS_71;
	 
	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	@Test
	public void testBaseDir() throws CoreException {
		IServer server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
		IRuntime rt = server.getRuntime();
		LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)rt.loadAdapter(LocalJBoss7ServerRuntime.class, null);
		IPath loc = rt.getLocation();
		String baseDir = jb7rt.getBaseDirectory();
		String assumedBaseDir = loc.append(IJBossRuntimeResourceConstants.AS7_STANDALONE).toFile().getAbsolutePath();
		assertEquals(baseDir, assumedBaseDir);
		
		IRuntimeWorkingCopy wc = rt.createWorkingCopy();
		((RuntimeWorkingCopy)wc).setAttribute(LocalJBoss7ServerRuntime.BASE_DIRECTORY, "standalone2");
		rt = wc.save(false, null);
		
		baseDir = jb7rt.getBaseDirectory();
		assumedBaseDir = loc.append("standalone2").toFile().getAbsolutePath();
		assertEquals(baseDir, assumedBaseDir);
		
		wc = rt.createWorkingCopy();
		((RuntimeWorkingCopy)wc).setAttribute(LocalJBoss7ServerRuntime.BASE_DIRECTORY, "/home/user/tmp/standalone");
		rt = wc.save(false, null);
		
		baseDir = jb7rt.getBaseDirectory();
		assumedBaseDir = new Path("/home/user/tmp/standalone").toFile().getAbsolutePath();
		assertEquals(baseDir, assumedBaseDir);
		
	}
}
