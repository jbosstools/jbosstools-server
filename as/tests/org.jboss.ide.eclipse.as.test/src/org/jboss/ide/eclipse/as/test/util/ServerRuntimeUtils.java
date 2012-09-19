/******************************************************************************* 
 * Copyright (c) 2009 - 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.test.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServer.IOperationListener;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IProcessProvider;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerCreationUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.publishing.AbstractDeploymentTest;
import org.jboss.tools.test.util.JobUtils;
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
	public static final String as_server_7_0_jar = "7.0.0.mf.jboss-as-server.jar";
	public static final String as_server_7_1_jar = "7.1.0.mf.jboss-as-server.jar";
	public static final String twiddle_eap_4_3 = "eap4.3" + twiddle_suffix;
	public static final String twiddle_eap_5_0 = "eap5.0" + twiddle_suffix;
	public static final String eap_server_6_0_jar = "eap6.0.0.mf.jboss-as-server.jar";
	public static final String run_jar = "run.jar";
	public static final String service_xml = "service.xml";
	public static final IPath mockedServers = ASTest.getDefault().getStateLocation().append("mockedServers");
	public static HashMap<String, String> asSystemJar = new HashMap<String, String>();
	public static HashMap<String, String> serverRuntimeMap = new HashMap<String, String>();
	
	static {
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_32, twiddle_3_2_8);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_40, twiddle_4_0_5);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_42, twiddle_4_2_3);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_50, twiddle_5_0_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_51, twiddle_5_1_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_60, twiddle_6_0_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_70, as_server_7_0_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_AS_71, as_server_7_1_jar);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_43, twiddle_eap_4_3);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_50, twiddle_eap_5_0);
		asSystemJar.put(IJBossToolingConstants.SERVER_EAP_60, eap_server_6_0_jar);

		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_32, IJBossToolingConstants.AS_32);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_40, IJBossToolingConstants.AS_40);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_42, IJBossToolingConstants.AS_42);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_50, IJBossToolingConstants.AS_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_51, IJBossToolingConstants.AS_51);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_60, IJBossToolingConstants.AS_60);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_AS_70, IJBossToolingConstants.AS_70);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_43, IJBossToolingConstants.EAP_43);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_50, IJBossToolingConstants.EAP_50);
		serverRuntimeMap.put(IJBossToolingConstants.SERVER_EAP_60, IJBossToolingConstants.EAP_60);
	}
	
	public static IServer createMockDeployOnlyServer() throws CoreException {
		return ServerRuntimeUtils.createMockDeployOnlyServer(getDeployFolder(), getTmpDeployFolder());
	}
	public static IServer createMockJBoss7Server() throws CoreException {
		return createMockJBoss7Server(getDeployFolder(), getTmpDeployFolder());
	}
	
	private static String getDeployFolder() {
		return getBaseDir().append(getRandomString()).append("deploy").toOSString();
	}
	
	private static String getTmpDeployFolder() {
		return getBaseDir().append(getRandomString()).append("tmpDeploy").toOSString();
	}

	public static IPath getBaseDir() {
		return ASTest.getDefault().getStateLocation().append("testDeployments");
	}
	
	private static String getRandomString() {
		return String.valueOf(System.currentTimeMillis());
	}
	
	public static IServer createMockJBoss7Server(String deployLocation, String tempDeployLocation) throws CoreException {
		return createMockJBoss7Server("/", deployLocation, tempDeployLocation);
	}

	public static IServer createMockJBoss7Server(String rtLoc, String deployLocation, String tempDeployLocation) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(IJBossToolingConstants.AS_70, rtLoc, "default");
		IServer s = ServerCreationUtils.createServer2(runtime, IJBossToolingConstants.SERVER_AS_70);
		IServerWorkingCopy swc = s.createWorkingCopy();
		swc.setServerConfiguration(null);
		swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLocation);
		swc.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempDeployLocation);
		IServer server = swc.save(true, null);
		return server;
	}

	public static IServer useMockPublishMethod(IServer server) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.SERVER_MODE, "mock");
		return wc.save(true, new NullProgressMonitor());
	}

	public static IServer useMock2PublishMethod(IServer server) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(IDeployableServer.SERVER_MODE, "mock2");
		return wc.save(true, new NullProgressMonitor());
	}

	public static IServer createMockDeployOnlyServer(String deployLocation, String tempDeployLocation) throws CoreException {
		return createMockDeployOnlyServer(deployLocation, tempDeployLocation, "testRuntime", "testServer");
	}
	public static IServer createMockDeployOnlyServer(String deployLocation, String tempDeployLocation, 
			String rtName, String serverName) throws CoreException {
		return ServerCreationUtils.createDeployOnlyServer(deployLocation, tempDeployLocation, rtName, serverName);
	}

	public static IServer createMockServerWithRuntime(String serverType, String name, String config) {
		try {
			IServerType type = ServerCore.findServerType(serverType);
			if( ServerUtil.isJBoss7(type))
				return createMockJBoss7Server();
			IPath serverDir = createAS6AndBelowMockServerDirectory(name, asSystemJar.get(serverType), config);
			return createServer(serverRuntimeMap.get(serverType), serverType, serverDir.toOSString(), config);
		} catch( CoreException ce ) {
		}
		return null;
	}
	
	public static IServer create32Server() throws CoreException {
		return createServer(IJBossToolingConstants.AS_32, IJBossToolingConstants.SERVER_AS_32, ASTest.JBOSS_AS_32_HOME, DEFAULT_CONFIG);
	}
	public static IServer create40Server() throws CoreException {
		return createServer(IJBossToolingConstants.AS_40, IJBossToolingConstants.SERVER_AS_40, ASTest.JBOSS_AS_40_HOME, DEFAULT_CONFIG);
	}
	public static IServer create42Server() throws CoreException {
		return createServer(IJBossToolingConstants.AS_42, IJBossToolingConstants.SERVER_AS_42, ASTest.JBOSS_AS_42_HOME, DEFAULT_CONFIG);
	}
	public static IServer create50Server() throws CoreException {
		return createServer(IJBossToolingConstants.AS_50, IJBossToolingConstants.SERVER_AS_50, ASTest.JBOSS_AS_50_HOME, DEFAULT_CONFIG);
	}
	
	public static IServer create51Server() throws CoreException {
		return createServer(IJBossToolingConstants.AS_51, IJBossToolingConstants.SERVER_AS_51, ASTest.JBOSS_AS_51_HOME, DEFAULT_CONFIG);
	}
	
	public static IServer create60Server() throws CoreException {
		return createServer(IJBossToolingConstants.AS_60, IJBossToolingConstants.SERVER_AS_60, ASTest.JBOSS_AS_60_HOME, DEFAULT_CONFIG);
	}

	public static IServer create70Server() throws CoreException {
		return createServer(IJBossToolingConstants.AS_70, IJBossToolingConstants.SERVER_AS_70, ASTest.JBOSS_AS_70_HOME, /* irrelevant */ DEFAULT_CONFIG);
	}

	public static IServer createServer(String runtimeID, String serverID,
			String location, String configuration) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(runtimeID, location, configuration);
		return ServerCreationUtils.createServer2(runtime, serverID);
	}
	public static IServer createServer(String runtimeID, String serverID,
			String location, String configuration, IVMInstall install) throws CoreException {
		IRuntime runtime = RuntimeUtils.createRuntime(runtimeID, location, configuration);
		return ServerCreationUtils.createServer2(runtime, serverID);
	}

	public static IRuntime createRuntime(String runtimeId, String homeDir,
			String config) throws CoreException {
		return RuntimeUtils.createRuntime(runtimeId, homeDir, config, VM_INSTALL);
	}
	
	public static IRuntime createRuntime(String runtimeId, String homeDir,
			String config, IVMInstall install) throws CoreException {
		assertTrue("path \"" + homeDir + "\" does not exist", new Path(homeDir).toFile().exists());
		return RuntimeUtils.createRuntime(runtimeId, homeDir, config, install);
	}

	public static IRuntime createRuntime(String runtimeId, String homeDir,
			String config, IExecutionEnvironment environment) throws CoreException {
		assertTrue("path \"" + homeDir + "\" does not exist", new Path(homeDir).toFile().exists());
		return RuntimeUtils.createRuntime(runtimeId, homeDir, config);
	}

	public static void deleteAllServers() throws CoreException {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			servers[i].delete();
		}
	}

	public static void deleteAllRuntimes() throws CoreException {
		IRuntime[] runtimes = ServerCore.getRuntimes();
		for( int i = 0; i < runtimes.length; i++ ) {
			runtimes[i].delete();
		}
	}
	

	
	public static IPath createAS6AndBelowMockServerDirectory(String name, String twiddleJar, String configurationName )  {
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

	public static IPath createAS7StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		try {
			loc.toFile().mkdirs();
			IPath serverJarBelongs = loc.append("modules/org/jboss/as/server/main");
			serverJarBelongs.toFile().mkdirs();
			File serverJarLoc = getFileLocation("serverMock/" + serverJar);
			FileUtil.fileSafeCopy(serverJarLoc, serverJarBelongs.append("anything.jar").toFile());
		} catch(CoreException ce) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		}
		return loc;
	}

	public static IPath createEAP6StyleMockServerDirectory(String name, String serverTypeId, String serverJar) {
		IPath loc = mockedServers.append(name);
		try {
			loc.toFile().mkdirs();
			IPath productConf = loc.append("bin/product.conf");
			loc.append("bin").toFile().mkdirs();
			IOUtil.setContents(productConf.toFile(), "slot=eap");
			loc.append("modules/org/jboss/as/product/eap/dir/META-INF").toFile().mkdirs();
			IPath manifest = loc.append("modules/org/jboss/as/product/eap/dir/META-INF/MANIFEST.MF");
			String manString = "JBoss-Product-Release-Name: EAP\nJBoss-Product-Release-Version: 6.0.0.Alpha\nJBoss-Product-Console-Slot: eap";
			IOUtil.setContents(manifest.toFile(), manString);
		} catch(CoreException ce) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		} catch(IOException ioe) {
			FileUtil.completeDelete(loc.toFile());
			return null;
		}
		return loc;
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
	
	public static String getDeployRoot(IServer server) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		return ds.getDeployFolder();
	}
	
	public static IServer setServerAttribute(IServer server, String attribute, boolean value) {
		ServerAttributeHelper helper = 
			new ServerAttributeHelper(server, server.createWorkingCopy());
		helper.setAttribute(attribute, value);
		return helper.save();
	}
	
	public static IServer addModule(IServer server, IModule module) throws CoreException  {
		IServerWorkingCopy copy = server.createWorkingCopy();
		copy.modifyModules(new IModule[]{module}, new IModule[0], new NullProgressMonitor());
		return copy.save(false, new NullProgressMonitor());
	}

	public static IServer removeModule(IServer server, IModule module) throws CoreException  {
		IServerWorkingCopy copy = server.createWorkingCopy();
		copy.modifyModules(new IModule[]{}, new IModule[] {module}, new NullProgressMonitor());
		return copy.save(false, new NullProgressMonitor());
	}

	public static IStatus publish(IServer server) throws CoreException {
		return publish(IServer.PUBLISH_INCREMENTAL, server);
	}

	public static IStatus publishFull(IServer server) throws CoreException {
		return publish(IServer.PUBLISH_FULL, server);
	}

	public static IStatus publish(int type, IServer server) throws CoreException {
		IStatus s = server.publish(type, new NullProgressMonitor());
		JobUtils.waitForIdle(1000);
		return s;
	}
	
	public static IServer setZipped(IServer server, boolean val) {
		return ServerRuntimeUtils.setServerAttribute(server, IDeployableServer.ZIP_DEPLOYMENTS_PREF, val);
	}

	public static final int DEFAULT_STARTUP_TIME = 150000;
	public static final int DEFAULT_SHUTDOWN_TIME = 90000;
	public static void startup(IServer server) { startup(server, DEFAULT_STARTUP_TIME); }
	public static void startup(final IServer currentServer, int maxWait) {
		long finishTime = new Date().getTime() + maxWait;
		
		// operation listener, which is only alerted when the startup is *done*
		final StatusWrapper opWrapper = new StatusWrapper();
		final IOperationListener listener = new IOperationListener() {
			public void done(IStatus result) {
				opWrapper.setStatus(result);
			} };
			
			
		// a stream listener to listen for errors
		ErrorStreamListener streamListener = new ErrorStreamListener();
		
		// the thread to actually start the server
		Thread startThread = new Thread() { 
			public void run() {
				currentServer.start(ILaunchManager.RUN_MODE, listener);
			}
		};
		
		startThread.start();
		
		boolean addedStream = false;
		while( finishTime > new Date().getTime() && opWrapper.getStatus() == null) {
			// we're waiting for startup to finish
			if( !addedStream ) {
				IStreamMonitor mon = getStreamMonitor(currentServer);
				if( mon != null ) {
					mon.addListener(streamListener);
					addedStream = true;
				}
			}
			try {
				Display.getDefault().readAndDispatch();
			} catch( SWTException swte ) {}
		}
		
		try {
			assertTrue("Startup has taken longer than what is expected for a default startup", finishTime >= new Date().getTime());
			assertNotNull("Startup never finished", opWrapper.getStatus());
			assertFalse("Startup failed", opWrapper.getStatus().getSeverity() == IStatus.ERROR);
			assertFalse("Startup had System.error output", streamListener.hasError());
		} catch( AssertionFailedError afe ) {
			// cleanup
			currentServer.stop(true);
			// rethrow
			throw afe;
		}
		if( getStreamMonitor(currentServer) != null )
			getStreamMonitor(currentServer).removeListener(streamListener);
	}

	
	public static void shutdown(IServer currentServer) { shutdown(currentServer, DEFAULT_SHUTDOWN_TIME); }
	public static void shutdown(final IServer currentServer, int maxWait) {
		long finishTime = new Date().getTime() + maxWait;
		
		// operation listener, which is only alerted when the startup is *done*
		final StatusWrapper opWrapper = new StatusWrapper();
		final IOperationListener listener = new IOperationListener() {
			public void done(IStatus result) {
				opWrapper.setStatus(result);
			} };
			
			
		// a stream listener to listen for errors
		ErrorStreamListener streamListener = new ErrorStreamListener();
		if( getStreamMonitor(currentServer) != null ) 
			getStreamMonitor(currentServer).addListener(streamListener);
		
		// the thread to actually start the server
		Thread stopThread = new Thread() { 
			public void run() {
				currentServer.stop(false, listener);
			}
		};
		
		stopThread.start();
		
		while( finishTime > new Date().getTime() && opWrapper.getStatus() == null) {
			// we're waiting for startup to finish
			try {
				Display.getDefault().readAndDispatch();
			} catch( SWTException swte ) {}
		}
		
		try {
			assertTrue("Startup has taken longer than what is expected for a default startup", finishTime >= new Date().getTime());
			assertNotNull("Startup never finished", opWrapper.getStatus());
			assertFalse("Startup had System.error output", streamListener.hasError());
		} catch( AssertionFailedError afe ) {
			// cleanup
			currentServer.stop(true);
			// rethrow
			throw afe;
		}
	}
	
	protected static class ErrorStreamListener implements IStreamListener {
		protected boolean errorFound = false;
		String entireLog = "";
		public void streamAppended(String text, IStreamMonitor monitor) {
			entireLog += text;
		} 
		
		// will need to be fixed or decided how to figure out errors
		public boolean hasError() {
			return errorFound;
		}
	}

		
	protected static IStreamMonitor getStreamMonitor(IServer server) {
		DelegatingServerBehavior behavior = 
			(DelegatingServerBehavior)server.loadAdapter(DelegatingServerBehavior.class, null);
		if( behavior != null ) {
			if( ((IProcessProvider)behavior.getDelegate()).getProcess() != null ) {
				return ((IProcessProvider)behavior.getDelegate()).getProcess().getStreamsProxy().getOutputStreamMonitor();
			}
		}
		return null;
	}
	
	public static class StatusWrapper {
		protected IStatus status;
		public IStatus getStatus() { return this.status; }
		public void setStatus(IStatus s) { this.status = s; }
	}



}
