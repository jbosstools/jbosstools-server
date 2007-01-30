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
package org.jboss.ide.eclipse.as.core.runtime.server.polling;

import java.util.Date;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.JBossServerLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.server.TwiddleLauncher.ProcessData;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;

public class TwiddlePoller implements IServerStatePoller {

	public static final String STATUS = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.status";

	public static final String TYPE_TERMINATED = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.TYPE_TERMINATED";
	public static final String TYPE_RESULT = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.TYPE_RESULT";
	
	public static final int STATE_STARTED = 1;
	public static final int STATE_STOPPED = 0;
	public static final int STATE_TRANSITION = -1;
	
	private boolean expectedState;
	private int started; 
	private boolean canceled;
	private boolean done;
	private IServer server;
	private boolean securityException = false;
	
	private EventLogTreeItem event;
	public void beginPolling(IServer server, boolean expectedState, PollThread pt) {
		this.expectedState = expectedState;
		this.canceled = false;
		this.done = false;
		this.server = server;
		event = pt.getActiveEvent();
		launchTwiddlePoller();
	}

	private class PollerRunnable implements Runnable {
		private TwiddleLauncher launcher;
		public void run() {
			String args = "get \"jboss.system:type=Server\" Started";
			while( !canceled && !done ) {
				// are start processes still going?
				ServerProcessModelEntity ent = ServerProcessModel.getDefault().getModel(server.getId());
				IProcess[] processes = ent.getProcesses(JBossServerLaunchConfiguration.START);
				if( ServerProcessModel.allProcessesTerminated(processes)) {
					done = true;
					started = 0;
					eventAllProcessesTerminated();
				} else {
					launcher = new TwiddleLauncher();
					ProcessData[] datas = launcher.getTwiddleResults(server, args, true);
					if( datas.length == 1 ) {
						ProcessData d = datas[0];
						String out = d.getOut();
						if( out.startsWith("Started=true")) {
							started = STATE_STARTED;
						} else if(out.startsWith("Started=false")) {
							started = STATE_TRANSITION; // it's alive and responding
						} else if( out.indexOf("java.lang.SecurityException") != -1 ) {
							// throw exception 
							securityException = true;
						} else {
							started = STATE_STOPPED; // It's fully down
						}
						
						if( started == 1 && expectedState == SERVER_UP ) {
							done = true;
						} else if( started == 0 && expectedState == SERVER_DOWN) {
							done = true;
						} 
					}
				}
				if( !canceled )
					eventTwiddleExecuted();
			}
		}

		public void setCanceled() {
			if( launcher != null ) {
				launcher.setCanceled();
			}
		}
	}
	public void eventTwiddleExecuted() {
		TwiddlePollerEvent tpe = new TwiddlePollerEvent(event, TYPE_RESULT, started, expectedState);
		EventLogModel.markChanged(event);
	}
	public void eventAllProcessesTerminated() {
		TwiddlePollerEvent tpe = new TwiddlePollerEvent(event, TYPE_TERMINATED, started, expectedState);
		EventLogModel.markChanged(event);
	}
	
	private void launchTwiddlePoller() {
		PollerRunnable run = new PollerRunnable();
		Thread t = new Thread(run, "Twiddle Poller");
		t.start();
	}
	
	
	public void cancel(int type) {
		canceled = true;
	}

	public void cleanup() {
		ServerProcessModelEntity ent = ServerProcessModel.getDefault().getModel(server.getId());
		if( expectedState == SERVER_UP) {
			ent.terminateProcesses(JBossServerLaunchConfiguration.TWIDDLE);
		} else {
			ent.terminateProcesses(JBossServerLaunchConfiguration.TWIDDLE);
			ent.terminateProcesses(JBossServerLaunchConfiguration.STOP);
		}
	}

	public class PollingSecurityException extends PollingException {
		public PollingSecurityException(String msg) {super(msg);}
	}
	public boolean getState() throws PollingException  {
		if( securityException ) throw new PollingSecurityException("Security Exception");
		if( started == 0 ) return SERVER_DOWN;
		if( started == 1 ) return SERVER_UP;

		if( !done && !canceled ) 
			return !expectedState; // Not there yet.

		return expectedState; // done or canceled, doesnt matter
	}

	public boolean isComplete() throws PollingException {
		return done;
	}
	
	
	public class TwiddlePollerEvent extends EventLogTreeItem {
		public TwiddlePollerEvent(SimpleTreeItem parent, String type, int status, boolean expectedState) {
			super(parent, PollThread.SERVER_STATE_MAJOR_TYPE, type);
			setProperty(PollThread.EXPECTED_STATE, new Boolean(expectedState));
			setProperty(STATUS, new Integer(status));
			setProperty(DATE, new Long(new Date().getTime()));
		}
	}
	
	

}
