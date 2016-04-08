/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.WTPOveridePlugin;

/**
 * @author Jeff Maury
 *
 */
public class ServerToggleActivateOnOutputAction extends Action {

    /*
     * the associated console
     */
    private IConsole console;
    
    /**
     * @param console the associated console
     */
    public ServerToggleActivateOnOutputAction(IConsole console) {
        super(Messages.ShowConsoleOnOutputActionLabel, IAction.AS_CHECK_BOX);
        setToolTipText(Messages.ShowConsoleOnOutputActionLabel);
        this.console = console;
        setChecked(true);
        setImageDescriptor(WTPOveridePlugin.getInstance().getSharedImages().descriptor(WTPOveridePlugin.STDOUT_IMG));
    }

    @Override
    public void run() {
        if (console instanceof TextConsole) {
            boolean activateOnWrite = (boolean) ((TextConsole)console).getAttribute(ServerConsoleWriter.ACTIVATE_ON_WRITE_ATTRIBUTE_NAME);
            ((TextConsole)console).setAttribute(ServerConsoleWriter.ACTIVATE_ON_WRITE_ATTRIBUTE_NAME, !activateOnWrite);
        }
    }
}
