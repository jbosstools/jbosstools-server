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

import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * @author Jeff Maury
 */
public class ServerConsolePagePaticipant implements IConsolePageParticipant {

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage, org.eclipse.ui.console.IConsole)
     */
    @Override
    public void init(IPageBookViewPage page, IConsole console) {
        page.getSite().getActionBars().getToolBarManager().appendToGroup(IConsoleConstants.OUTPUT_GROUP, new ServerToggleActivateOnOutputAction(console));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
     */
    @Override
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
     */
    @Override
    public void activated() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
     */
    @Override
    public void deactivated() {
    }

}
