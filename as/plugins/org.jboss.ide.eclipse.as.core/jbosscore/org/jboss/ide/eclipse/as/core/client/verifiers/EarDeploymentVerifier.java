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
package org.jboss.ide.eclipse.as.core.client.verifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.client.ICopyAsLaunch;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.IProcessLogVisitor;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class EarDeploymentVerifier extends AbstractDeploymentVerifier implements ICopyAsLaunch {

	private String argsStart;
	private ArrayList newProcesses;
	private int numProcessesExpected;
	private int numProcessesTerminated;

	public EarDeploymentVerifier(JBossModuleDelegate delegate) {
		super(delegate);
		newProcesses = new ArrayList();
	}

	public boolean supportsDeploy(IServer server, String launchMode) {
		if( JBossServerCore.getServer(server) == null ) {
			return false;
		}
		return true;
	}

	protected void initialize() {
		int jndiPort = jbServer.getDescriptorModel().getJNDIPort();
		String host = jbServer.getRuntimeConfiguration().getHost();
		argsStart = "-s " + host + ":" + jndiPort +  " -a jmx/rmi/RMIAdaptor ";
	}

	protected void runVerifier() {
		// First, delay until we know the server has responded to the file copy
		try {
			Thread.sleep(5000);
		} catch(Throwable t) {
			System.out.println("Got exception t: " + t);
			t.printStackTrace();
		}
		
		String id = jbServer.getServer().getId();
		ServerProcessModelEntity ent = ServerProcessModel.getDefault().getModel(id);

		
		// start one thread to get a list of services to query
		final String serviceNameQuery = argsStart + "query \"jboss.management.local:J2EEApplication=" + 
								delegate.getResourceName() + ",*\"";
		
		TwiddleLauncher launcher = new TwiddleLauncher(8000, 100);
		ProcessLogEvent event = launcher.getTwiddleResults(jbServer, serviceNameQuery);
		

		
		IProcessLogVisitor visitor = new IProcessLogVisitor() {
			private ArrayList list = new ArrayList();
			public Object getResult() {
				return list;
			}

			public boolean visit(ProcessLogEvent event) {
				System.out.println("hey!");
				ASDebug.p("Visiting " + event.getText() + " with kind " + event.getEventType() + " as compared to " + ProcessLogEvent.STDOUT, this);
				if( event.getEventType() == ProcessLogEvent.STDOUT) {
					list.add(event);
				}
				return true;
			} 
			
		};
		event.accept(visitor);
		logEvent.addChild(event);
		logEvent.getRoot().branchChanged();
		
		Iterator i = ((ArrayList)visitor.getResult()).iterator();
		while(i.hasNext()) {
			String serviceArgs = ((ProcessLogEvent)i.next()).getText();
			ProcessLogEvent event2 = launcher.getTwiddleResults(jbServer, argsStart + "get \"" + serviceArgs + "\"");
			logEvent.addChild(event2);
			logEvent.getRoot().branchChanged();
		}
	}

	private void launchTwiddleThreads2(String[] args) {
	}

	
	protected void processesAdded(ProcessData[] processes) {
	}
	
	protected void threadTerminated(DebugEvent[] events) {
	}

	protected void cleanup() {
	}

}
