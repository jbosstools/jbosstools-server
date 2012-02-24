package org.jboss.ide.eclipse.as.test.server;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

public class ServerSecureStorageTest extends TestCase {
//	public void tearDown() {
//		try {
//			ProjectUtility.deleteAllProjects();
//			ServerRuntimeUtils.deleteAllServers();
//			ServerRuntimeUtils.deleteAllRuntimes();
//		} catch(Exception e) {
//		}
//	}
//	
//	public void testServerSecureStorage() throws CoreException {
//		IServer server = ServerRuntimeUtils.create60Server();
//		String val = ServerUtil.getFromSecureStorage(server, "TEST");
//		assertNull(val);
//		ServerUtil.storeInSecureStorage(server, "TEST", "VAL");
//		val = ServerUtil.getFromSecureStorage(server, "TEST");
//		assertEquals(val, "VAL");
//	}
//	
//	public void testUserPass() throws CoreException {
//		IServer server = ServerRuntimeUtils.create60Server();
//		JBossServer jbs = ServerConverter.getJBossServer(server);
//		assertEquals(jbs.getUsername(), "admin");
//		assertEquals(jbs.getPassword(), "admin");
//
//		IServerWorkingCopy wc = server.createWorkingCopy();
//		wc.setAttribute(IJBossToolingConstants.SERVER_USERNAME, "newUser");
//		wc.setAttribute(IJBossToolingConstants.SERVER_PASSWORD, "newPass");
//		server = wc.save(false, null);
//		
//		assertEquals(jbs.getUsername(), "newUser");
//		assertEquals(jbs.getPassword(), "newPass");
//		
//		try {
//			jbs = ServerConverter.getJBossServer(server);
//			jbs.setUsername("failUser");
//			fail();
//		} catch( NullPointerException npe ) {
//			// expected... cannot set attributes when there is no working copy!
//		}
//		
//		IServer server2 = ServerCore.findServer(server.getId());
//		jbs = ServerConverter.getJBossServer(server2);
//		assertEquals(jbs.getUsername(), "newUser");
//		assertEquals(jbs.getPassword(), "newPass");
//		
//		// Note, the jbs is NOT from this new working copy
//		try {
//			wc = server2.createWorkingCopy();
//			jbs.setUsername("successUser");
//			fail();
//		} catch(NullPointerException npe ) {
//			// expected. The jbs must be created after the working copy
//		}
//		
//		// Note, jbs must be made out of wc
//		wc = server2.createWorkingCopy();
//		jbs = ServerConverter.getJBossServer(wc);
//		jbs.setUsername("successUser");
//		jbs.setPassword("successPass");
//		server = wc.save(false, null);
//		
//		jbs = ServerConverter.getJBossServer(server);
//		assertEquals(jbs.getUsername(), "successUser");
//		assertEquals(jbs.getPassword(), "successPass");
//	}
}
