package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.io.IOException;

import junit.framework.Assert;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.internal.management.as71.AS71Manager;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.ide.eclipse.as.test.server.JBossManagerTest.MockAS7ManagementDetails;

public class StartupUtility extends Assert {
	public static Process runServer(String homeDir) {
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
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			return p;
		} catch( IOException ioe) {
			fail(homeDir + " server failed to start: " + ioe.getMessage());
		}
		return null;
	}
	
	private String homeDir;
	private Process process;
	private boolean started = false;
	public StartupUtility() {
	}
	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}
	public String getHomeDir() {
		return homeDir;
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
			} catch( IllegalThreadStateException itse) {
				// Process has not yet exited, so shutdown the server
				shutdownServer(process);
			}
		}
		started = false;
	}
	
	private void waitForStarted(Process p) {
		Exception ex = null;
		AS71Manager manager = null;
		try {
			Thread.sleep(3000);
		} catch(InterruptedException ie){}
		
		try {
			manager = new AS71Manager( new MockAS7ManagementDetails(
					AS7ManagerTestUtils.LOCALHOST, AS71Manager.MGMT_PORT));
			JBoss7ServerState state = null;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + (1000*60);
			while( state != JBoss7ServerState.RUNNING && System.currentTimeMillis() < endTime) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException ie){}
				try {
					state = manager.getServerState(); 
					ex = null;
				} catch(JBoss7ManangerException ioe) {
					ex = ioe;
				}
			}
		} catch(Exception e) {
			ex = e;
		} finally {
			if( manager != null )
				manager.dispose();
			if( ex != null ) {
				if( p != null)
					p.destroy();
				if( ex != null )
					fail("Could not stop server " + homeDir + ": " + ex.getMessage());
			}
		}
	}
		
	private void shutdownServer(Process p) {
		AS71Manager manager = null;
		Exception ex = null;
		try {
			boolean isListening = (AS7ManagerTestUtils.isListening(
					AS7ManagerTestUtils.LOCALHOST, AS71Manager.MGMT_PORT));
			assertTrue(isListening);
			manager = new AS71Manager( new MockAS7ManagementDetails(
					AS7ManagerTestUtils.LOCALHOST, AS71Manager.MGMT_PORT));
			manager.stopServer();
			ThreadUtils.sleepFor(3000);
			isListening = (AS7ManagerTestUtils.isListening(
					AS7ManagerTestUtils.LOCALHOST, AS71Manager.MGMT_PORT));
			assertFalse(isListening);
		} catch(Exception e) {
			ex = e;
		} finally {
			if( manager != null )
				manager.dispose();
			if( p != null )
				p.destroy();
			if( ex != null )
				fail("Could not stop server " + homeDir + ": " + ex.getMessage());
		}
	}
	
}
