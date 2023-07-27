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
package org.jboss.tools.as.rsp.ui.model;

/**
 * Controls the state for an RSP
 */
public interface IRspStateController {
    /**
     * Start the RSP and alert the callback to changes in the state
     * @param callback
     * @return
     */
    public ServerConnectionInfo start(IRspStartCallback callback) throws StartupFailedException;

    /**
     * Terminate the rsp and alert the callback to changes in the state
     * @param callback
     */
    public void terminate(IRspStartCallback callback);

}
