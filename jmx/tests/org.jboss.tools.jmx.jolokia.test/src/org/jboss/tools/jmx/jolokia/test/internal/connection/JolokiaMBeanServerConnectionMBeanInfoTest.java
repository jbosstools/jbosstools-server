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

import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.jboss.tools.jmx.jolokia.test.util.OperationChecking;
import org.jboss.tools.jmx.jolokia.test.util.OperationCheckingMBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JolokiaMBeanServerConnectionMBeanInfoTest extends JolokiaTestEnvironmentSetup {
	
	@Test
	public void testClassNameAvailable() throws Exception {
		MBeanInfo res = jolokiaMBeanServerConnection.getMBeanInfo(new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation"));
		assertThat(res.getClassName()).isEqualTo(OperationChecking.class.getName());
	}
	
	@Test
	public void testGetOperations() throws Exception {
		MBeanInfo res = jolokiaMBeanServerConnection.getMBeanInfo(new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation"));
		assertThat(res.getOperations()).hasSameSizeAs(OperationCheckingMBean.class.getDeclaredMethods());
	}
	
}
