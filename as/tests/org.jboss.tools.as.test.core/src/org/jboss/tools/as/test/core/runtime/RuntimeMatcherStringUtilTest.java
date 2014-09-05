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
package org.jboss.tools.as.test.core.runtime;

import org.jboss.tools.as.runtimes.integration.internal.RuntimeMatcherStringUtil;
import org.junit.Test;

import junit.framework.TestCase;

public class RuntimeMatcherStringUtilTest extends TestCase {
	
	@Test
	public void testSafeVersion() {
		assertEquals("6.0", RuntimeMatcherStringUtil.getSafeVersionString("6.0"));
		assertEquals("6.0.", RuntimeMatcherStringUtil.getSafeVersionString("6.0."));
		assertEquals("6.0.1", RuntimeMatcherStringUtil.getSafeVersionString("6.0.1"));
		assertEquals("6.0.1.", RuntimeMatcherStringUtil.getSafeVersionString("6.0.1."));
		assertEquals("6.0.1.AB", RuntimeMatcherStringUtil.getSafeVersionString("6.0.1.AB"));
		assertEquals("6.0.0.CR01", RuntimeMatcherStringUtil.getSafeVersionString("6.0.0.CR01"));
		assertEquals("6.0.0.CR01_1", RuntimeMatcherStringUtil.getSafeVersionString("6.0.0.CR01.1"));
		assertEquals("6.0.0.CR01_1_", RuntimeMatcherStringUtil.getSafeVersionString("6.0.0.CR01.1."));
		assertEquals("6.0.0.CR01_1_1", RuntimeMatcherStringUtil.getSafeVersionString("6.0.0.CR01.1.1"));
	}
}
