package org.jboss.tools.as.management.itests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class VerifyJREFlagsTest extends TestCase {
	private static final String JRE7_SYSPROP = "jbosstools.test.jre.7";
	private static final String JRE8_SYSPROP = "jbosstools.test.jre.8";
	
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testJava7HomeSet() throws RuntimeException {
		String j7 = getJava7Home();
		if( j7 == null ) {
			fail("Sysprop  " + JRE7_SYSPROP + " must be set.");
		}

		String cmdName = (Platform.getOS().equals(Platform.OS_WIN32) ? "java.exe" : "java");
		String j7Bin = new Path(j7).append("bin").append(cmdName).toOSString();
		assertTrue("java 7 binary does not exist", new File(j7Bin).exists());
		try {
			String[] j7Lines = call(j7Bin, new String[] {"-version"}, 
					ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(),
					new HashMap<String,String>(), 10000, SYSERR);
			
			String j7v = getVersion(j7Lines);
			assertNotNull("Unable to verify java7 version", j7v);
			assertTrue("Java 7 version string must start with 1.7. Please verify sysprop " + JRE7_SYSPROP + ". Current version: " + j7v, j7v.startsWith("1.7."));
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	@Test
	public void testJava8HomeSet() throws RuntimeException {
		
		String cmdName = (Platform.getOS().equals(Platform.OS_WIN32) ? "java.exe" : "java");
		String j8 = getJava8Home();
		if( j8 == null ) {
			fail("Sysprop  " + JRE8_SYSPROP + " must be set.");
		}
		String j8Bin = new Path(j8).append("bin").append(cmdName).toOSString();

		assertTrue("java 8 binary does not exist", new File(j8Bin).exists());
		
		try {
			String[] j8Lines = call(j8Bin, new String[] {"-version"}, 
					ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(),
					new HashMap<String,String>(), 10000, SYSERR);
			
			String j8v = getVersion(j8Lines);
			assertNotNull("Unable to verify java7 version", j8v);
			assertTrue("Java 7 version string must start with 1.7: " + j8v, j8v.startsWith("1.8."));
			System.out.println("java 8 version: " + getVersion(j8Lines));
		} catch(Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	public static String getJava8Home() {
		String java = System.getProperty(JRE8_SYSPROP);
		return java;
	}
	
	public static String getJava7Home() {
		String java = System.getProperty(JRE7_SYSPROP);
		return java;
	}

	
	public String getVersion(String[] arr) {
		if( arr == null || arr.length == 0 )
			return null;
		String lineOne = arr[0];
		String fromQuote = lineOne.substring(lineOne.indexOf("\"")).trim();
		return fromQuote.substring(1, fromQuote.length() - 1);
	}
	
	public static Process callProcess(String rootCommand, String[] args, File vagrantDir, Map<String, String> env) throws IOException {
		List<String> cmd = new ArrayList<>();
		cmd.add(rootCommand);
		cmd.addAll(Arrays.asList(args));
		String[] asArr = (String[]) cmd.toArray(new String[cmd.size()]);
		Process p = null;
		p = Runtime.getRuntime().exec(asArr, null, vagrantDir);
		return p;
	}

	public static boolean SYSOUT = true;
	public static boolean SYSERR = false;
	
	public static String[] call(String rootCommand, String[] args, File vagrantDir, 
			Map<String, String> env, int timeout) throws IOException, CommandTimeoutException {
		return call(rootCommand, args, vagrantDir, env, timeout, SYSOUT);
	}
	
	public static String[] call(String rootCommand, String[] args, File vagrantDir, 
			Map<String, String> env, int timeout, boolean stream) throws IOException, CommandTimeoutException {
	
	
		final Process p = callProcess(rootCommand, args, vagrantDir, env);

		InputStream errStream = p.getErrorStream();
		InputStream inStream = p.getInputStream();

		StreamGobbler inGob = new StreamGobbler(inStream);
		StreamGobbler errGob = new StreamGobbler(errStream);
		
		inGob.start();
		errGob.start();
		
		Integer exitCode = null;
		if( p.isAlive()) {
		
			exitCode = runWithTimeout(timeout, new Callable<Integer>() {
				@Override
			 	public Integer call() throws Exception {
					return p.waitFor();
				}
			});
		} else {
			exitCode = p.exitValue();
		}
		inGob.waitComplete(100, 5000);
		errGob.waitComplete(100, 5000);
		List<String> retLines = null;
		if( exitCode == null ) {
			inGob.cancel();
			errGob.cancel();
			
			// Timeout reached
			p.destroyForcibly();
			List<String>  inLines = inGob.getOutput();
			List<String> errLines = errGob.getOutput();
			throw new CommandTimeoutException(inLines, errLines);
		}
		
		if( stream == SYSOUT)
			retLines = inGob.getOutput();
		else
			retLines = errGob.getOutput();
		
		return (String[]) retLines.toArray(new String[retLines.size()]);
	}
	
	private static class StreamGobbler extends Thread {
		InputStream is;
		ArrayList<String> ret = new ArrayList<String>();
		private boolean canceled = false;
		private boolean complete = false;
		public StreamGobbler(InputStream is) {
			this.is = is;
		}
		private synchronized void add(String line) {
			ret.add(line);
		}
		private synchronized ArrayList<String> getList() {
			return ret;
		}
		
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while (!isCanceled() && (line = br.readLine()) != null)
					add(line);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			if( is != null ) {
				try {
					is.close();
				} catch(IOException ioe) {
					// ignore
				}
			}
			setComplete();
		}
		
		private synchronized void setComplete() {
			complete = true;
		}
		private synchronized boolean isComplete() {
			return complete;
		}
		private synchronized void setCanceled() {
			canceled = true;
		}
		private synchronized boolean isCanceled() {
			return canceled;
		}
		public void cancel() {
			setCanceled();
			if( is != null ) {
				try {
					is.close();
				} catch(IOException ioe) {
					// ignore
				}
			}
		}
		
		private void waitComplete(long delay, long maxwait) {
			long start = System.currentTimeMillis();
			long end = start + maxwait;
			while( !isComplete() && System.currentTimeMillis() < end) {
				try {
					Thread.sleep(delay);
				} catch(InterruptedException ie) {
					
				}
			}
			if( !isComplete()) {
				cancel();
			}
		}
		

		private static final long MAX_WAIT_AFTER_TERMINATION = 5000;
		private static final long DELAY = 100;
		/**
		 * Wait a maximum 5 seconds for the streams to finish reading whatever is in the pipeline
		 * @return
		 */
		public List<String> getOutput() {
			waitComplete(DELAY, MAX_WAIT_AFTER_TERMINATION);
			return getList();
		}
	}
	
	public static class CommandTimeoutException extends TimeoutException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 37067116146846743L;
		private static String getTimeoutError(List<String> output, List<String> err) {
			StringBuilder msg = new StringBuilder();
			msg.append("Process output:\n");
			output.forEach(line -> msg.append("   ").append(line));
			err.forEach(line -> msg.append("   ").append(line));
			return msg.toString();
		}
		
		private List<String> inLines;
		private List<String> errLines;
		public CommandTimeoutException(List<String> inLines, List<String> errLines) {
			super(getTimeoutError(inLines, errLines));
			this.inLines = inLines;
			this.errLines = errLines;
		}
		public List<String> getInLines() {
			return inLines;
		}
		public List<String> getErrLines() {
			return errLines;
		}
		
	}
	public static <R> R runWithTimeout(long millisTimeout, Callable<R> callable) {
		ExecutorService singleThreadExecutor = Executors.newFixedThreadPool(1);
		Future<R> future = singleThreadExecutor.submit(callable);
		try {
			return future.get(millisTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
		} finally {
			singleThreadExecutor.shutdown();
		}
		return null;
	}
}
