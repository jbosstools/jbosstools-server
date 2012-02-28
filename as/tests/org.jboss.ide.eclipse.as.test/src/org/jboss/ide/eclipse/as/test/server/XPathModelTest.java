package org.jboss.ide.eclipse.as.test.server;

import java.io.File;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;

public class XPathModelTest extends SimpleServerImplTest {

	static {
		JobUtils.waitForIdle(5000);
	}
	
	public void setUp() {
		JobUtils.waitForIdle();
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
	
}
