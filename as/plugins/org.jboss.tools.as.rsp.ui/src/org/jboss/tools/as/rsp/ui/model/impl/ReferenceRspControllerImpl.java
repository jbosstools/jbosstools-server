/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.model.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.IRspStartCallback;
import org.jboss.tools.as.rsp.ui.model.IRspStateController;
import org.jboss.tools.as.rsp.ui.model.IRspType;
import org.jboss.tools.as.rsp.ui.model.ServerConnectionInfo;
import org.jboss.tools.as.rsp.ui.model.StartupFailedException;
import org.jboss.tools.as.rsp.ui.util.JavaUtils;
import org.jboss.tools.as.rsp.ui.util.PortFinder;
import org.jboss.tools.as.rsp.ui.util.ProcessMonitorThread;

/**
 * Provides the logic to start and stop a reference-implementation-based RSP
 * including launching the felix command with flags for logging, port, data
 * directory, and data locking.
 */
public class ReferenceRspControllerImpl implements IRspStateController {
	private IRspType serverType;
	private int portMin;
	private int portMax;

	private Process runningProcess;

	public ReferenceRspControllerImpl(IRspType rspServerType, int portMin, int portMax) {
		this.serverType = rspServerType;
		this.portMin = portMin;
		this.portMax = portMax;
	}

	@Override
	public ServerConnectionInfo start(IRspStartCallback callback) throws StartupFailedException {
		String rspHome = serverType.getServerHome();
		File rspHomeFile = new File(rspHome);
		if (!rspHomeFile.exists() || !rspHomeFile.isDirectory())
			throw new StartupFailedException("RSP does not appear to be installed.");

		File felixFile = new File(new File(rspHomeFile, "bin"), "felix.jar");
		if (!felixFile.exists() || !felixFile.isFile())
			throw new StartupFailedException(
					"RSP does not appear to be installed or is broken. Please use the Download / Update RSP action.");

		int port = new PortFinder().nextFreePort(portMin, portMax);
		if (port == -1)
			throw new StartupFailedException("No free port within the defined range found.");

		File java = JavaUtils.findJavaExecutable();
		if (java == null || !java.exists())
			throw new StartupFailedException("A java executable could not be located on this system.");

		String portInUse = getLockedWorkspacePort();
		if (portInUse != null) {
			callback.updateRspState(IRspCore.IJServerState.STARTED, false);
			return new ServerConnectionInfo("localhost", Integer.parseInt(portInUse));
		}
		Process p = startRSP(rspHome, port, java, callback);
		if (p != null) {
			setRunningProcess(p);
			boolean started = waitForPortInUse(port);
			if (started) {
				callback.updateRspState(IRspCore.IJServerState.STARTED, true);
				return new ServerConnectionInfo("localhost", port);
			} else {
				terminate(callback);
				throw new StartupFailedException("Unable to connect to RSP after startup.");
			}
		}
		return null;
	}

	private synchronized void setRunningProcess(Process p) {
		this.runningProcess = p;
	}

	private synchronized Process getRunningProcess() {
		return this.runningProcess;
	}

	private boolean waitForPortInUse(int port) {
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() < (time + 60000)) {
			if (!PortFinder.isLocalPortFree(port))
				return true;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				// Ignore
			}
		}
		return false;
	}

	private Process startRSP(String rspHome, int port, File java, IRspStartCallback callback) {
		callback.updateRspState(IRspCore.IJServerState.STARTING);
		File workingDir = new File(rspHome);
		File felix = new File(new File(workingDir, "bin"), "felix.jar");

		String cmd = java.getAbsolutePath();
		String portFlag = "-Drsp.server.port=" + port;
		String id = "-Dorg.jboss.tools.rsp.id=" + serverType.getId();
		String logbackFlag = "-Dlogback.configurationFile=./conf/logback.xml";
		String jar = "-jar";

		String[] cmdArr = new String[] { cmd, portFlag, id, logbackFlag, jar, felix.getAbsolutePath() };
		try {
			Process p = Runtime.getRuntime().exec(cmdArr, convertEnvironment(System.getenv()),
					new File(workingDir.getPath()));
			ProcessMonitorThread pmt = new ProcessMonitorThread(p, (Process proc9) -> {
				callback.updateRspState(IRspCore.IJServerState.STOPPED);
				setRunningProcess(null);
			});
			pmt.start();
			return p;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Convert a string/string hashmap into an array of string environment variables
	 * as required by java.lang.Runtime This will super-impose the provided
	 * environment variables ON TOP OF the existing environment in eclipse, as users
	 * may not know *all* environment variables that need to be set, or to do so may
	 * be tedious.
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

	@Override
	public void terminate(IRspStartCallback callback) {
		Process p = getRunningProcess();
		if (p != null)
			p.destroy();
		setRunningProcess(null);
		callback.updateRspState(IRspCore.IJServerState.STOPPED);
	}

	private String getLockedWorkspacePort() {
		File lockFile = getLockFile();
		if (!lockFile.exists())
			return null;
		Path p = lockFile.toPath();
		String portInUse = null;
		try {
			portInUse = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
		} catch (IOException e) {
		}
		if (portInUse == null || portInUse.isEmpty())
			return null;
		if (PortFinder.isLocalPortFree(Integer.parseInt(portInUse))) {
			lockFile.delete();
			return null;
		}
		return portInUse;
	}

	private File getLockFile() {
		String userHome = JavaUtils.getUserHome();
		return new File(userHome).toPath().resolve(".rsp").resolve(serverType.getId()).resolve(".lock").toFile();
	}

}
