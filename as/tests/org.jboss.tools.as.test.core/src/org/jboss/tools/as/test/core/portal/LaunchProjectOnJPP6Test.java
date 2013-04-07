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
package org.jboss.tools.as.test.core.portal;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.PortalUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.as.test.core.TestConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;

public class LaunchProjectOnJPP6Test extends TestCase {

	private IServer server;
	private File location;
	private IJBossServerRuntime runtime;

	protected void setUp() throws Exception {
		server = ServerCreationTestUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_EAP_60, "server");
		IJBossServer jbossServer = ServerConverter.checkedGetJBossServer(server);
		runtime = jbossServer.getRuntime();
		location = new File(runtime.getRuntime().getLocation().toOSString());
		File gatein = new File(location, PortalUtil.GATEIN);
		gatein.mkdirs();
	}

	public void testLaunch() throws Exception {
		int type = PortalUtil.getServerPortalType(runtime);
		assertEquals(PortalUtil.TYPE_JPP6, type);
	}
	
	protected void tearDown() throws Exception {
		server.delete();
		runtime.getRuntime().delete();
		FileUtil.completeDelete(location);
	}
}
