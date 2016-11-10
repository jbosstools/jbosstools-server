package org.jboss.tools.as.management.itests.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils.MockAS7ManagementDetails;

import junit.framework.Assert;

public class StartupUtility extends Assert {
	private static final String JRE7_SYSPROP = "jbosstools.test.jre.7";
	private static final String JRE8_SYSPROP = "jbosstools.test.jre.8";

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

	private static boolean requiresJava8(String runtimeType) {
		return "JavaSE-1.8".equals(getRequiredExecEnv(runtimeType).getId());
	}
	private static boolean requiresJava7(String runtimeType) {
		return "JavaSE-1.7".equals(getRequiredExecEnv(runtimeType).getId());
	}
	private static boolean requiresJava6(String runtimeType) {
		return "JavaSE-1.6".equals(getRequiredExecEnv(runtimeType).getId());
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
		envp.add("JAVA_HOME=" + getJavaHome(rtType, homeDir));
		
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
	
	private static String getJavaHome(String rtType, String serverHome) {
		// We have some with java8 requirements. 
		ServerBeanLoader sb = new ServerBeanLoader(new File(serverHome));
		String version = sb.getFullServerVersion();
		System.out.println("**** ____  server version is " + version);
		if( requiresJava8(rtType)) {
			return getJavaHome(serverHome, JRE8_SYSPROP, "JavaSE-1.8");
		}
		// For all older, use j7
		return getJavaHome(serverHome, JRE7_SYSPROP, "JavaSE-1.7");
	}
	
	private static String getJavaHome(String serverHome, String sysprop, String javaVersion) {
		String java = System.getProperty(sysprop);
		if( java == null ) {
			fail("Launching " + serverHome + " requires a " + javaVersion + ", which has not been provided via the " + sysprop + " system property");
		}

		if( !new File(java).exists()) {
			fail("Java Home " + java + " provided by the " + sysprop + " system property does not exist.");
		}

		return java;
	}
	

	private String homeDir, runtimeType;
	private Process process;
	private boolean started = false;
	private int port;
	
	public StartupUtility() {
	}
	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
		setRuntimeType(ParameterUtils.serverHomeToRuntimeType.get(homeDir));
		
		String javaHome = getJavaHome(getRuntimeType(), homeDir);
		
		setPort(ManagementPortTestUtility.getManagementPort(homeDir, javaHome));
		assertTrue("Management port for server " + homeDir + " must not be -1", getPort() != -1);
	}
	protected void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return port;
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
	
	String out = null;
	String err = null;
	
	public void start(boolean wait) {
		out = "";
		err = "";
		process = runServer(homeDir);
		addDebugging(process);
		
		if( wait )
			waitForStarted(process);
		started = true;
	}
	
	private void addDebugging(Process process) {
		final InputStream inStream = process.getInputStream();
		final InputStream errStream = process.getErrorStream();
		new Thread("startupUtility_out") {
			public void run() {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				try {
					int nRead;
					byte[] data = new byte[16384];
	
					while ((nRead = inStream.read(data, 0, data.length)) != -1) {
					  buffer.write(data, 0, nRead);
					}
					buffer.flush();
				} catch(IOException ioe) {}
				StartupUtility.this.out = buffer.toString();
			}
		}.start();

		new Thread("startupUtility_err") {
			public void run() {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				try {
					int nRead;
					byte[] data = new byte[16384];
	
					while ((nRead = errStream.read(data, 0, data.length)) != -1) {
					  buffer.write(data, 0, nRead);
					}
					buffer.flush();
				} catch(IOException ioe) {}
				StartupUtility.this.err = buffer.toString();
			}
		}.start();

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
				boolean alive = process.isAlive();
				if( !alive ) {
					System.out.println("Output:\n" + out);
					System.out.println("Errors:\n" + err);
					fail("Server prematurely terminated while waiting to complete startup: " + homeDir + " " );
				}
				try {
					Thread.sleep(1000);
				} catch(InterruptedException ie){}
				try {
					state = service.getServerState(createConnectionDetails());
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
					AS7ManagerTestUtils.LOCALHOST, getPort()));
			assertTrue(isListening);
			service.stop(createConnectionDetails());
			ThreadUtils.sleepFor(3000);
			isListening = (AS7ManagerTestUtils.isListening(
					AS7ManagerTestUtils.LOCALHOST, getPort()));
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

	protected IAS7ManagementDetails createConnectionDetails() {
		return new MockAS7ManagementDetails(AS7ManagerTestUtils.LOCALHOST, getPort());
	}
	
}
