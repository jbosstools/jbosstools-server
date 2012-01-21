/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.test;
import org.jboss.tools.jmx.ui.internal.test.MBeanUtilsTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JMXUIAllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MBeanUtilsTestCase.class);
		return suite;
	}
}
