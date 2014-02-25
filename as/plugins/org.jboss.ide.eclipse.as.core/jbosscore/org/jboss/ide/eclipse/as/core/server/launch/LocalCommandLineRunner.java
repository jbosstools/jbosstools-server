/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.launch;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

public class LocalCommandLineRunner {
	
	public static final String PROCESS_TYPE = "LocalCommandLine"; //$NON-NLS-1$
	
	public IProcess launchCommand(String command, IProgressMonitor monitor) throws CoreException {
		Launch l = new Launch(null, "run", null); //$NON-NLS-1$
		return launchCommand(command, l, monitor);
	}

	public IProcess launchCommand(String command, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		Process p = createProcessForCommand(command, monitor);
		if( p != null ) {
			String[] cmdLine = new String[]{command};
			return getIProcessForProcess(cmdLine, launch, null,null, p);
		}
		return null;
	}

	private Process createProcessForCommand(String cmdLine, IProgressMonitor monitor) throws CoreException {
		Process p = exec(cmdLine);
		if (p == null) {
			return null;
		}
		
		// check for cancellation
		if (monitor.isCanceled()) {
			p.destroy();
			return null;
		}		
		return p;
	}
	
	private IProcess getIProcessForProcess(String[] cmdLine, ILaunch launch, File workingDir, String[] envp, Process p) throws CoreException {
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
		IProcess process= newProcess(launch, p, renderProcessLabel(cmdLine, timestamp), getDefaultProcessMap());
		process.setAttribute(DebugPlugin.ATTR_PATH, cmdLine[0]);
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
		String ltime = launch == null ? null : launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		process.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, ltime != null ? ltime : timestamp);
		if(workingDir != null) {
			process.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, workingDir.getAbsolutePath());
		}
		if(envp != null) {
			Arrays.sort(envp);
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < envp.length; i++) {
				buff.append(envp[i]);
				if(i < envp.length-1) {
					buff.append('\n');
				}
			}
			process.setAttribute(DebugPlugin.ATTR_ENVIRONMENT, buff.toString());
		}
		return process;
	}
	

	protected String renderCommandLine(String[] commandLine) {
		return DebugPlugin.renderArguments(commandLine, null);
	}	
	
	public static String renderProcessLabel(String[] commandLine, String timestamp) {
		String format= LaunchingMessages.StandardVMRunner__0____1___2; 
		return NLS.bind(format, new String[] { commandLine[0], timestamp });
	}

	protected Map<String, String> getDefaultProcessMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(IProcess.ATTR_PROCESS_TYPE, PROCESS_TYPE); 
		return map;
	}

	protected IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes) throws CoreException {
		IProcess process= DebugPlugin.newProcess(launch, p, label, attributes);  
		if (process == null) {
			p.destroy();
			abort(LaunchingMessages.AbstractVMRunner_0, null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR); 
		}
		return process;
	}

	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, code, message, exception));
	}
	
	private static boolean needsQuoting(String s) {
		int len = s.length();
		if (len == 0) // empty string has to be quoted
			return true;
		for (int i = 0; i < len; i++) {
			switch (s.charAt(i)) {
				case ' ': case '\t': case '\\': case '"':
					return true;
			}
		}
		return false;
	}
	private static String winQuote(String s) {
		if (! needsQuoting(s))
			return s;
		s = s.replaceAll("([\\\\]*)\"", "$1$1\\\\\""); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("([\\\\]*)\\z", "$1$1"); //$NON-NLS-1$ //$NON-NLS-2$
		return "\"" + s + "\""; //$NON-NLS-1$ //$NON-NLS-2$
	}
	private static String[] quoteWindowsArgs(String[] cmdLine) {
		// see https://bugs.eclipse.org/387504 , workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6511002
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			String[] winCmdLine = new String[cmdLine.length];
			for (int i = 0; i < cmdLine.length; i++) {
				winCmdLine[i] = winQuote(cmdLine[i]);
			}
			cmdLine = winCmdLine;
		}
		return cmdLine;
	}
	

	protected Process exec(String cmdLine) throws CoreException {
		cmdLine = quoteWindowsArgs(new String[]{cmdLine})[0];
		Process p= null;
		try {
			p= Runtime.getRuntime().exec(cmdLine);
		} catch (IOException e) {
		    Status status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, DebugCoreMessages.DebugPlugin_0, e); 
		    throw new CoreException(status);
		} catch (NoSuchMethodError e) {
		    Status status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, DebugCoreMessages.DebugPlugin_0, e); 
		    throw new CoreException(status);
		}
		return p;
	}
}