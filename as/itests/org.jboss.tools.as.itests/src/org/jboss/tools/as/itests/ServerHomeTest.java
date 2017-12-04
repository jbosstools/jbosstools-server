/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.itests;

import java.io.File;
import java.util.Collection;

import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.as.test.core.TestConstants;
import org.jboss.tools.as.test.core.parametized.server.ServerParameterUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ServerHomeTest extends Assert {
	

	 private String serverType;
	 public ServerHomeTest(String serverType) {
		 this.serverType = serverType;
	 }
	 @Parameters(name = "{0}")
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParamterers());
	 }
	@Test
	public void testServerHomeSet() {
		assertNotNull(serverType);
		IServerType type = ServerCore.findServerType(serverType);
		if( type == null )
			fail("Server type " + type + " not found in the build");
		if( type.getRuntimeType() == null ) 
			fail("Server type " + serverType + " does not have an associated runtime");
		String loc = TestConstants.getServerHome(serverType);
		if( loc == null )
			fail( "Test Runtime for " + serverType + " is not set properly in the build" );
		boolean exists = new File(loc).exists();
		if (!exists)
			fail(serverType + " (" + loc + ") does not exist.");
	}
}
