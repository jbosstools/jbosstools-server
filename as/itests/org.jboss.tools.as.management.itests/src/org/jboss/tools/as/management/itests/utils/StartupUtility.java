package org.jboss.tools.as.management.itests.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerServiceProxy;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.tools.as.management.itests.utils.AS7ManagerTestUtils.MockAS7ManagementDetails;
import org.junit.Assert;

public class StartupUtility extends Assert {

	public static Process runServer(String homeDir) {
		String rtType = ParameterUtils.serverHomeToRuntimeType.get(homeDir);
		System.out.println("Running server " + homeDir + " with rtType = " + rtType);
		String scriptName = null;
		String cmd = null;
		IPath bin = new Path(homeDir).append("bin");
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			scriptName = "standalone.bat";
			IPath script = bin.append(scriptName);
			cmd = script.toFile().getAbsolutePath();
		} else {
			scriptName = "standalone.sh";
			IPath script = bin.append(scriptName);
			script.toFile().setExecutable(true);
			cmd = script.toFile().getAbsolutePath();
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("JAVA_HOME", JREParameterUtils.getJavaHome(rtType, homeDir));

		String[] envList = convertEnvironment(map);
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd, envList);
			return p;
		} catch (IOException ioe) {
			fail(homeDir + " server failed to start: " + ioe.getMessage());
		}
		System.out.println("Somehow launch completed without returning a process");
		return null;
	}

	/*
	 * Convert a string/string hashmap into an array of string environment
	 * variables as required by java.lang.Runtime This will super-impose the
	 * provided environment variables ON TOP OF the existing environment in
	 * eclipse, as users may not know *all* environment variables that need to
	 * be set, or to do so may be tedious.
	 */
	public static String[] convertEnvironment(Map<String, String> env) {
		if (env == null || env.size() == 0)
			return null;

		// Create a new map based on pre-existing environment of Eclipse
		Map<String, String> original = new HashMap<>(System.getenv());

		// Add additions or changes to environment on top of existing
		original.putAll(env);

		// Convert the combined map into a form that can be used to launch
		// process
		ArrayList<String> ret = new ArrayList<>();
		Iterator<String> it = original.keySet().iterator();
		String working = null;
		while (it.hasNext()) {
			working = it.next();
			ret.add(working + "=" + original.get(working)); //$NON-NLS-1$
		}
		return ret.toArray(new String[ret.size()]);
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

		String javaHome = JREParameterUtils.getJavaHome(getRuntimeType(), homeDir);

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
		this.runtimeType = runtimeType;
	}

	public String getRuntimeType() {
		return runtimeType;
	}

	public void startIfNotStarted(boolean wait) {
		if (!started)
			start(wait);
	}

	String out = null;
	String err = null;

	public void start(boolean wait) {
		out = "";
		err = "";
		process = runServer(homeDir);
		addDebugging(process);

		if (wait)
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
				} catch (IOException ioe) {
				}
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
				} catch (IOException ioe) {
				}
				StartupUtility.this.err = buffer.toString();
			}
		}.start();

	}

	public void dispose() {
		if (homeDir != null && process != null) {
			try {
				process.exitValue();
				// Process has exited already, so no need to shutdown server
				System.out.println("Server has already shutdown");
			} catch (IllegalThreadStateException itse) {
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
		} catch (InterruptedException ie) {
		}

		try {
			JBoss7ServerState state = null;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + (1000 * 60);
			while (state != JBoss7ServerState.RUNNING && System.currentTimeMillis() < endTime) {
				System.out.println("    in while loop, waiting for app server to start");
				boolean alive = process.isAlive();
				if (!alive) {
					System.out.println("Output:\n" + out);
					System.out.println("Errors:\n" + err);
					fail("Server prematurely terminated while waiting to complete startup: " + homeDir + " ");
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
				try {
					state = service.getServerState(createConnectionDetails());
					ex = null;
				} catch (JBoss7ManangerException ioe) {
					ex = ioe;
				}
			}
		} catch (Exception e) {
			ex = e;
		} finally {
			if (service != null)
				service.dispose();
			if (ex != null) {
				if (p != null)
					p.destroyForcibly();
				if (ex != null) {
					ex.printStackTrace();
					fail("Could not correctly discover if server has started: " + homeDir + ": " + ex.getMessage());
					System.out.println("Output:\n" + out);
					System.out.println("Errors:\n" + err);
				}
			}
		}
	}

	private void shutdownServer(Process p) {
		IJBoss7ManagerService service = AS7ManagerTestUtils.findService(runtimeType);
		Exception ex = null;
		try {
			// Counting the number of running java processes is a workaround 
			// for windows. Since we're launching via the .bat file, 
			// Even though we're stopping via mgmt, the server itself stops, but, 
			// the process that was launched is actually a conhost.exe, which
			// doesn't terminate even when the java process terminates. (NO IDEA WHY). 
			// So we revert to counting java processes in the before and after case.
			int preStop = countJavaProcesses();
			boolean isListening = (AS7ManagerTestUtils.isListening(AS7ManagerTestUtils.LOCALHOST, getPort()));
			if (isListening) {
				// Some output
				System.out.println("Trying to stop runtimeType=" + runtimeType);
				JBoss7ManagerServiceProxy service2 = (JBoss7ManagerServiceProxy) service;
				IJBoss7ManagerService deleg = service2.getService();
				System.out.println("Service class: " + (deleg == null ? "null" : deleg.getClass().getName()));

				service.stop(createConnectionDetails());

				boolean terminated = waitForTermination(p, 15000);
				isListening = (AS7ManagerTestUtils.isListening(AS7ManagerTestUtils.LOCALHOST, getPort()));
				assertFalse(isListening);
				int postStop = countJavaProcesses();

				if (!terminated) {
					// It's not listening. Either it's frozen or it was started incorrectly or something 
					// Or it's on windows, and the batch file itself is still running for some reason
					p.destroyForcibly();
					boolean t2 = waitForTermination(p, 6000);
					int postStop2 = countJavaProcesses();
					
					// Not implemented for linux, and since linux behaves properly, 
					// we don't need to keep count and can just assume this has failed
					boolean supportCountingProcs = !(preStop == -1 && postStop == -1);
					boolean stopHadEffect = preStop > postStop;
					boolean terminateHadEffect = postStop != postStop2; // Terminating did something to java procs count
					if (!stopHadEffect || terminateHadEffect || !supportCountingProcs) {
						fail("Shutdown of server at " + getHomeDir() + " did not work. The process has been killed instead. Termination "+ (t2 ? "succeeded" : "failed."));
					}
				}
			} else {
				// It's not listening. Either it's frozen or it was started incorrectly or something
				p.destroyForcibly();
				boolean terminated = waitForTermination(p, 15000);
				fail("Shutdown of server at " + getHomeDir() + " did not work. The process has been killed instead. Termination " + (terminated ? "succeeded" : "failed."));
			}
		} catch (Exception e) {
			ex = e;
		} finally {
			if (service != null)
				service.dispose();
			if (p != null)
				p.destroy();
			if (ex != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				fail("Could not stop server " + homeDir + ": " + ex.getMessage() + "\n" + sStackTrace);
			}
		}
	}
	
	
	protected int countJavaProcesses() {
		if( Platform.getOS().equals(Platform.OS_WIN32)) {
			return countJavaProcessesWin();
		}
		return countJavaProcessesNix();
	}

	protected int countJavaProcessesNix() {
		return -1;
	}
	
	protected int countJavaProcessesWin() {
		final int[] result = new int[] {0};
		Process process;
		try {
			process = new ProcessBuilder("tasklist.exe", "/fo", "csv", "/nh").start();
			new Thread(() -> {
				Scanner sc = new Scanner(process.getInputStream());
				if (sc.hasNextLine())
					sc.nextLine();
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					String[] parts = line.split(",");
					String unq = parts[0].substring(1).replaceFirst(".$", "");
					String pid = parts[1].substring(1).replaceFirst(".$", "");
					if( unq.toLowerCase().startsWith("java.exe") || unq.toLowerCase().startsWith("javaw.exe")) {
						result[0]++;
					}
				}
			}).start();
			try {
				process.waitFor();
			} catch(InterruptedException ie) {}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result[0];
	}

	protected boolean waitForTermination(Process p, int timeout) {
		long l = System.currentTimeMillis();
		long end = l + timeout;
		while(System.currentTimeMillis() < end ) {
			try {
				p.exitValue();
				return true;
			} catch( IllegalThreadStateException itse ) {
				// ignore
			}
			try {
				Thread.sleep(200);
			} catch(InterruptedException ie) {
				// ignore
			}
		}
		return false;
	}
	
	protected IAS7ManagementDetails createConnectionDetails() {
		return new MockAS7ManagementDetails(AS7ManagerTestUtils.LOCALHOST, getPort(), homeDir);
	}

}
