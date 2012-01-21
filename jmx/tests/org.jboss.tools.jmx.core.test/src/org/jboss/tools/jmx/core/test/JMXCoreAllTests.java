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
package org.jboss.tools.jmx.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JMXCoreAllTests extends TestSuite {
    public static Test suite(){
        return new JMXCoreAllTests();
    }

	public JMXCoreAllTests() {
		super("JMX Core All Tests");
		addTest(new TestSuite(DefaultProviderTest.class));
		addTest(new TestSuite(NodeBuilderTestCase.class));
		addTestSuite(JMXExceptionTest.class);
		addTestSuite(ImpactTest.class);
		addTestSuite(ErrorRootTest.class);
	}
}
