/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.test.internal.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.CartridgeAsserts.assertThatContainsCartridge;

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ListCartridgesIntegrationTest {

	private OpenshiftService openshiftService;

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
	}

	@Test
	public void canListCartridges() throws Exception {
		List<Cartridge> cartridges = openshiftService.getCartridges();
		assertNotNull(cartridges);
		assertTrue(cartridges.size() > 0);
		assertThatContainsCartridge("jbossas-7.0", cartridges);
	}
}
