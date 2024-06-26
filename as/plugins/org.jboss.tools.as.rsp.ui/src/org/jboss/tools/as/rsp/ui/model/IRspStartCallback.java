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
 * Allow an IRspStateController to update its caller with state changes for the
 * given rsp
 */
public interface IRspStartCallback {
	public void updateRspState(IRspCore.IJServerState state);

	public void updateRspState(IRspCore.IJServerState state, boolean launched);
}
