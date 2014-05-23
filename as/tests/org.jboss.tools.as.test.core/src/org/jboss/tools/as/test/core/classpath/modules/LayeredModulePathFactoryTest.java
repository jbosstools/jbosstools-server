/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.classpath.modules;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.jbossmodules.LayeredModulePathFactory;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.After;

public class LayeredModulePathFactoryTest extends TestCase {
	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	} 
	
	/**
	 * First test to make sure our testing structure is doing the right thing, making a mocked patch installation, etc
	 */
	public void testOverlayAndLayerAdditions() throws IOException {
		IServer s2 = MockJBossModulesUtil.createMockServerWithRuntime(IJBossToolingConstants.SERVER_EAP_61, "TestOne");
		IRuntime rt = s2.getRuntime();
		IPath modules = rt.getLocation().append("modules");
		File[] possible = LayeredModulePathFactory.resolveLayeredModulePath(modules.toFile());
		assertTrue(possible.length == 2);
		
		IPath layersRoot = modules.append("system").append("layers");
		IPath base = layersRoot.append("base");
		
		// Add an overlay
		MockJBossModulesUtil.addOverlay(base, "overlay1");
		MockJBossModulesUtil.setActiveOverlays(base, new String[]{"overlay1"});
		File[] possible2 = LayeredModulePathFactory.resolveLayeredModulePath(modules.toFile());
		assertTrue(possible2.length == 3);

		// Add an overlay2, inactive
		MockJBossModulesUtil.addOverlay(base, "overlay2");
		File[] possible3 = LayeredModulePathFactory.resolveLayeredModulePath(modules.toFile());
		assertTrue(possible3.length == 3);
		
		// Enable that overlay
		MockJBossModulesUtil.setActiveOverlays(base, new String[]{"overlay1", "overlay2"});
		File[] possible4 = LayeredModulePathFactory.resolveLayeredModulePath(modules.toFile());
		assertTrue(possible4.length == 4);

		// Add a layer
		IPath myLayer = MockJBossModulesUtil.addLayer(modules, "mylayer", true);
		File[] possible5 = LayeredModulePathFactory.resolveLayeredModulePath(modules.toFile());
		assertTrue(possible5.length == 5);
		
		// Add an overlay to my layer, do not activate
		MockJBossModulesUtil.addOverlay(myLayer, "mylayerOverlay1");
		File[] possible6 = LayeredModulePathFactory.resolveLayeredModulePath(modules.toFile());
		assertTrue(possible6.length == 5);
		
		// Activate the overlay
		MockJBossModulesUtil.setActiveOverlays(myLayer, new String[]{"mylayerOverlay1"});
		possible6 = LayeredModulePathFactory.resolveLayeredModulePath(modules.toFile());
		assertTrue(possible6.length == 6);
	}
}
