/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.core;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.ServerProcess;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

public class RSEServerDummyProcess extends ServerProcess {
	private RSEStreamsProxy proxy;
	private PipedOutputStream pipeOut;
	
	public RSEServerDummyProcess(IServer server, ILaunch launch, String label) {
		super(launch, server, label);
	}

	public void processComplete() {
		complete = true;
		if( pipeOut != null ) {
			try {
				pipeOut.flush();
				pipeOut.close();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		fireTerminateEvent();
	}
	
    @Override
    public IStreamsProxy getStreamsProxy() {
    	if( proxy == null ) {
    		try {
	    		PipedInputStream sysout = new PipedInputStream();
	    		pipeOut = new PipedOutputStream(sysout);
	    		proxy = new RSEStreamsProxy(sysout, null, null, null);
    		} catch(IOException ioe) {
    			if( pipeOut != null ) { 
    				try {
    					pipeOut.close();
    				} catch(IOException ioe2) {
    				}
    			}
    		}
    	}
        return proxy;
    }
    
    public synchronized void appendToSysout(String[] lines) {
    	if( pipeOut != null && !complete ) {
    		if( lines != null ) {
    			for( int i = 0; i < lines.length; i++ ) {
    				try {
	    				pipeOut.write(lines[i].getBytes());
	    				pipeOut.write(System.lineSeparator().getBytes());
    				} catch(IOException ioe) {
    					ioe.printStackTrace();
    				}
    			}
    		}
    	}
    }

	@Override
	public void serverChanged(ServerEvent event) {
		super.serverChanged(event);
		if( UnitedServerListener.serverSwitchesToState(event, IServer.STATE_STOPPED)) {
			ILaunch l = getLaunch();
			if( l != null ) {
				try {
					ILaunchConfiguration lc = l.getLaunchConfiguration();
					boolean skipLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(lc);
					// If we didn't launch the server, then we can't have expected the shutdown
					// to have cleaned up the debugger
					if( skipLaunch ) {
						IDebugTarget dt = l.getDebugTarget();
						if( dt.canDisconnect()) {
							dt.disconnect();
							fireTerminateEvent();
						}
					}
				} catch(CoreException ce) {
					// ignore
				}
			}
		}
	}
	
	@Override
	public boolean canTerminate() {
		return !isTerminated() && getProcessId() != null;
	}
	
	private String getProcessId() {
		return (String)getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.PROCESS_ID);
	}
	
	private IControllableServerBehavior getControllableBehavior() {
		if( server != null ) {
			ServerBehaviourDelegate del = (ServerBehaviourDelegate)server.loadAdapter(ServerBehaviourDelegate.class, null);
			if( del instanceof ControllableServerBehavior)
				return (IControllableServerBehavior)del;
		}
		return null;
	}
}