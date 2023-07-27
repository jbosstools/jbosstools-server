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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.dialogs.NewServerDialog;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.IRspCore.IJServerState;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerType;

public class CreateServerAction extends AbstractTreeAction {

	private static final String ERROR_DISCOVERY = "Error discovering server in selected folder";
    private static final String ERROR_LOADING_ATTRIBUTES = "Error requesting attributes for server type";
    private static final String ERROR_CREATING_SERVER = "Error creating server";
    
    public CreateServerAction(ISelectionProvider provider) {
		super(provider, "Create Server");
	}

    @Override
    protected boolean isVisible(Object[] o) {
        return safeSingleItemClass(o, IRsp.class);
    }

    @Override
    protected boolean isEnabled(Object[] o) {
        if( safeSingleItemClass(o, IRsp.class) ) {
            if( ((IRsp)o[0]).getState() == IRspCore.IJServerState.STARTED) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void singleSelectionActionPerformed(Object selected) {
        if( selected instanceof IRsp) {
            IRsp server = (IRsp)selected;
            if( server.getState() == IJServerState.STARTED) {
            	DirectoryDialog fd = new DirectoryDialog(Display.getDefault().getActiveShell());
            	String ret = fd.open();
            	if( ret != null) {
                    RspClientLauncher client = RspCore.getDefault().getClient(server);
                    createServerFromBean(client, ret);
            	}
            }
        }
    }

    private void createServerFromBean(RspClientLauncher client, String filePath) {
        if( filePath != null && client != null ) {
        	new Thread("[RSP] Query server type for system path") {
        		public void run() {
        			String fp2 = filePath;
                    CompletableFuture<List<ServerBean>> fut = client.getServerProxy().findServerBeans(new DiscoveryPath(fp2));
                    List<ServerBean> beans = null;
                    try {
                        beans = fut.get();
                    } catch (InterruptedException | ExecutionException e) {
                        apiError(e, ERROR_DISCOVERY);
                    }
                    if( beans == null || beans.size() == 0 ) {
                        apiError(new Exception("No server found at " + filePath), ERROR_DISCOVERY);
                    } else {
                        showCreateServerFromBeanDialog(beans, client);
                    }
        		}
        	}.start();
        }
    }

    private void showCreateServerFromBeanDialog(List<ServerBean> beans, RspClientLauncher client) {
        ServerBean bean1 = beans.get(0);
        String typeId = bean1.getServerAdapterTypeId();
        if( typeId == null || typeId.isEmpty() ) {
            UIHelper.executeInUI(() -> {
                showError("No server found in the given folder", "Invalid Selection");
            });
            return;
        }
        ServerType st = new ServerType(typeId, null, null);
        List<ServerType> allTypes = null;
        Attributes required2 = null;
        Attributes optional2 = null;
        try {
            required2 = client.getServerProxy()
                    .getRequiredAttributes(st).get();
            optional2 = client.getServerProxy()
                    .getOptionalAttributes(st).get();
            allTypes = client.getServerProxy().getServerTypes().get();
        } catch(InterruptedException | ExecutionException e ) {
            apiError(new Exception("Error loading attributes for server type " + typeId), ERROR_LOADING_ATTRIBUTES);
            return;
        }

        final HashMap<String,Object> values = new HashMap<>();
        if( required2.getAttributes().containsKey(DefaultServerAttributes.SERVER_HOME_DIR)) {
            values.put(DefaultServerAttributes.SERVER_HOME_DIR, bean1.getLocation());
        } else if( required2.getAttributes().containsKey(DefaultServerAttributes.SERVER_HOME_FILE)) {
            values.put(DefaultServerAttributes.SERVER_HOME_FILE, bean1.getLocation());
        }

        final Attributes required3 = required2;
        final Attributes optional3 = optional2;
        ServerType stype = null;
        for( int i = 0; i < allTypes.size(); i++ ) {
        	if( allTypes.get(i).getId().equals(typeId))
        		stype = allTypes.get(i);
        }
        final ServerType stype2 = stype;
        UIHelper.executeInUI(() -> {
            NewServerDialog td = new NewServerDialog(client, stype2, required3, optional3, values);
            td.open();
        });
    }
}
