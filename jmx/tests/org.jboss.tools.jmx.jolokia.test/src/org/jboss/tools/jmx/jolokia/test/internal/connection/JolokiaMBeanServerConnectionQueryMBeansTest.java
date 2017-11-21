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

import org.jboss.tools.jmx.jolokia.test.util.AttributeChecking;
import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.jboss.tools.jmx.jolokia.test.util.OperationChecking;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JolokiaMBeanServerConnectionQueryMBeansTest extends JolokiaTestEnvironmentSetup {
	
	@Test
	public void testQueryWithFullySpecifiedName() throws Exception {
		ObjectName javaLangMemoryObjectName = new ObjectName("java.lang:type=Memory");
		Set<ObjectInstance> mBeans = jolokiaMBeanServerConnection.queryMBeans(javaLangMemoryObjectName, null);
		
		assertThat(mBeans).containsExactly(new ObjectInstance(javaLangMemoryObjectName, "sun.management.MemoryImpl"));
	}
	
	@Test
	public void testQueryWithPartialSpecifiedName() throws Exception {
		Set<ObjectInstance> mBeans = jolokiaMBeanServerConnection.queryMBeans(new ObjectName(JOLOKIA_IT_DOMAIN+":*"), null);
		
		assertThat(mBeans).containsOnly(
				new ObjectInstance(new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation"), OperationChecking.class.getName()),
				new ObjectInstance(new ObjectName(JOLOKIA_IT_DOMAIN+":type=attributetest"), AttributeChecking.class.getName()));
	}
}
