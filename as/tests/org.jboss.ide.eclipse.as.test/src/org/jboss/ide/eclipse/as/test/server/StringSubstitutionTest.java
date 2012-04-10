package org.jboss.ide.eclipse.as.test.server;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;


public class StringSubstitutionTest extends TestCase /* SimpleServerImplTest */ {

	public void testAS7ConfigFileReplacement() throws CoreException {
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_AS_70, "server1", "default");
		IRuntime rt = server.getRuntime();
		
		String ret = new ConfigNameResolver().performSubstitutions("some/path", server.getName());
		assertEquals("some/path", ret);
		ret = new ConfigNameResolver().performSubstitutions("some/path/${jboss_config_file}" , server.getName());
		assertEquals("some/path/standalone.xml", ret);

		IRuntimeWorkingCopy rtwc = rt.createWorkingCopy();
		LocalJBoss7ServerRuntime rtwc7 = (LocalJBoss7ServerRuntime)rtwc.loadAdapter(LocalJBoss7ServerRuntime.class, new NullProgressMonitor());
		rtwc7.setConfigurationFile("someFile.xml");
		rtwc.save(false, new NullProgressMonitor());
		ret = new ConfigNameResolver().performSubstitutions("some/path/${jboss_config_file}" , server.getName());
		assertEquals("some/path/someFile.xml", ret);
	}
	
//	protected void serverTestImpl(String type) {
//		// TODO how to handle this check for every server type?  Not needed right now
//	}

}
