/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.management.itests;

import org.jboss.tools.as.management.itests.utils.JREParameterUtils;
import org.jboss.tools.as.management.itests.utils.ParameterUtils;
import org.junit.After;
import org.junit.Before;

import junit.framework.TestCase;

public class VerifyJREFlagsTest extends TestCase {
	
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
	
	public void testJavaHomesSet() {
		if( ParameterUtils.isSingleRuntime()) {
			String home = ParameterUtils.getSingleRuntimeHome();
			assertNotNull(home);
			JREParameterUtils.verifyJava8HomeSet();
			JREParameterUtils.verifyJava11HomeSet();
		} else {
			JREParameterUtils.verifyJava8HomeSet();
			JREParameterUtils.verifyJava11HomeSet();
		}
	}
}
