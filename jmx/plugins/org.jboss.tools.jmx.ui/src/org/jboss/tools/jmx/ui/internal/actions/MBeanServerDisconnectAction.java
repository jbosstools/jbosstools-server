/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.internal.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jboss.tools.jmx.core.DisconnectJob;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.JMXUIActivator;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.internal.JMXImages;

/**
 * Disconnect from a server
 */
public class MBeanServerDisconnectAction extends Action {
	private IConnectionWrapper[] connection;
    public MBeanServerDisconnectAction(IConnectionWrapper[] wrapper) {
        super(Messages.MBeanServerDisconnectAction_text, AS_PUSH_BUTTON);
        JMXImages.setLocalImageDescriptors(this, "detachAgent.gif"); //$NON-NLS-1$
        this.connection = wrapper;
    }

    public void run() {
		if( connection != null ) {
			if( showDialog(connection))
				new DisconnectJob(connection).schedule();
		}
    }
    
    protected boolean showDialog(IConnectionWrapper[] wrappers) {
        return MessageDialog.openConfirm(JMXUIActivator
                .getActiveWorkbenchShell(),
                Messages.MBeanServerDisconnectAction_dialogTitle,
                Messages.MBeanServerDisconnectAction_dialogText);
    }
}