/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.server.IServerProcessListener;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.ServerProcessEvent;
import org.jboss.ide.eclipse.as.core.server.ServerStateChecker;
import org.jboss.ide.eclipse.as.core.server.runtime.AbstractServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.IJBossServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;

/**
 * Static methods to create and execute twiddle-oriented
 * launches. These launches will run on their own via the 
 * debug APIs and callers will only be allerted to the
 * start or completion of the launch if they are
 * registered to accept debug events.
 * 
 * @author rstryker
 *
 */
public class TwiddleLauncher implements IServerProcessListener {
	
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
	public static IProcess[] launchTwiddles(String[] twiddleArgs, JBossServer jbServer, String seed ) {
		ILaunchConfigurationWorkingCopy wc;
		ArrayList list = new ArrayList();
		for( int i = 0; i < twiddleArgs.length; i++ ) {
			try {
				System.out.println("[Twiddle Args] " + twiddleArgs[i]);
				wc = createTwiddleLaunch(jbServer, twiddleArgs[i], seed);
				ILaunch launch = wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor(), false, false);
				list.addAll(Arrays.asList(launch.getProcesses()));
			} catch( CoreException ce ) {
				// do nothing
			}
		}
		
		IProcess[] ret = new IProcess[list.size()];
		list.toArray(ret);
		
		return ret;
	}
	
	/**
	 * Create the proper launch configuration for a twiddle launch.
	 * 
	 * @param jbServer
	 * @param args
	 * @param seed
	 * @return
	 * @throws CoreException
	 */
	public static ILaunchConfigurationWorkingCopy createTwiddleLaunch(JBossServer jbServer, 
			String args, String seed) throws CoreException {
		
		JBossServerRuntime runtime = jbServer.getJBossRuntime();
		AbstractServerRuntimeDelegate runtimeDelegate = runtime.getVersionDelegate();

		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type =
			lm.getLaunchConfigurationType("org.jboss.ide.eclipse.as.core.jbossLaunch");

		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null,
				DebugPlugin.getDefault().getLaunchManager()
				.generateUniqueLaunchConfigurationNameFrom(seed));

		List classpath = runtimeDelegate.getRuntimeClasspath(jbServer, IJBossServerRuntimeDelegate.ACTION_TWIDDLE);
//		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
//				jbServer.getRuntimeConfiguration().getServerHome());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, runtimeDelegate.getTwiddleMainType(jbServer));
		workingCopy.setAttribute(Server.ATTR_SERVER_ID, jbServer.getServer().getId());
		workingCopy.setAttribute(JBossServerBehavior.ATTR_ACTION, JBossServerBehavior.ACTION_TWIDDLE);
		
		//workingCopy.setAttribute("org.eclipse.debug.ui.ATTR_CONSOLE_OUTPUT_ON", "false");
		//workingCopy.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false);
		
        workingCopy.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
                jbServer.getRuntimeConfiguration().getServerHome() + Path.SEPARATOR + "bin");

		
		
		return workingCopy;

	}

	
	private ProcessData[] processDatas = new ProcessData[0];
	private int max, delay;
	private int current;
	public TwiddleLauncher(int maxDuration, int delay) {
		this.max = maxDuration;
		this.delay = delay;
		current = 0;
	}
	
	public TwiddleLogEvent getTwiddleResults(JBossServer jbServer, String args) {
		String id = jbServer.getServer().getId();
		ServerProcessModelEntity ent = ServerProcessModel.getDefault().getModel(id);
		return getTwiddleResults(ent, jbServer, args);
	}
	
	public TwiddleLogEvent getTwiddleResults(ServerProcessModelEntity ent, JBossServer jbServer, String args ) {
		this.processDatas = null;
		ent.clear(ServerProcessModel.TWIDDLE_PROCESSES);
		
		ent.addSPListener(this);

		TwiddleLauncher.launchTwiddles(new String[] { args }, jbServer, ServerStateChecker.class.getName() + args);
		while( (this.processDatas == null && current < max)) {
			current +=delay;
			try {
				//System.out.println("current is " + current + " and datas is " + processDatas);
				Thread.sleep(delay);
			} catch( InterruptedException ie ) {
			}
		}
		
		if( processDatas == null ) processDatas = new ProcessData[0];
		// we now have some processdatas. Let's wait until they're all terminated.
		boolean allTerminated = false;
		while( !allTerminated && current < max ) {
			allTerminated = true;
			for( int i = 0; i < processDatas.length; i++ ) {
				if( processDatas[i].getProcess().isTerminated() == false ) {
					allTerminated = false;
				}
			}
			current += delay;
			if( !allTerminated ) {
				// sleep
				try {
					Thread.sleep(delay);
				} catch( InterruptedException ie ) {
				}
			}
		}
		
		TwiddleLogEvent event = createTwiddleEvents(args, allTerminated);
		
		
		ent.removeSPListener(this);
		
		return event;
	}
	
	
	private TwiddleLogEvent createTwiddleEvents(String args, boolean allTerminated) {
		// If there's only one process, we dont need to do all the rest
		if( processDatas.length == 1 ) {
			TwiddleLogEvent e = new TwiddleLogEvent("Twiddle Launch: " + args, 
					ProcessLogEvent.TWIDDLE, processDatas[0]);
//			ProcessLogEvent e = new ProcessLogEvent("Twiddle Launch: " + args, ProcessLogEvent.TWIDDLE);
//			if( !processDatas[0].getOut().equals("")) {
//				String out = processDatas[0].getOut();
//				String[] outArray = out.split("\r\n|\r|\n");
//				for( int i = 0; i < outArray.length; i++ ) {
//					e.addChild(outArray[i], ProcessLogEvent.STDOUT);
//				}
//			}
//			if( !processDatas[0].getErr().equals("")) {
//				String err = processDatas[0].getErr();
//				String[] errArray = err.split("\r\n|\r|\n");
//				for( int i = 0; i < errArray.length; i++ ) {
//					e.addChild(errArray[i], ProcessLogEvent.STDOUT);
//				}
//			}
			return e;
		}
		return new TwiddleLogEvent("Error in TwiddleLauncher (unexpected response)", ProcessLogEvent.ERROR, null);
	}
	
	public int getDuration() {
		return current;
	}
	
	public boolean exceededMax() {
		return current > max ? true : false;
	}
	
	public void ServerProcessEventFired(ServerProcessEvent event) {
		if( event.getProcessType().equals(ServerProcessModel.TWIDDLE_PROCESSES)) {
			if( event.getEventType().equals(IServerProcessListener.PROCESS_ADDED)) {
				ProcessData[] processDatas = event.getProcessDatas();
				for( int i = 0; i < processDatas.length; i++ ) {
					processDatas[i].startListening();
				}
				this.processDatas = processDatas; 
			}
		}
	}
	
	
	public static class TwiddleLogEvent extends ProcessLogEvent {
		
		private String out;
		private String err; 

		public TwiddleLogEvent(String text, int eventType, ProcessData data) {
			super(text, eventType);
			if( data != null ) {
				this.out = data.getOut();
				this.err = data.getErr();
			}
		}
		
		public String getOut() {
			return out;
		}
		public String getErr() {
			return err;
		}
		public boolean isException() {
			return false;
		}
	}
}
