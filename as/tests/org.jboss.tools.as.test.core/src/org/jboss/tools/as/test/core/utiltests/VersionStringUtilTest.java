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
package org.jboss.tools.as.test.core.utiltests;

import junit.framework.TestCase;

import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;

public class VersionStringUtilTest extends TestCase {
	public void testVersionMajorMinorTrim() {
		assertEquals("4.1", r("4.1.0.Alpha1"));
		assertEquals("4.10", r("4.10.0.Alpha3"));
		assertEquals("1.100", r("1.100.Alpha3"));
		assertEquals("100.3", r("100.3.GA"));
		assertEquals("100.3", r("100.3")); 
	}
	
	// Simple util method. 
	private String r(String v) {
		return ServerBeanLoader.getMajorMinorVersion(v);
	}
}
