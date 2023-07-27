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
package org.jboss.tools.as.rsp.ui.client;

import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.*;

import java.util.concurrent.CompletableFuture;

/**
 * Make requests to, and receive events from, the remote rsp
 */
public class ServerManagementClientImpl implements RSPClient {
    private final IRsp uiRspServer;
    private RSPServer server;
    public ServerManagementClientImpl(IRsp rspUi) {
        this.uiRspServer = rspUi;
    }

    public void initialize(RSPServer server) {
        this.server = server;
    }

    public RSPServer getProxy() {
        return server;
    }

    private static interface MyRunnable {
        public void run();
    }

    @Override
    public CompletableFuture<String> promptString(StringPrompt stringPrompt) {
        return uiRspServer.getModel().promptString(uiRspServer, stringPrompt);
    }

    private void async(MyRunnable run) {
        new Thread(() -> run.run()).start();
    }

    @Override
    public void jobAdded(JobHandle jobHandle) {
        async(()->uiRspServer.getModel().jobAdded(uiRspServer, jobHandle));
    }

    @Override
    public void jobRemoved(JobRemoved jobRemoved) {
        async(()->uiRspServer.getModel().jobRemoved(uiRspServer, jobRemoved));
    }

    @Override
    public void jobChanged(JobProgress jobProgress) {
        async(()->uiRspServer.getModel().jobChanged(uiRspServer, jobProgress));
    }

    @Override
    public void messageBox(MessageBoxNotification messageBoxNotification) {
        async(()->uiRspServer.getModel().messageBox(uiRspServer, messageBoxNotification));
    }

    @Override
    public void discoveryPathAdded(DiscoveryPath discoveryPath) {
        // Ignore, not worth showing / displaying
    }

    @Override
    public void discoveryPathRemoved(DiscoveryPath discoveryPath) {
        // Ignore, not worth showing / displaying
    }

    @Override
    public void serverAdded(ServerHandle serverHandle) {
        async(()->uiRspServer.getModel().serverAdded(uiRspServer, serverHandle));
    }

    @Override
    public void serverRemoved(ServerHandle serverHandle) {
        async(()->uiRspServer.getModel().serverRemoved(uiRspServer, serverHandle));
    }

    @Override
    public void serverAttributesChanged(ServerHandle serverHandle) {
        async(()->uiRspServer.getModel().serverAttributesChanged(uiRspServer, serverHandle));
    }

    @Override
    public void serverStateChanged(ServerState serverState) {
        async(()->uiRspServer.getModel().serverStateChanged(uiRspServer, serverState));
    }

    @Override
    public void serverProcessCreated(ServerProcess serverProcess) {
        async(()->uiRspServer.getModel().serverProcessCreated(uiRspServer, serverProcess));
    }

    @Override
    public void serverProcessTerminated(ServerProcess serverProcess) {
        uiRspServer.getModel().serverProcessTerminated(uiRspServer, serverProcess);
    }

    @Override
    public void serverProcessOutputAppended(ServerProcessOutput serverProcessOutput) {
        async(()->uiRspServer.getModel().serverProcessOutputAppended(uiRspServer, serverProcessOutput));
    }
}
