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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.client.ICopyAsLaunch;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.module.factory.EjbModuleFactory.EjbModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class EjbDeploymentVerifier extends AbstractDeploymentVerifier implements ICopyAsLaunch  {
	
	public EjbDeploymentVerifier(JBossModuleDelegate delegate) {
		super(delegate);
	}

	public boolean supportsDeploy(IServer server, String launchMode) {
		if( JBossServerCore.getServer(server) == null ) {
			return false;
		}
		return true;
	}

	public String[] createTwiddleArgs(JBossServer server, JBossModuleDelegate delegate1) {
		if( !(delegate1 instanceof EjbModuleDelegate) ) {
			return new String[] { };
		} 
		
		EjbModuleDelegate delegate = (EjbModuleDelegate)delegate1;
		
		int jndiPort = server.getDescriptorModel().getJNDIPort();
		String host = server.getRuntimeConfiguration().getHost();

		String filename = delegate.getResourceName();
		
		String argsFront = "-s " + host + ":" + jndiPort +  " -a jmx/rmi/RMIAdaptor get ";
		
		String moduleArgs = createModuleArgs(argsFront, filename);
		String[] beanArgs = createBeanArgs(argsFront, delegate);
		String[] allArgs = new String[beanArgs.length+1];
		allArgs[0] = moduleArgs;
		for( int i = 0; i < beanArgs.length; i++ ) {
			allArgs[i+1] = beanArgs[i];
		}

		//String final2 = argsFront + service + " StateString Name";
		return allArgs;
	}

	private String createModuleArgs(String front, String filename) {
		return front + "\"jboss.j2ee:module=" + filename + ",service=EjbModule\" " + 
			"StateString Name";
	}
	
	private String[] createBeanArgs(String front, EjbModuleDelegate delegate) {
		String[] names = delegate.getBeanJNDINames();
		String[] args = new String[names.length];
		for( int i = 0; i < names.length; i++ ) {
			args[i] = front + "\"jboss.j2ee:jndiName=" + names[i] + ",service=EJB\" " + 
			"StateString Name";
		}
		return args;
	}
	
	protected String formatConsoleOutput() {
		return "";
	}

	protected boolean isComplete() {
		return true;
	}
	

	protected void cleanup() {

	}

	protected void initialize() {
	}

	protected void runVerifier() {
		String[] twiddleArgs = createTwiddleArgs(jbServer, (EjbModuleDelegate)delegate);
		for( int i = 0; i < twiddleArgs.length; i++ ) {
			ProcessLogEvent event = 
				new TwiddleLauncher(8000,100).getTwiddleResults(jbServer, twiddleArgs[i]);
			logEvent.addChild(event);
			logEvent.getRoot().branchChanged();
		}
	}

	protected void threadTerminated(DebugEvent[] events) {
	}

}
