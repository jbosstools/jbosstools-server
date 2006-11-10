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
package org.jboss.ide.eclipse.as.core.runtime.server.internal;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.runtime.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.server.TwiddleLauncher.ProcessData;
import org.jboss.ide.eclipse.as.core.server.attributes.IServerPollingAttributes;

public class TwiddlePoller implements IServerStatePoller {

	private boolean expectedState;
	private int started; 
	private boolean canceled;
	private boolean done;
	private IServer server;
	
	private PollerRunnable currentRunnable;
	public void beginPolling(IServer server, boolean expectedState) {
		this.expectedState = expectedState;
		this.canceled = false;
		this.done = false;
		this.server = server;
		
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
				} else {
					launcher = new TwiddleLauncher();
					ProcessData[] datas = launcher.getTwiddleResults(server, args, true);
					if( datas.length == 1 ) {
						ProcessData d = datas[0];
						String out = d.getOut();
						if( out.startsWith("Started=true")) {
							started = 1;
						} else if(out.startsWith("Started=false")) {
							started = -1; // it's alive and responding
						} else {
							started = 0; // It's fully down
						}
						
						if( started == 1 && expectedState == SERVER_UP ) {
							done = true;
						} else if( started == 0 && expectedState == SERVER_DOWN) {
							done = true;
						} 
					}
				}
			}
		}

		public void setCanceled() {
			if( launcher != null ) {
				launcher.setCanceled();
			}
		}
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

	public boolean getState() {
		if( started == 0 ) return SERVER_DOWN;
		if( started == 1 ) return SERVER_UP;

		if( !done && !canceled ) 
			return !expectedState; // Not there yet.

		return expectedState; // done or canceled, doesnt matter
	}

	public boolean isComplete() {
		return done;
	}

}
