/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.management.itests.utils.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandRunner {

	public static boolean SYSOUT = true;
	public static boolean SYSERR = false;
	
	public static Process callProcess(String rootCommand, String[] args, File vagrantDir, Map<String, String> env)
			throws IOException {
		List<String> cmd = new ArrayList<>();
		cmd.add(rootCommand);
		cmd.addAll(Arrays.asList(args));
		String[] asArr = (String[]) cmd.toArray(new String[cmd.size()]);
		Process p = null;
		p = Runtime.getRuntime().exec(asArr, null, vagrantDir);
		return p;
	}


	public static String[] call(String rootCommand, String[] args, File vagrantDir, Map<String, String> env,
			int timeout) throws IOException, CommandTimeoutException {
		return call(rootCommand, args, vagrantDir, env, timeout, SYSOUT);
	}

	public static String[] call(String rootCommand, String[] args, File vagrantDir, Map<String, String> env,
			int timeout, boolean stream) throws IOException, CommandTimeoutException {

		final Process p = callProcess(rootCommand, args, vagrantDir, env);

		InputStream errStream = p.getErrorStream();
		InputStream inStream = p.getInputStream();

		StreamGobbler inGob = new StreamGobbler(inStream);
		StreamGobbler errGob = new StreamGobbler(errStream);

		inGob.start();
		errGob.start();

		Integer exitCode = null;
		if (p.isAlive()) {

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
		if (exitCode == null) {
			inGob.cancel();
			errGob.cancel();

			// Timeout reached
			p.destroyForcibly();
			List<String> inLines = inGob.getOutput();
			List<String> errLines = errGob.getOutput();
			throw new CommandTimeoutException(inLines, errLines);
		}

		if (stream == SYSOUT)
			retLines = inGob.getOutput();
		else
			retLines = errGob.getOutput();

		return (String[]) retLines.toArray(new String[retLines.size()]);
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

}
