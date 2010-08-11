package org.jboss.ide.eclipse.as.test.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.publishing.AbstractDeploymentTest;
import org.osgi.framework.Bundle;

public class ServerRuntimeUtils extends TestCase {
	public static final IVMInstall VM_INSTALL = JavaRuntime.getDefaultVMInstall();
	public static final String DEFAULT_CONFIG = "default";
	public static final String twiddle_suffix = ".mf.twiddle.jar";
	public static final String twiddle_3_2_8 = "3.2.8" + twiddle_suffix;
	public static final String twiddle_4_0_5 = "4.0.5" + twiddle_suffix;
	public static final String twiddle_4_2_3 = "4.2.3" + twiddle_suffix;
	public static final String twiddle_5_0_0 = "5.0.0" + twiddle_suffix;
	public static final String twiddle_5_0_1 = "5.0.1" + twiddle_suffix;
	public static final String twiddle_5_1_0 = "5.1.0" + twiddle_suffix;
	public static final String twiddle_6_0_0 = "6.0.0" + twiddle_suffix;
	public static final String twiddle_eap_4_3 = "eap4.3" + twiddle_suffix;
	public static final String twiddle_eap_5_0 = "eap5.0" + twiddle_suffix;
	public static final String run_jar = "run.jar";
	public static final String service_xml = "service.xml";
	public static final IPath mockedServers = ASTest.getDefault().getStateLocation().append("mockedServers");
	public static HashMap<String, String> twiddleMap = new HashMap<String, String>();
	public static HashMap<String, String> serverRuntimeMap = new HashMap<String, String>();
	
	static {
		twiddleMap.put(IJBossToolingConstants.SERVER_AS_32, twiddle_3_2_8);
		twiddleMap.put(IJBossToolingConstants.SERVER_AS_40, twiddle_4_0_5);
		twiddleMap.put(IJBossToolingConstants.SERVER_AS_42, twiddle_4_2_3);
		twiddleMap.put(IJBossToolingConstants.SERVER_AS_50, twiddle_5_0_0);
		twiddleMap.put(IJBossToolingConstants.SERVER_AS_51, twiddle_5_1_0);
		twiddleMap.put(IJBossToolingConstants.SERVER_AS_60, twiddle_6_0_0);
		twiddleMap.put(IJBossToolingConstants.SERVER_EAP_43, twiddle_eap_4_3);
		twiddleMap.put(IJBossToolingConstants.SERVER_EAP_50, twiddle_eap_5_0);

		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_32, IJBossToolingConstants.AS_32);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_40, IJBossToolingConstants.AS_40);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_42, IJBossToolingConstants.AS_42);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_50, IJBossToolingConstants.AS_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_51, IJBossToolingConstants.AS_51);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_60, IJBossToolingConstants.AS_60);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_43, IJBossToolingConstants.EAP_43);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_50, IJBossToolingConstants.EAP_50);
}
	
	public static IServer createMockServerWithRuntime(String serverType, String name, String config) {
		try {
			IPath serverDir = createMockServerDirectory(name, twiddleMap.get(serverType), config);
			return createServer(serverRuntimeMap.get(serverType), serverType, serverDir.toOSString(), config);
		} catch( CoreException ce ) {
		}
		return null;
	}
	
	public static IServer createServer(String runtimeID, String serverID,
			String location, String configuration) throws CoreException {
		// if file doesnt exist, abort immediately.
		assertTrue("path \"" + location + "\" does not exist", new Path(location).toFile().exists());

		IRuntime currentRuntime = createRuntime(runtimeID, location,
				configuration);
		IServerType serverType = ServerCore.findServerType(serverID);
		IServerWorkingCopy serverWC = serverType.createServer(null, null,
				new NullProgressMonitor());
		serverWC.setRuntime(currentRuntime);
		serverWC.setName(serverID);
		serverWC.setServerConfiguration(null);
		return serverWC.save(true, new NullProgressMonitor());
	}

	public static IRuntime createRuntime(String runtimeId, String homeDir,
			String config) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null,
				runtimeId);
		assertEquals("expects only one runtime type", runtimeTypes.length, 1);
		IRuntimeType runtimeType = runtimeTypes[0];
		IRuntimeWorkingCopy runtimeWC = runtimeType.createRuntime(null,
				new NullProgressMonitor());
		runtimeWC.setName(runtimeId);
		runtimeWC.setLocation(new Path(homeDir));
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_VM_ID, VM_INSTALL.getId());
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_VM_TYPE_ID, VM_INSTALL
						.getVMInstallType().getId());
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);

		IRuntime savedRuntime = runtimeWC.save(true, new NullProgressMonitor());
		return savedRuntime;
	}
	
	public static void deleteAllServers() throws CoreException {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			servers[i].delete();
		}
	}

	public static void deleteAllRuntimes() throws CoreException {
		// FIXME It doesn't harm to be commented, but location is null should be fixed
//		IRuntime[] runtimes = ServerCore.getRuntimes();
//		for( int i = 0; i < runtimes.length; i++ ) {
//			assertNotNull("runtime " + runtimes[i].getName() + " has a null location", runtimes[i].getLocation());
//			if( mockedServers.isPrefixOf(runtimes[i].getLocation())) {
//				FileUtil.completeDelete(runtimes[i].getLocation().toFile());
//			}
//			runtimes[i].delete();
//		}
	}
	

	
	public static IPath createMockServerDirectory(String name, String twiddleJar, String configurationName )  {
		IPath loc = mockedServers.append(name);
		try {
			loc.toFile().mkdirs();
			loc.append("bin").toFile().mkdirs();
			loc.append("server").toFile().mkdirs();
			loc.append("server").append(configurationName).toFile().mkdirs();
			IPath configConf = loc.append("server").append(configurationName).append("conf");
			configConf.toFile().mkdirs();
			File twiddleLoc = getFileLocation("serverMock/" + twiddleJar);
			FileUtil.fileSafeCopy(twiddleLoc, loc.append("bin").append("twiddle.jar").toFile());
			File runJar = getFileLocation("serverMock/run.jar");
			FileUtil.fileSafeCopy(runJar, loc.append("bin").append("run.jar").toFile());
			File serviceXml = getFileLocation("serverMock/jboss-service.xml");
			FileUtil.fileSafeCopy(serviceXml, configConf.append("jboss-service.xml").toFile());
			return loc;
		} catch( CoreException ce ) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		}
	}

	// Find a file in our bundle
	protected static File getFileLocation(String path) throws CoreException {
		Bundle bundle = Platform.getBundle(AbstractDeploymentTest.BUNDLE_NAME);
		URL url = null;
		try {
			url = FileLocator.resolve(bundle.getEntry(path));
		} catch (IOException e) {
			String msg = "Cannot find file " + path + " in " + AbstractDeploymentTest.BUNDLE_NAME;
			IStatus status = new Status(IStatus.ERROR, ASTest.PLUGIN_ID, msg, e);
			throw new CoreException(status);
		}
		String location = url.getFile();
		return new File(location);
	}
}
