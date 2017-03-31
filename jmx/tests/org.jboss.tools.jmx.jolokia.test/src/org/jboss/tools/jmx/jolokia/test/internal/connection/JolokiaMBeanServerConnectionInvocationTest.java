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

import java.util.Collection;

import javax.management.ObjectName;

import org.jboss.tools.jmx.jolokia.internal.connection.JolokiaMBeanServerConnection;
import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.junit.Before;
import org.junit.Test;

public class JolokiaMBeanServerConnectionInvocationTest extends JolokiaTestEnvironmentSetup {
	
	private JolokiaMBeanServerConnection jolokiaMBeanServerConnection;

	@Before
	public void setup(){
		jolokiaMBeanServerConnection = new JolokiaMBeanServerConnection(j4pClient, null);
	}

	@Test
	public void testInvocationWithoutParameters() throws Exception {
		Object res = jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation"), "reset", new Object[]{}, new String[]{});
		assertThat(res).isNull();
	}
	
	@Test
	public void testInvocationWithParameter() throws Exception {
		Object res = jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation"), "overloadedMethod", new Object[]{"dummy"}, new String[]{String.class.getName()});
		assertThat(res).isEqualTo(1L);
	}
	
	@Test
	public void testInvocationWithSetResult() throws Exception {
		Collection<Object> res = (Collection<Object>) jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation"), "setOfResult", new Object[]{}, new String[]{});
		assertThat(res).containsOnly("value1", "value2");
	}
	
}
