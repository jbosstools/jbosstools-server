package org.jboss.ide.eclipse.as.test.publishing;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.server.StartupShutdownTest;

public class JBIDE4184Test extends TestCase {
	private IServer server;
	private IPath tmpPath;
	public void setUp() {
		try {
			tmpPath = new Path(ASTest.JBOSS_AS_42_HOME);
			tmpPath = tmpPath.append("server").append("default").append("tmp").append(IJBossServerConstants.JBOSSTOOLS_TMP);
			if( tmpPath.toFile().exists())
				FileUtil.safeDelete(tmpPath.toFile());
			assertFalse(tmpPath.toFile().exists());
			server = StartupShutdownTest.createServer(
					IJBossToolingConstants.AS_42, IJBossToolingConstants.SERVER_AS_42, 
					ASTest.JBOSS_AS_42_HOME, StartupShutdownTest.DEFAULT_CONFIG);
		} catch( CoreException ce) {
			fail(ce.getMessage());
		}
	}
	public void testJira() {
		try {
			IServerWorkingCopy wc = server.createWorkingCopy();
			IDeployableServer ds = (IDeployableServer)wc.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
			ds.setDeployLocationType(IDeployableServer.DEPLOY_SERVER);
			server = wc.save(true, new NullProgressMonitor());
			String folder = ds.getTempDeployFolder();
			File f = new Path(folder).toFile();
			assertTrue(f.exists());
		} catch( CoreException ce) {
			fail(ce.getMessage());
		}
	}
	public void tearDown() {
		try {
			IRuntime rt = server.getRuntime();
			server.delete();
			rt.delete();
			FileUtil.safeDelete(tmpPath.toFile());
		} catch( CoreException ce) {
			fail();
		}
	}
}
