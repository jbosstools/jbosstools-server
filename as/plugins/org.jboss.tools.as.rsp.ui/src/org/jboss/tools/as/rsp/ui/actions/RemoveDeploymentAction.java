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
package org.jboss.tools.as.rsp.ui.actions;

import java.util.concurrent.ExecutionException;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.DeployableStateWrapper;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.Status;

public class RemoveDeploymentAction extends AbstractTreeAction {
	private static final String ERROR_REMOVING_DEPLOYMENT = "Error removing deployment";
    public RemoveDeploymentAction(ISelectionProvider provider) {
		super(provider, "Remove Deployment");
	}


    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, DeployableStateWrapper.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        return safeSingleItemClass(o, DeployableStateWrapper.class);
    }

    @Override
    protected void singleSelectionActionPerformed(Object selected) {
        if( selected instanceof DeployableStateWrapper) {
            DeployableStateWrapper wrap = (DeployableStateWrapper)selected;
            ServerDeployableReference sdr = getServerDeployableReference(wrap);
            IRsp rsp = wrap.getServerState().getRsp();
            RSPServer rspServer = RspCore.getDefault().getClient(rsp).getServerProxy();
            new Thread("Remove Deployment") {
                public void run() {
                    actionPerformedThread(rspServer, sdr);
                }
            }.start();
        }
    }

    protected void actionPerformedThread(RSPServer rspServer, ServerDeployableReference sdr) {
        try {
            Status stat = rspServer.removeDeployable(sdr).get();
            //TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_REMOVE, sdr.getServer().getType().getId(), stat);
            if( stat == null || !stat.isOK()) {
                statusError(stat, ERROR_REMOVING_DEPLOYMENT);
            }
        } catch (InterruptedException | ExecutionException ex) {
            //TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DEPLOYMENT_REMOVE, sdr.getServer().getType().getId(), ex);
            apiError(ex, ERROR_REMOVING_DEPLOYMENT);
        }
    }

    public static ServerDeployableReference getServerDeployableReference(DeployableStateWrapper wrap) {
        DeployableState ds = wrap.getDeployableState();
        ServerHandle sh = wrap.getServerState().getServerState().getServer();
        ServerDeployableReference sdr = new ServerDeployableReference(sh, ds.getReference());
        return sdr;
    }


}
