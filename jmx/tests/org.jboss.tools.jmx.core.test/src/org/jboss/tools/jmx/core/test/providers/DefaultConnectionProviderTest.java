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
package org.jboss.tools.jmx.core.test.providers;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.tools.jmx.core.providers.DefaultConnectionProvider;
import org.jboss.tools.jmx.core.providers.DefaultConnectionWrapper;
import org.jboss.tools.jmx.core.providers.MBeanServerConnectionDescriptor;
import org.junit.Test;

public class DefaultConnectionProviderTest {

	@Test
	public void testAddConnection() throws Exception {
		String id = "connectionId";
		MBeanServerConnectionDescriptor descriptor = new MBeanServerConnectionDescriptor(id, "", null, null);
		DefaultConnectionProvider provider = new DefaultConnectionProvider();
		
		provider.addConnection(new DefaultConnectionWrapper(descriptor));
		
		assertThat(provider.getConnection(id)).isNotNull();
	}
	
	@Test
	public void testRemoveConnectionWhenConnectionNotPresent() throws Exception {
		String id = "connectionId";
		DefaultConnectionProvider provider = new DefaultConnectionProvider();
		DefaultConnectionWrapper connection = new DefaultConnectionWrapper(new MBeanServerConnectionDescriptor(id, "", null, null));
		
		provider.removeConnection(connection);
		
		assertThat(provider.getConnection(id)).isNull();
	}
	
	@Test
	public void testRemoveConnectionWhenConnectionIsPresent() throws Exception {
		String id = "connectionId";
		MBeanServerConnectionDescriptor descriptor = new MBeanServerConnectionDescriptor(id, "", null, null);
		DefaultConnectionProvider provider = new DefaultConnectionProvider();
		DefaultConnectionWrapper connection = new DefaultConnectionWrapper(descriptor);
		provider.addConnection(connection);
		
		provider.removeConnection(connection);
		
		assertThat(provider.getConnection(id)).isNull();
	}
	
}
