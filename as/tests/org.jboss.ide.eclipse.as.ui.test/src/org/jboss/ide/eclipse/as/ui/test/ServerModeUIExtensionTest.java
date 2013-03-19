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
package org.jboss.ide.eclipse.as.ui.test;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.jboss.ide.eclipse.as.ui.IBrowseBehavior;
import org.jboss.ide.eclipse.as.ui.IExploreBehavior;
import org.jboss.ide.eclipse.as.ui.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.ui.editor.EditorExtensionManager;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class ServerModeUIExtensionTest extends Assert {
	
	private String serverType;
	private String mode;
	
	@Parameters
	public static Collection<Object[]> data() {
		Object[] servers = ServerParameterUtils.getJBossServerTypeParamterers();
		String[] types = new String[]{"local", "rse"};
		Object[][] blocks = new Object[][]{servers, types};
		return MatrixUtils.toMatrix(blocks);
	}
	@Before
	public void setUp() {
		JobUtils.waitForIdle();
	}
	
	 
	public ServerModeUIExtensionTest(String serverType, String mode) {
		this.serverType = serverType;
		this.mode = mode;
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			ASMatrixTests.cleanup();
		} catch(Exception ce ) {
			// ignore
		}
	}
	@Test
	public void testLaunchTabProviders() {
		List<IJBossLaunchTabProvider> providers = EditorExtensionManager.getDefault().getLaunchTabProviders(mode, serverType);
		assertNotNull(providers);
		assertTrue(providers.size() > 0);
	}

	@Test
	public void testBrowseBehavior() {
		IBrowseBehavior beh = EditorExtensionManager.getDefault().getBrowseBehavior(mode);
		assertNotNull(beh);
	}

	@Test
	public void testExploreBehavior() {
		IExploreBehavior beh = EditorExtensionManager.getDefault().getExploreBehavior(mode);
		assertNotNull(beh);
	}

}
