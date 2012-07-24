package org.jboss.tools.as.test.core.parametized.server;

import java.io.File;
import java.util.Collection;

import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.as.test.core.TestConstants;
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
	 @Parameters
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
		if (!new File(loc).exists())
			fail(serverType + " (" + loc + ") does not exist.");
	}

}
