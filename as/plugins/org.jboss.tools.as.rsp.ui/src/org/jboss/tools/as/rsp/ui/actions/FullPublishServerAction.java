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
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;

public class FullPublishServerAction extends IncrementalPublishServerAction {

	public FullPublishServerAction(ISelectionProvider provider) {
		super(provider, Messages.FullPublishServerAction_0);
	}

	protected void singleSelectionActionPerformed(Object selected) {
		singleSelectionActionPerformed(selected, ServerManagementAPIConstants.PUBLISH_FULL);
	}

}
