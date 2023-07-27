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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.dialogs.StringPromptDialog;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.IRspCoreChangeListener;
import org.jboss.tools.as.rsp.ui.model.IRspType;
import org.jboss.tools.as.rsp.ui.model.ServerConnectionInfo;
import org.jboss.tools.as.rsp.ui.telemetry.TelemetryService;
import org.jboss.tools.as.rsp.ui.types.CommunityServerConnector;
import org.jboss.tools.as.rsp.ui.types.RedHatServerConnector;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.JobRemoved;
import org.jboss.tools.rsp.api.dao.MessageBoxNotification;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerProcess;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StringPrompt;

/**
 * The primary model for the framework. It mostly just links events from rsp and client
 * to listeners and stores some basic information about each model object
 */
public class RspCore implements IRspCore {
    private static RspCore instance = new RspCore();
    public static RspCore getDefault() {
        return instance;
    }


    private Map<String,SingleRspModel> allRsps = new HashMap<>();
    private List<IRspCoreChangeListener> listeners = new ArrayList<>();
    private Map<String, RspProgressJob> uiJobs = new HashMap<>();

    private RspCore() {
        loadRSPs();
    }

    @Override
    public IRspType findRspType(String id) {
        SingleRspModel srm = allRsps.get(id);
        if( srm != null )
            return srm.getType();
        return null;
    }

    @Override
    public IRsp findRsp(String id) {
        SingleRspModel srm = allRsps.get(id);
        if( srm != null )
            return srm.getServer();
        return null;
    }

    private SingleRspModel findModel(String typeId) {
        return allRsps.get(typeId);
    }

    private void loadRSPs() {
        // TODO load from xml file or something
        IRsp rht = new RedHatServerConnector().getRsp(this);
        IRsp community = new CommunityServerConnector().getRsp(this);
        allRsps.put(rht.getRspType().getId(), new SingleRspModel(rht));
        allRsps.put(community.getRspType().getId(), new SingleRspModel(community));
    }

    public void startServer(IRsp server) {
        ServerConnectionInfo info = server.start();
        if( info != null ) {
            try {
                RspClientLauncher launcher = launch(server, info.getHost(), info.getPort());
                String typeId = server.getRspType().getId();
                SingleRspModel srm = findModel(typeId);
                if( srm != null ) {
                    srm.setClient(launcher);
                }
//                telemetry().sendWithType(TelemetryService.TELEMETRY_START_RSP, server.getRspType().getId());
            } catch(IOException e ) {
 //               telemetry().sendWithType(TelemetryService.TELEMETRY_START_RSP, server.getRspType().getId(), e);
            } catch(InterruptedException ie) {
   //             telemetry().sendWithType(TelemetryService.TELEMETRY_START_RSP, server.getRspType().getId(), ie);
            } catch( ExecutionException ee) {
     //           telemetry().sendWithType(TelemetryService.TELEMETRY_START_RSP, server.getRspType().getId(), ee);
            }
        }
    }

    public RspClientLauncher getClient(IRsp rsp) {
        SingleRspModel srm = findModel(rsp.getRspType().getId());
        return srm == null ? null : srm.getClient();
    }

    @Override
    public void stopServer(IRsp server) {
        server.stop();
        //telemetry().sendWithType(TelemetryService.TELEMETRY_STOP_RSP, server.getRspType().getId());
    }

    private TelemetryService telemetry() {
        //return TelemetryService.instance();
    	return null;
    }

    @Override
    public void stateUpdated(RspImpl rspServer) {
        if( rspServer.getState() == IJServerState.STOPPED) {
            SingleRspModel srm = findModel(rspServer.getRspType().getId());
            if(srm != null ) {
                srm.setClient(null);
                srm.clear();
            }
        }
        modelUpdated(rspServer);
    }

    private RspClientLauncher launch(IRsp rsp, String host, int port) throws IOException, InterruptedException, ExecutionException {
        RspClientLauncher launcher = new RspClientLauncher(rsp, host, port);
        launcher.setListener(() -> {
            rsp.terminate();
        });
        launcher.launch();
        ClientCapabilitiesRequest clientCapRequest = createClientCapabilitiesRequest();
        launcher.getServerProxy().registerClientCapabilities(clientCapRequest).get();
        return launcher;
    }

    private ClientCapabilitiesRequest createClientCapabilitiesRequest() {
        Map<String, String> clientCap = new HashMap<>();
        clientCap.put(ICapabilityKeys.STRING_PROTOCOL_VERSION, ICapabilityKeys.PROTOCOL_VERSION_0_10_0);
        clientCap.put(ICapabilityKeys.BOOLEAN_STRING_PROMPT, Boolean.toString(true));
        return new ClientCapabilitiesRequest(clientCap);
    }


    @Override
    public IRsp[] getRSPs() {
        List<IRsp> ret = allRsps.values().stream().filter(p->p.getServer() != null).
                map(SingleRspModel::getServer).collect(Collectors.toList());

        return ret.toArray(new IRsp[ret.size()]);
    };

    @Override
    public ServerState[] getServersInRsp(IRsp rsp) {
    	if( rsp == null )
    		return new ServerState[0];
        SingleRspModel srm = findModel(rsp.getRspType().getId());
        List<ServerState> states = srm.getServerState();
        return states.toArray(new ServerState[states.size()]);
    }

    @Override
    public ServerState findServerInRsp(IRsp rsp, String serverId) {
        ServerState[] all = getServersInRsp(rsp);
        if( all == null )
            return null;
        for( int i = 0; i < all.length; i++ ) {
            if( all[i].getServer().getId().equals(serverId))
                return all[i];
        }
        return null;
    }


    @Override
    public void modelUpdated(Object o) {
        for( IRspCoreChangeListener l : listeners ) {
            l.modelChanged(o);
        }
    }

    @Override
    public void addChangeListener(IRspCoreChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(IRspCoreChangeListener listener) {
        listeners.remove(listener);
    }



    /*
    Events from clients
     */
    @Override
    public void jobAdded(IRsp rsp, JobHandle jobHandle) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.addJob(jobHandle);
            String id = jobHandleToUniqueId(rsp, jobHandle);
            RspProgressJob progJob = new RspProgressJob(rsp, jobHandle);
            uiJobs.put(id, progJob);
            progJob.schedule();
            modelUpdated(rsp);
        }
    }

    private String jobHandleToUniqueId(IRsp rsp, JobHandle handle) {
        return rsp.getRspType().getId() + ":" + handle.getId();
    }

    @Override
    public void jobRemoved(IRsp rsp, JobRemoved jobRemoved) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.removeJob(jobRemoved.getHandle());
            String id = jobHandleToUniqueId(rsp, jobRemoved.getHandle());
            RspProgressJob uiJob = uiJobs.get(id);
            if( uiJob != null ) {
                uiJob.setJobRemoved(jobRemoved);
                uiJobs.remove(id);
            }
            modelUpdated(rsp);
        }
    }

    @Override
    public void jobChanged(IRsp rsp, JobProgress jobProgress) {

        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.jobChanged(jobProgress);
            String id = jobHandleToUniqueId(rsp, jobProgress.getHandle());
            RspProgressJob uiJob = uiJobs.get(id);
            if( uiJob != null ) {
                uiJob.setJobProgress(jobProgress);
            }
            modelUpdated(rsp);
        }
    }

    @Override
    public JobProgress[] getJobs(IRsp rsp) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            List<JobProgress> jps = model.getJobs();
            return jps.toArray(new JobProgress[0]);
        }
        return new JobProgress[0];
    }

    @Override
    public void serverAdded(IRsp rsp, ServerHandle serverHandle) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.addServer(serverHandle);
            modelUpdated(rsp);
        }
    }

    @Override
    public void serverRemoved(IRsp rsp, ServerHandle serverHandle) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.removeServer(serverHandle);
            modelUpdated(rsp);
        }
    }

    @Override
    public void serverAttributesChanged(IRsp rsp, ServerHandle serverHandle) {
        // Ignore?
    }

    @Override
    public void serverStateChanged(IRsp rsp, ServerState serverState) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.updateServer(serverState);
            modelUpdated(rsp);
        }
    }

    @Override
    public void serverProcessCreated(IRsp rsp, ServerProcess serverProcess) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
        	model.addServerProcess(rsp, serverProcess);
        }
    }

    @Override
    public void serverProcessTerminated(IRsp rsp, ServerProcess serverProcess) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.serverProcessTerminated(serverProcess);
        }
    }

    @Override
    public void serverProcessOutputAppended(IRsp rsp, ServerProcessOutput serverProcessOutput) {
        SingleRspModel model = findModel(rsp.getRspType().getId());
        if( model != null ) {
            model.serverProcessOutputAppended(serverProcessOutput);
        }
    }

    @Override
    public CompletableFuture<String> promptString(IRsp rsp, StringPrompt stringPrompt) {
        final String[] ret = new String[1];
        UIHelper.executeInUI(() -> {
            StringPromptDialog spd = new StringPromptDialog(rsp, stringPrompt);
            int result = spd.open();
            if( result == Window.OK) {
                ret[0] = spd.getText();
            } else {
                ret[0] = null;
            }
        });
        return CompletableFuture.completedFuture(ret[0]);
    }

    @Override
    public void messageBox(IRsp rsp, MessageBoxNotification messageBoxNotification) {
        int sev = messageBoxNotification.getSeverity();
        int istatusSev = 0;
        if( sev == Status.ERROR) {
        	istatusSev = IStatus.ERROR;
        } else if( sev == Status.INFO) {
        	istatusSev = IStatus.INFO;
        } else if( sev == Status.WARNING) {
        	istatusSev = IStatus.WARNING;
        } else if( sev == Status.OK) {
        	istatusSev = IStatus.INFO;
        }
        IStatus stat = StatusFactory.getInstance(istatusSev, RspUiActivator.PLUGIN_ID, messageBoxNotification.getMessage());
        showStatusDialog(stat, null);
    }
    
    public static void showStatusDialog(String msg, String title, int severity) {
        IStatus stat = StatusFactory.getInstance(severity, RspUiActivator.PLUGIN_ID, msg);
        showStatusDialog(stat, title);
    }
    public static void showStatusDialog(IStatus status, String title) {
        final StatusDialog dialog = title == null ? new CustomStatusDialog(status) : new CustomStatusDialog(title, status); 
        Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				dialog.open();
			}});
    }
    
    private static class CustomStatusDialog extends StatusDialog {
    	public CustomStatusDialog(String title, IStatus s) {
    		super(Display.getDefault().getActiveShell());
    		if( title != null ) {
    			this.setTitle(title);
    		}
    		updateStatus(s);
    	}
    	public CustomStatusDialog(IStatus s) {
    		this(null, s);
    	}
    }

}
