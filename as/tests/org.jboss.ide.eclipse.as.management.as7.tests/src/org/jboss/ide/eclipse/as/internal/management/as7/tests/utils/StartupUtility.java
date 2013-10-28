package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;

public class StartupUtility extends Assert {
	private static final String JRE7_SYSPROP = "jbosstools.test.jre.7";


	private static boolean isJava7() {
		return (System.getProperty("java.version").startsWith("1.7."));
	}

	private static IExecutionEnvironment getRequiredExecEnv(String runtimeType) {
		IRuntimeType type = ServerCore.findRuntimeType(runtimeType);
		System.out.println("getRequiredExecenv for " + type.getId());
		ServerExtendedProperties o = new ExtendedServerPropertiesAdapterFactory().getExtendedProperties(type);
		IExecutionEnvironment env = ((JBossExtendedProperties)o).getDefaultExecutionEnvironment();
		return env;
	}

	private static boolean requiresJava7(String runtimeType) {
		return "JavaSE-1.7".equals(getRequiredExecEnv(runtimeType).getId());
	}

	public static Process runServer(String homeDir) {
		String rtType = ParameterUtils.serverHomeToRuntimeType.get(homeDir);
		System.out.println("Running server " + homeDir + " with rtType = " + rtType);
		String scriptName = null;
		String cmd = null;
		IPath bin = new Path(homeDir).append("bin");
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			scriptName = "standalone.bat";
			IPath script = bin.append(scriptName);
			fail("These tests cannot run on windows currently");
		} else {
			scriptName = "standalone.sh";
			IPath script = bin.append(scriptName);
			script.toFile().setExecutable(true);
			cmd = script.toFile().getAbsolutePath();
		}

		List<String> envp = new ArrayList<String>();
		if( !isJava7() && requiresJava7(rtType)) {
			String java = System.getProperty(JRE7_SYSPROP);
			if( java == null || !new File(java).exists()) {
				fail("Launching " + homeDir + " requires a java7 jdk, which has not been provided via the " + JRE7_SYSPROP + " system property, or does not exist");
			}
			envp.add("JAVA_HOME=" + java);
		}
		
		String[] envList = (String[]) envp.toArray(new String[envp.size()]);
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd, envList);
			return p;
		} catch( IOException ioe) {
			fail(homeDir + " server failed to start: " + ioe.getMessage());
		}
		System.out.println("Somehow launch completed without returning a process");
		return null;
	}

	private String homeDir, runtimeType;
	private Process process;
	private boolean started = false;
	public StartupUtility() {
	}
	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
		setRuntimeType(ParameterUtils.serverHomeToRuntimeType.get(homeDir));
	}
	public String getHomeDir() {
		return homeDir;
	}
	public void setRuntimeType(String runtimeType) {
		this.runtimeType= runtimeType;
	}
	public String getRuntimeType() {
		return runtimeType;
	}
	public void startIfNotStarted(boolean wait) {
		if( !started )
			start(wait);
	}
	public void start(boolean wait) {
		process = runServer(homeDir);
		if( wait )
			waitForStarted(process);
		started = true;
	}

	public void dispose() {
		if( homeDir != null && process != null ) {
			try {
				process.exitValue();
				// Process has exited already, so no need to shutdown server
				System.out.println("Server has already shutdown");
			} catch( IllegalThreadStateException itse) {
				// Process has not yet exited, so shutdown the server
				System.out.println("Shutting down server");
				shutdownServer(process);
			}
		}
		started = false;
	}

	private void waitForStarted(Process p) {
		System.out.println("Waiting for server to complete startup");
		Exception ex = null;
		IJBoss7ManagerService service = AS7ManagerTestUtils.findService(runtimeType);
		try {
			Thread.sleep(3000);
		} catch(InterruptedException ie){}

		try {
			JBoss7ServerState state = null;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + (1000*60);
			while( state != JBoss7ServerState.RUNNING && System.currentTimeMillis() < endTime) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException ie){}
				try {
					state = service.getServerState(AS7ManagerTestUtils.createStandardDetails());
					ex = null;
				} catch(JBoss7ManangerException ioe) {
					ex = ioe;
				}
			}
		} catch(Exception e) {
			ex = e;
		} finally {
			if( service != null )
				service.dispose();
			if( ex != null ) {
				if( p != null)
					p.destroy();
				if( ex != null )
					fail("Could not correctly discover if server has started: " + homeDir + ": " + ex.getMessage());
			}
		}
	}

	private void shutdownServer(Process p) {
		IJBoss7ManagerService service = AS7ManagerTestUtils.findService(runtimeType);
		Exception ex = null;
		try {
			boolean isListening = (AS7ManagerTestUtils.isListening(
					AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.MGMT_PORT));
			assertTrue(isListening);
			service.stop(AS7ManagerTestUtils.createStandardDetails());
			ThreadUtils.sleepFor(3000);
			isListening = (AS7ManagerTestUtils.isListening(
					AS7ManagerTestUtils.LOCALHOST, AS7ManagerTestUtils.MGMT_PORT));
			assertFalse(isListening);
		} catch(Exception e) {
			ex = e;
		} finally {
			if( service != null )
				service.dispose();
			if( p != null )
				p.destroy();
			if( ex != null )
				fail("Could not stop server " + homeDir + ": " + ex.getMessage());
		}
	}

}
