package org.jboss.tools.as.test.core.utiltests;

import java.util.ArrayList;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISubSystemConfigurator;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchConfigProperties;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;

public class RSEUtilsTest extends TestCase {
	
	public static final String HOST_NAME = "127.0.0.1";
	
	private IHost host;
	protected void tearDown() throws Exception {
		ASMatrixTests.cleanup();
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		sr.deleteHost(host);
	}
	
	public void testRemoteUnix() {
		host = createLinuxHost(HOST_NAME);
		assertNotNull(host);
		String home = "/home/wonkauser/jboss-eap-5.1";
		IServer s = createEAP5Server(home);
		String args = getStartArgsForServer(s);
		assertTrue(args.contains("/home/wonkauser/jboss-eap-5.1/lib/endorsed"));
		assertTrue(args.contains("/home/wonkauser/jboss-eap-5.1/server"));
		assertTrue(args.contains("/home/wonkauser/jboss-eap-5.1/bin/native"));
	}
	public void testRemoteSshOnly() {
		host = createSshOnlyHost(HOST_NAME);
		assertNotNull(host);
		String home = "/home/wonkauser/jboss-eap-5.1";
		IServer s = createEAP5Server(home);
		String args = getStartArgsForServer(s);
		assertTrue(args.contains("/home/wonkauser/jboss-eap-5.1/lib/endorsed"));
		assertTrue(args.contains("/home/wonkauser/jboss-eap-5.1/server"));
		assertTrue(args.contains("/home/wonkauser/jboss-eap-5.1/bin/native"));
	}
	public void testRemoteWindows() {
		host = createWindowsHost(HOST_NAME);
		assertNotNull(host);
		String home = "c:\\apps\\jboss\\jboss-eap-5.1";
		IServer s = createEAP5Server(home);
		String args = getStartArgsForServer(s);
		assertTrue(args.contains("c:\\apps\\jboss\\jboss-eap-5.1\\lib\\endorsed"));
		assertTrue(args.contains("c:\\apps\\jboss\\jboss-eap-5.1\\server"));
		assertTrue(args.contains("c:\\apps\\jboss\\jboss-eap-5.1\\bin\\native"));
	}

	
	public IServer createEAP5Server(String homeDir) {
		IServer s = null;
		try {
			s = createRemoteServer(IJBossToolingConstants.EAP_50, host, homeDir, "default");
		} catch(CoreException ce) {
			fail("Server not created: " + ce.getMessage());
		}
		if( s == null ) {
			fail("Server not created.");
		}
		return s;
	}
	public String getStartArgsForServer(IServer s) {
		try {
			ILaunchConfiguration lc = s.getLaunchConfiguration(true, new NullProgressMonitor());
			String currentStartupCmd = RSELaunchConfigProperties.getStartupCommand(lc);
			return currentStartupCmd;
		} catch (CoreException e) {
			fail(e.getMessage());
			return null;
		}
	}
	public IServer createRemoteServer(String serverType, IHost host, String remoteHome, String remoteConfig) throws CoreException {
		IServer s = ServerCreationTestUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_EAP_50, "server1");
		return RSEUtils.setServerToRSEMode(s, host, 
				remoteHome, remoteConfig);
	}
	
	public IHost createSshOnlyHost(String hostname) throws AssertionFailedError {
		String[] configurations = new String[]{
				"ssh.files", "ssh.shells", "ssh.terminals"};
		return createHost(hostname, "org.eclipse.rse.systemtype.ssh", configurations);
	}
	public IHost createLinuxHost(String hostName) throws AssertionFailedError {
		String[] configurations = new String[]{
				"ssh.files", "processes.shell.linux",
				"ssh.shells", "ssh.terminals"};
		return createHost(hostName, "org.eclipse.rse.systemtype.linux", configurations);
	}
	public IHost createWindowsHost(String hostName) throws AssertionFailedError {
		String[] configurations = new String[]{"dstore.windows.files", "dstore.shells"};
		return createHost(hostName, "org.eclipse.rse.systemtype.windows", configurations);
	}
	public IHost createHost(String hostName, String sysType, String[] configurationIds) throws AssertionFailedError {
		String profileName = getOrCreateSystemProfileName();
		IRSESystemType systemType = findSystemType(sysType);
		assertNotNull("System type not found: " + sysType);
		ArrayList<ISubSystemConfigurator> configuratorList = new ArrayList<ISubSystemConfigurator>();
		for( int i = 0; i < configurationIds.length; i++ ) {
			configuratorList.add(createSubsystemConfigurator(configurationIds[i]));
		}
		ISubSystemConfigurator[] configurators = (ISubSystemConfigurator[]) configuratorList
				.toArray(new ISubSystemConfigurator[configuratorList.size()]);
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		try {
			IHost host = sr.createHost(profileName, systemType, hostName, hostName, "test host", "", 0, configurators);
			return host;
		} catch(Exception e) {
			fail("Failed to create host: " + e.getMessage());
		}
		return null;
	}
	

	private ISubSystemConfigurator createSubsystemConfigurator(String id) {
		return new DefaultConfigurator(findSubsystemConfiguration(id));
	}
	private ISubSystemConfiguration findSubsystemConfiguration(String id) {
		return RSECorePlugin.getTheSystemRegistry().getSubSystemConfiguration(id);
	}
	private class DefaultConfigurator implements ISubSystemConfigurator {
		private ISubSystemConfiguration _configuration;
		public DefaultConfigurator(ISubSystemConfiguration configuration){
			_configuration = configuration;
		}
		
		public boolean applyValues(ISubSystem ss) {
			return true;
		}

		public ISubSystemConfiguration getSubSystemConfiguration() {
			return _configuration;
		}						
	}

	
	private String getOrCreateSystemProfileName() {
		String[] profiles = RSECorePlugin.getTheSystemProfileManager().getActiveSystemProfileNames();
		for( int i = 0; i < profiles.length; i++ )
			if( profiles[i] != null )
				return profiles[i];
		ISystemProfile p = RSECorePlugin.getTheSystemProfileManager().createSystemProfile("test", true);
		return p.getName();
	}
	
	private IRSESystemType findSystemType(String id) {
		IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
		for( int i = 0; i < systemTypes.length; i++ ) {
			if( systemTypes[i].getId().equals(id))
				return systemTypes[i];
		}
		return null;
	}
}
