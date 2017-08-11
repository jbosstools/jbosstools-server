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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.tools.jmx.jolokia.internal.connection.JolokiaMBeanServerConnection;
import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JolokiaMBeanServerConnectionInvocationTest extends JolokiaTestEnvironmentSetup {
	
	private static final String JOLOKIA_IT_OPERATION = JOLOKIA_IT_DOMAIN+":type=operation";
	private JolokiaMBeanServerConnection jolokiaMBeanServerConnection;

	@Before
	public void setup(){
		jolokiaMBeanServerConnection = new JolokiaMBeanServerConnection(j4pClient, null);
	}

	@Test
	public void testInvocationWithoutParameters() throws Exception {
		Object res = jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_OPERATION), "reset", new Object[]{}, new String[]{});
		assertThat(res).isNull();
	}
	
	@Test
	public void testInvocationWithParameter() throws Exception {
		Object res = jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_OPERATION), "overloadedMethod", new Object[]{"dummy"}, new String[]{String.class.getName()});
		assertThat(res).isEqualTo(1L);
	}
	
	@Test
	@Ignore("Conversion of invocation return type nto supported for specific case of overloaded methods")
	public void testInvocationReturningTypeConvertedWithOverloadedMethodHavingSeveralMethodsWithSameArgumentNumber() throws Exception {
		Object res = jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_OPERATION), "overloadedMethod", new Object[]{"dummy"}, new String[]{String.class.getName()});
		assertThat(res).isEqualTo(1);
	}
	
	@Test
	public void testInvocationReturningTypeConvertedWithOverloadedMethodHavingSeveralMethodsWithDifferentArgumentNumber() throws Exception {
		Object res = jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_OPERATION), "overloadedMethod", new Object[]{"dummy", 1}, new String[]{String.class.getName(), int.class.getName()});
		assertThat(res).isEqualTo(2);
	}
	
	@Test
	public void testInvocationWithSetResult() throws Exception {
		Set<Object> res = (Set<Object>) jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_OPERATION), "setOfResult", new Object[]{}, new String[]{});
		assertThat(res).containsOnly("value1", "value2");
	}
	
	@Test
	public void testInvocationReturningLong() throws Exception {
		invokeWithoutParameter("returnLong", 1L);
	}
	
	@Test
	public void testInvocationReturningLongObject() throws Exception {
		invokeWithoutParameter("returnLongObject", Long.valueOf(1L));
	}
	
	@Test
	public void testInvocationReturningInt() throws Exception {
		invokeWithoutParameter("returnInt", 1);
	}
	
	@Test
	public void testInvocationReturningInteger() throws Exception {
		invokeWithoutParameter("returnIntegerObject", Integer.valueOf(1));
	}
	
	@Test
	public void testInvocationReturningDouble() throws Exception {
		invokeWithoutParameter("returnDouble", Double.MAX_VALUE);
	}
	
	@Test
	public void testInvocationReturningShort() throws Exception {
		invokeWithoutParameter("returnShort", (short)1);
	}
	
	@Test
	public void testInvocationReturningFloat() throws Exception {
		invokeWithoutParameter("returnFloat", Float.MAX_VALUE);
	}
	
	@Test
	@Ignore("https://github.com/rhuss/jolokia/issues/334 need to be fixed first")
	public void testInvocationReturningBigInteger() throws Exception {
		invokeWithoutParameter("returnBigInteger", BigInteger.TEN);
	}
	
	@Test
	public void testInvocationReturningBigDecimal() throws Exception {
		invokeWithoutParameter("returnBigDecimal", BigDecimal.TEN);
	}

	@Test
	public void testInvocationReturningDate() throws Exception {
		invokeWithoutParameter("returnDate", Date.from(Instant.ofEpochSecond(50000)));
	}
	
	protected void invokeWithoutParameter(String operationNameWithNoParameter, Object expectedResult) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException {
		Object res = jolokiaMBeanServerConnection.invoke(new ObjectName(JOLOKIA_IT_OPERATION), operationNameWithNoParameter, new Object[]{}, new String[]{});
		assertThat(res).isInstanceOf(expectedResult.getClass());
		assertThat(res).isEqualTo(expectedResult);
	}
	
}
