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
package org.jboss.tools.as.test.core.parametized.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ServerRuntimeArrayConstantsTest extends Assert {
	

	 private String typeId;
	 public ServerRuntimeArrayConstantsTest(String typeId) {
		 this.typeId = typeId;
	 }
	 @Parameters(name = "{0}")
	 public static Collection<Object[]> data() {
		 IServerType[] servers = ServerCore.getServerTypes();
		 IRuntimeType[] rtTypes = ServerCore.getRuntimeTypes();
		 
		 // add all server / runtime type id's
		 ArrayList<String> collector = new ArrayList<String>();
		 for( int i = 0; i < servers.length; i++ ) {
			 if( servers[i].getId().startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX) || servers[i].getId().startsWith(IJBossToolingConstants.SERVER_AS_PREFIX))
				 collector.add(servers[i].getId());
		 }
		 for( int i = 0; i < rtTypes.length; i++ ) {
			 if( rtTypes[i].getId().startsWith(IJBossToolingConstants.RUNTIME_PREFIX))
				 collector.add(rtTypes[i].getId());
		 }
		 
		 // exclude deploy-only items
		 collector.remove(IJBossToolingConstants.DEPLOY_ONLY_RUNTIME);
		 collector.remove(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
		 
		 String[] allTypes = (String[]) collector.toArray(new String[collector.size()]);
		 return ServerParameterUtils.asCollection(allTypes);
	 }
	@Test
	public void testServerHomeSet() {
		assertNotNull(typeId);
		IServerType type = ServerCore.findServerType(typeId);
		if( type != null ) {
			if( type.getRuntimeType() == null ) 
				fail("Server type " + typeId + " does not have an associated runtime");
			String rtType = type.getRuntimeType().getId();
			if( rtType == null ) {
				fail("Runtime type for servertype " + typeId + " has a null id.");
			}
			assertTrue(Arrays.asList(IJBossToolingConstants.ALL_JBOSS_RUNTIMES).contains(rtType));
		} else {
			IRuntimeType t = ServerCore.findRuntimeType(typeId);
			assertNotNull(t);
			assertTrue(Arrays.asList(IJBossToolingConstants.ALL_JBOSS_RUNTIMES).contains(typeId));
		}
		
	}
}
