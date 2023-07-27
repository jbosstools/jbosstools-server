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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.LaunchManager;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspType;
import org.jboss.tools.as.rsp.ui.util.ui.RemoteServerProcess;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerProcess;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;
import org.jboss.tools.rsp.api.dao.ServerState;

/**
 * The model for a single RSP, including its type, client, jobs currently executing,
 * and state for each of the servers declared on the RSP (including deployment state)
 */
public class SingleRspModel {
    private IRspType type;
    private IRsp server;
    private RspClientLauncher client;
    private List<JobProgress> jobs;
    private List<ServerState> serverState;
    
    private Map<String, ILaunch> launchForServer;

    public SingleRspModel(IRsp server) {
        this.server = server;
        this.type = server.getRspType();
        this.jobs = new ArrayList<>();
        this.serverState = new ArrayList<>();
        this.launchForServer = new HashMap<>();
    }

    public void setClient(RspClientLauncher client) {
        this.client = client;
    }

    public IRspType getType() {
        return type;
    }
    public IRsp getServer() {
        return server;
    }
    public RspClientLauncher getClient() {
        return client;
    }
    public List<JobProgress> getJobs() {
        return new ArrayList<>(jobs);
    }
    public List<ServerState> getServerState() {
        return serverState;
    }

    public void addJob(JobHandle jobHandle) {
        JobProgress jp = new JobProgress(jobHandle, 0);
        jobs.add(jp);
    }
    public void removeJob(JobHandle jobHandle) {
        for( JobProgress jp : new ArrayList<>(jobs) ) {
            if( jp.getHandle().getId().equals(jobHandle.getId()) && jp.getHandle().getName().equals(jobHandle.getName())) {
                jobs.remove(jp);
            }
        }
    }

    public void jobChanged(JobProgress jobProgress) {
        JobHandle jobHandle = jobProgress.getHandle();
        for( JobProgress jp : new ArrayList<>(jobs) ) {
            if( jp.getHandle().getId().equals(jobHandle.getId()) && jp.getHandle().getName().equals(jobHandle.getName())) {
                jp.setPercent(jobProgress.getPercent());
            }
        }
    }

    public void addServer(ServerHandle serverHandle) {
        if( findServerState(serverHandle) == null) {
            ServerState ss = new ServerState();
            ss.setServer(serverHandle);
            ss.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
            ss.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN);
            serverState.add(ss);
        }
    }
    public void removeServer(ServerHandle serverHandle) {
        ServerState ss = findServerState(serverHandle);
        if( ss != null ) {
            serverState.remove(ss);
        }
    }
    public void updateServer(ServerState state) {
        ServerState ss = findServerState(state.getServer());
        if( ss != null ) {
            ss.setServer(state.getServer());
            ss.setDeployableStates(state.getDeployableStates());
            ss.setPublishState(state.getPublishState());
            ss.setState(state.getState());
            ss.setRunMode(state.getRunMode());
        } else {
            serverState.add(state);
        }
    }

    private ServerState findServerState(ServerHandle serverHandle) {
        for( ServerState ss : new ArrayList<>(serverState)) {
            ServerHandle ssh = ss.getServer();
            if( ssh.getId().equals(serverHandle.getId()) && ssh.getType().getId().equals(serverHandle.getType().getId()))
                return ss;
        }
        return null;
    }

    public void clear() {
        this.serverState = new ArrayList<>();
        this.jobs = new ArrayList<>();
    }

    private String internalIdForProcess(ServerProcess p) {
        ServerHandle sh = p.getServer();
        String id = sh.getType().getId() + ":" + sh.getId() + ":" + p.getProcessId();
        return id;
    }
    public IProcess addServerProcess(IRsp rsp, ServerProcess serverProcess) {
        ILaunch launch = findOrCreateLaunchForServer(rsp, serverProcess);
        IProcess sp = new RemoteServerProcess(launch, rsp, serverProcess);;
        launch.addProcess(sp);
        return sp;
    }

    protected ILaunch findOrCreateLaunchForServer(IRsp rsp, ServerProcess serverProcess) {
        String id = internalIdForProcess(serverProcess);
        ILaunch l = this.launchForServer.get(serverProcess.getServer().getId());
        if( l == null ) {
            l = new Launch(null, "run", null);
            l.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, Long.toString(System.currentTimeMillis()));
            l.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "true");
            this.launchForServer.put(serverProcess.getServer().getId(), l);
            getLaunchManager().addLaunch(l);
        }
        return l;
    }
        
	protected LaunchManager getLaunchManager() {
		return (LaunchManager)DebugPlugin.getDefault().getLaunchManager();
	}

    public void serverProcessTerminated(ServerProcess serverProcess) {
    	ILaunch l = this.launchForServer.get(serverProcess.getServer().getId());
    	if( l != null) {
    		IProcess[] all = l.getProcesses();
    		boolean allComplete = true;
    		for( int i = 0; i < all.length; i++ ) {
    			if( all[i] instanceof RemoteServerProcess) {
    				RemoteServerProcess rs = ((RemoteServerProcess)all[i]);
    				if( serverProcess.getProcessId().equals(rs.getServerProcessId())) {
        				rs.setComplete(true);
    				}
    			}
    			allComplete &= all[i].isTerminated();
    		}
    		if( allComplete ) {
    			this.launchForServer.remove(serverProcess.getServer().getId());
    		}
    	}
    }

    public void serverProcessOutputAppended(ServerProcessOutput spo) {
    	String procId = spo.getProcessId();
        ServerProcess mock = new ServerProcess(spo.getServer(), procId);
        ILaunch l = this.launchForServer.get(spo.getServer().getId());
        if( l != null ) {
        	IProcess[] processes = l.getProcesses();
        	for( int i = 0; i < processes.length; i++ ) {
        		if( processes[i] instanceof RemoteServerProcess ) {
        			RemoteServerProcess rProc = (RemoteServerProcess)processes[i];
        			if( spo.getStreamType() == ServerManagementAPIConstants.STREAM_TYPE_SYSOUT) {
        				rProc.outAppended(spo.getText());
        			} else {
        				rProc.errAppended(spo.getText());
        			}
        		}
        	}
        }
    }
}