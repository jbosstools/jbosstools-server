/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.jolokia.test.internal.connection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
	JolokiaMBeanUtilityTest.class,
	JolokiaMBeanServerConnectionGetDomainsTest.class,
	JolokiaMBeanServerConnectionInvocationTest.class,
	JolokiaMBeanServerConnectionMBeanInfoTest.class,
	JolokiaMBeanServerConnectionQueryMBeansTest.class
})
public class JolokiaTestSuite {
}
