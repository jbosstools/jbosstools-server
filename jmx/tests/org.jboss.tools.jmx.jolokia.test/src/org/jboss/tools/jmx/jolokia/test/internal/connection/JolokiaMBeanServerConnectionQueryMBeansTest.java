/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.test.internal.connection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.tools.jmx.jolokia.internal.connection.JolokiaMBeanServerConnection;
import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.jboss.tools.jmx.jolokia.test.util.OperationChecking;
import org.junit.Before;
import org.junit.Test;

public class JolokiaMBeanServerConnectionQueryMBeansTest extends JolokiaTestEnvironmentSetup {
	
	private JolokiaMBeanServerConnection jolokiaMBeanServerConnection;

	@Before
	public void setup(){
		jolokiaMBeanServerConnection = new JolokiaMBeanServerConnection(j4pClient, null);
	}

	@Test
	public void testQueryWithFullySpecifiedName() throws Exception {
		Set<ObjectInstance> mBeans = jolokiaMBeanServerConnection.queryMBeans(new ObjectName("java.lang:type=Memory"), null);
		
		assertThat(mBeans).containsExactly(new ObjectInstance(new ObjectName("java.lang:type=Memory"), "sun.management.MemoryImpl"));
	}
	
	@Test
	public void testQueryWithPartialSpecifiedName() throws Exception {
		Set<ObjectInstance> mBeans = jolokiaMBeanServerConnection.queryMBeans(new ObjectName(JOLOKIA_IT_DOMAIN+":*"), null);
		
		assertThat(mBeans).containsExactly(new ObjectInstance(new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation"), OperationChecking.class.getName()));
	}
}
