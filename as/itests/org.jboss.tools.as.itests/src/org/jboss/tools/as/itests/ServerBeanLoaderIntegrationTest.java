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
package org.jboss.tools.as.itests;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.TestConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.jboss.tools.as.test.core.parametized.server.ServerBeanLoader3Test;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
/**
 * This test will create run the server bean loader on all runtimes that are marked as jboss runtimes, 
 * which are all astools runtime types currently. If a type is found which does not have a cmd line arg, 
 * that test will fail.  It will verify that the proper type and version for serverbean is found
 * for the given server home. 
 * 
 *  It will also make MOCK structures for all server types, including 'subtypes' like 
 *  jpp, gatein, etc. This will help verify the mocks are created with the minimal requirements
 *  that the server bean loader looks for. 
 * 
 */
@RunWith(value = Parameterized.class)
public class ServerBeanLoaderIntegrationTest extends ServerBeanLoader3Test {
	@Parameters
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParametersPlusAdditionalMocks());
	 }

	public ServerBeanLoaderIntegrationTest(String serverType) {
		super(serverType);
	}
	
	@Before
	public void setUp() {
		super.setUp();
	}
	
	/*
	 * Test the server bean loader using runtime home flags passed in to the build
	 */
	@Test
	public void testServerBeanLoaderFromRuntimes() {
		String fLoc = TestConstants.getServerHome(serverType);
		if( fLoc == null ) {
			if( Arrays.asList(IJBossToolingConstants.ALL_JBOSS_SERVERS).contains(serverType)) {
				// This type should be tested but has no server-home, so fail
				fail("Test Suite has no server home for server type " + serverType);
			}
			// IF we're not one of the standard types, we're a test type (jpp, gate-in, etc), and this isn't a fail
			return;
		}
		Data p = expected.get(serverType);
		inner_testServerBeanLoaderForFolder(new File(fLoc), p.type, p.version, p.overrideId);
		if( p.type.equals(JBossServerType.EAP_STD)) {
			inner_testServerBeanLoaderForFolder(new File(fLoc).getParentFile(), JBossServerType.EAP, p.version, p.overrideId);
		}
	}
}
