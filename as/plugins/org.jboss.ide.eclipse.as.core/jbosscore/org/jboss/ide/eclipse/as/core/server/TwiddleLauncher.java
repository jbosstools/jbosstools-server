/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;

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
		ArrayList list = new ArrayList();
		for( int i = 0; i < twiddleArgs.length; i++ ) {
			wc = createTwiddleLaunch(server, twiddleArgs[i], addPrefix);
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
			datas[i] = new ProcessData(processes[i], "");
			datas[i].startListening();
		}
		return datas;
	}
	
	public static ILaunchConfigurationWorkingCopy createTwiddleLaunch(IServer server, 
			String args, boolean addPrefix) throws CoreException {
		
		ILaunchConfigurationWorkingCopy workingCopy =
			JBossServerLaunchConfiguration.setupLaunchConfiguration(server, JBossServerLaunchConfiguration.TWIDDLE);
		
		// If we have to use the prefix from the launch config, throw it in front. 
		if( addPrefix ) {
			String a2 = workingCopy.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS 
					+ JBossServerLaunchConfiguration.PRGM_ARGS_TWIDDLE_SUFFIX, "");
			args = a2 + " " + args;
		}
		
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		return workingCopy;
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
			if( canceled ) {
				try {
					// if canceled, terminate all twiddle processes
					processes[i].getProcess().terminate();
				} catch( Exception e ) {}
			}	
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
			
			out = "";
			err = "";
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
			out = "";
			err = "";
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
