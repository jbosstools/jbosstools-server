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

import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JolokiaMBeanServerConnectionGetDomainsTest extends JolokiaTestEnvironmentSetup {
	
	@Test
	public void testGetDomains() throws Exception {
		String[] res = jolokiaMBeanServerConnection.getDomains();
		assertThat(res).contains("java.lang", JOLOKIA_IT_DOMAIN);
	}
	
}
