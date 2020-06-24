/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.management;

import static org.junit.Assert.assertNotNull;

import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.junit.Test;

public class JBoss7ServerStateTest {
	
	@Test
	public void testStarting() {
		JBoss7ServerState ret  = JBoss7ServerState.valueOfIgnoreCase("starting");
		assertNotNull(ret);
	}

	@Test
	public void testRunning() {
		JBoss7ServerState ret  = JBoss7ServerState.valueOfIgnoreCase("running");
		assertNotNull(ret);
	}

	@Test
	public void testRestart() {
		JBoss7ServerState ret  = JBoss7ServerState.valueOfIgnoreCase("restart-required");
		assertNotNull(ret);
	}
}
