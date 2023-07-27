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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;

public class StopRspAction extends AbstractTreeAction {
    public StopRspAction(ISelectionProvider provider) {
		super(provider, "Stop RSP");
	}

	@Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, IRsp.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        return safeSingleItemClass(o, IRsp.class) && ((IRsp)o[0]).getState() != IRspCore.IJServerState.STOPPED && ((IRsp)o[0]).exists();
    }

    @Override
    protected void singleSelectionActionPerformed(Object selected) {
        if( selected instanceof IRsp) {
            IRsp server = (IRsp)selected;
            new Thread("Stop RSP Server: " + server.getRspType().getId()) {
                public void run() {
                    RspCore.getDefault().stopServer(server);
                }
            }.start();
        }
    }

}
