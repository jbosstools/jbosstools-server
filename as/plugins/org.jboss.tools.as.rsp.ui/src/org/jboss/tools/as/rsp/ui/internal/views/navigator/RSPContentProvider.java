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
package org.jboss.tools.as.rsp.ui.internal.views.navigator;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerState;

public class RSPContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if( parentElement instanceof RSPNavigator ) {
			return RspCore.getDefault().getRSPs();
		}
		if( parentElement instanceof IRsp ) {
			IRsp p = (IRsp) parentElement;
			return wrap(p, RspCore.getDefault().getServersInRsp(p));
		}
		if( parentElement instanceof ServerStateWrapper ) {
			ServerStateWrapper ssw = (ServerStateWrapper)parentElement;
			List<DeployableState> deployments = ssw.getServerState().getDeployableStates();
			return wrapDeployableStates(ssw, deployments);
		}

//		if( parentElement instanceof IRsp) {
//			IRsp parent = (IRsp) parentElement;
//			return parent.getState()
//		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		Object[] kids = getChildren(element);
		return kids != null && kids.length > 0;
	}

    private DeployableStateWrapper[] wrapDeployableStates(ServerStateWrapper element, List<DeployableState> ds) {
        if( ds == null )
            return new DeployableStateWrapper[0];

        DeployableStateWrapper[] ret = new DeployableStateWrapper[ds.size()];
        int i = 0;
        for( DeployableState ds1 : ds) {
            ret[i++] = new DeployableStateWrapper(element, ds1);
        }
        return ret;
    }

    private ServerStateWrapper[] wrap(IRsp rsp, ServerState[] state) {
        ServerStateWrapper[] wrappers = new ServerStateWrapper[state.length];
        for( int i = 0; i < state.length; i++ ) {
            wrappers[i] = new ServerStateWrapper(rsp, state[i]);
        }
        return wrappers;
    }

    public static class ServerStateWrapper {
        private IRsp rsp;
        private ServerState ss;
        public ServerStateWrapper(IRsp rsp, ServerState ss) {
            this.rsp = rsp;
            this.ss = ss;
        }

        public IRsp getRsp() {
            return rsp;
        }

        public ServerState getServerState() {
            return ss;
        }
    }
    public static class DeployableStateWrapper {
        private ServerStateWrapper serverState;
        private DeployableState ds;
        public DeployableStateWrapper(ServerStateWrapper serverState, DeployableState ds) {
            this.serverState = serverState;
            this.ds = ds;
        }

        public ServerStateWrapper getServerState() {
            return serverState;
        }
        public DeployableState getDeployableState() {
            return ds;
        }
    }

}
