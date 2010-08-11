/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;

public class TwiddleLauncher {
	
	/**
	 * Create one launch for each String of arguments.
	 * Each element in the array is a COMPLETE LIST of arguments. 
	 * For example: 
	 *     twiddleArgs[0] = "-s localhost:1099 -a jmx/rmi/RMIAdaptor get \"whatever\""
	 *     twiddleArgs[1] = "-s localhost:1099 -a jmx/rmi/RMIAdaptor get \"whatever2\""
	 *     
	 * @param twiddleArgs 
	 * @param jbServer
	 * @param seed
	 * @return
	 */
	public static ProcessData[] launchTwiddles(String[] twiddleArgs, IServer server, boolean addPrefix) throws CoreException {
		ILaunchConfigurationWorkingCopy wc;
		ArrayList<ProcessData> list = new ArrayList<ProcessData>();
		for( int i = 0; i < twiddleArgs.length; i++ ) {
			String args2 = addPrefix ? TwiddleLaunchConfiguration.getDefaultArgs(server) + twiddleArgs[i] : twiddleArgs[i]; 
			wc = TwiddleLaunchConfiguration.createLaunchConfiguration(server, args2);
			ILaunch launch = wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor(), false, false);
			list.addAll(Arrays.asList(toProcessDatas(launch.getProcesses())));
		}
		
		ProcessData[] ret = new ProcessData[list.size()];
		list.toArray(ret);
		
		return ret;
	}
	
	protected static ProcessData[] toProcessDatas(IProcess[] processes) {
		ProcessData[] datas = new ProcessData[processes.length];
		for( int i = 0; i < processes.length; i++ ) {
			datas[i] = new ProcessData(processes[i], ""); //$NON-NLS-1$
			datas[i].startListening();
		}
		return datas;
	}
	
	private boolean canceled = false;
	private int delay = 500;
	
	public void setCanceled() {
		canceled = true;
	}
	
	public ProcessData[] getTwiddleResults(IServer server, String args, boolean addPrefix ) {
		ProcessData[] processes;
		try {
			processes = TwiddleLauncher.launchTwiddles(new String[] { args }, server, addPrefix);
		} catch( CoreException ce ) {
			return new ProcessData[0];
		}
		waitForThreadsToTerminate(processes);
		
		// stop listening 
		for( int i = 0; i < processes.length; i++ ) {
			processes[i].stopListening();
		}	
		
		return processes;
	}
		
	protected void waitForThreadsToTerminate(ProcessData[] processes) {
		boolean allTerminated = false;
		while( !allTerminated && !canceled) {
			allTerminated = true;
			for( int i = 0; i < processes.length; i++ ) {
				if( processes[i].getProcess().isTerminated() == false ) {
					allTerminated = false;
				}
			}
			if( !allTerminated ) {
				if( canceled ) {
					try {
						// if canceled, terminate all twiddle processes
						for( int i = 0; i < processes.length; i++ )
							processes[i].getProcess().terminate();
					} catch( DebugException e ) {
						IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
								Messages.TerminateTwiddleFailed, e);
						JBossServerCorePlugin.getDefault().getLog().log(s);
					}
				}	
				// sleep
				try {
					Thread.sleep(delay);
				} catch( InterruptedException ie ) {
				}
			}
		}
	}
	
	public static class ProcessData implements IStreamListener {
		private String args;
		private String processType;

		private IProcess process;
		private IStreamMonitor outMonitor;
		private IStreamMonitor errMonitor;
		
		private String out, err;

		public ProcessData(IProcess process, String processType) {
			this(null, process, processType);
			try {
				this.args = process.getLaunch().getLaunchConfiguration().
					getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
			} catch( CoreException ce ) {
			}

		}
		
		public ProcessData(String args, IProcess process, String processType) {
			this.args = args;
			this.process = process;
			this.processType = processType;
			
			//ASDebug.p("process is " + process, this);
			//ASDebug.p("Stream Proxy is " + process.getStreamsProxy(), this);
			this.outMonitor = process.getStreamsProxy().getOutputStreamMonitor();
			this.errMonitor = process.getStreamsProxy().getErrorStreamMonitor();
			
			out = ""; //$NON-NLS-1$
			err = ""; //$NON-NLS-1$
		}
		
		public void startListening() {
			outMonitor.addListener(this);
			errMonitor.addListener(this);
		}
		
		public void stopListening() {
			outMonitor.removeListener(this);
			errMonitor.removeListener(this);
		}
		
		public void resetStrings() {
			out = ""; //$NON-NLS-1$
			err = ""; //$NON-NLS-1$
		}
		
		public void destroy() {
			stopListening();
			resetStrings();
		}

		public void streamAppended(String text, IStreamMonitor monitor) {
			if( monitor == outMonitor ) {
				out += text;
			} else if( monitor == errMonitor ) {
				err += text;
			}
		}

		public String getArgs() {
			return args;
		}

		public String getErr() {
			return err;
		}

		public String getOut() {
			return out;
		}
		
		public String getProcesType() {
			return processType;
		}
		
		public IProcess getProcess() {
			return process;
		}

	}

}
