package org.jboss.ide.eclipse.as.test.server;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;

public class XPathModelTest extends TestCase {

	static {
		JobUtils.waitForIdle(5000);
	}
	
	public void setUp() {
		JobUtils.waitForIdle();
	}
	
	public void tearDown() {
		try {
			ServerRuntimeUtils.deleteAllServers();
			ServerRuntimeUtils.deleteAllRuntimes();
			ProjectUtility.deleteAllProjects();
			ASTest.clearStateLocation();
		} catch(Exception ce ) {
			// ignore
		}
	}
	
	protected void serverTestImpl(String type) {
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(type, "server1", "default");
		File xpathFile = JBossServerCorePlugin.getServerStateLocation(server).append(IJBossToolingConstants.XPATH_FILE_NAME).toFile();
		JobUtils.waitForIdle(500);
		System.out.println(xpathFile.exists());
		
		System.out.println(xpathFile.exists());
		if( !xpathFile.exists())
			fail("The XPath File has not been created. Xpaths will be lost on workspace restart");
	}
	public void test32Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_32);
	}

	public void test40Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_40);
	}

	public void test42Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_42);
	}
	
	public void test50Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_50);
	}
	public void test51Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_51);
	}
	public void test60Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_60);
	}
	public void testEap43Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_43);
	}	
	public void testEap50Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_50);
	}
	public void testEap60Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_60);
	}

}
