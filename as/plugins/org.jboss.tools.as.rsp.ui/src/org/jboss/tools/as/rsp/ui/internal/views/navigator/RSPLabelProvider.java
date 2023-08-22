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

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.DeployableStateWrapper;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;

public class RSPLabelProvider extends AbstractLabelProvider {
	@Override
	public Image getImage(Object element) {
		if (element instanceof IRsp) {
			Image i = ((IRsp) element).getRspType().getIcon();
			return i;
		}
		if (element instanceof ServerStateWrapper) {
			ServerStateWrapper ssw = (ServerStateWrapper) element;
			IRsp rsp = ssw.getRsp();
			Image i = rsp.getRspType().getIcon(ssw.getServerState().getServer().getType().getId());
			return i;
		}
		if (element instanceof DeployableStateWrapper) {
			// ??
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IRsp) {
			return ((IRsp) element).getRspType().getName() + " [" + getRspState((IRsp) element) + "]";
		}
		if (element instanceof ServerStateWrapper) {
			return getServerStateString((ServerStateWrapper) element);
		}
		if (element instanceof DeployableStateWrapper) {
			return getDeploymentStateString((DeployableStateWrapper) element);
		}
		return null;
	}

	private static String getDeploymentStateString(DeployableStateWrapper element) {

		return element.getDeployableState().getReference().getLabel() + "   ["
				+ getRunStateString(element.getDeployableState().getState()) + ", "
				+ getPublishStateString(element.getDeployableState().getPublishState()) + "]";
	}

	private static String getServerStateString(ServerStateWrapper element) {
		return element.getServerState().getServer().getId() + "   ["
				+ getRunStateString(element.getServerState().getState()) + ", "
				+ getPublishStateString(element.getServerState().getPublishState()) + "]";
	}

	public static String getRspState(IRsp rsp) {
		IRspCore.IJServerState state = rsp.getState();
		if (state == IRspCore.IJServerState.STARTED && !rsp.wasLaunched()) {
			return "Connected";
		}
		return rsp.getState().toString().toUpperCase();
	}

	public static String getRunStateString(int state) {
		String stateString = "unknown";
		switch (state) {
		case ServerManagementAPIConstants.STATE_UNKNOWN:
			stateString = "unknown";
			break;
		case ServerManagementAPIConstants.STATE_STARTED:
			stateString = "started";
			break;
		case ServerManagementAPIConstants.STATE_STARTING:
			stateString = "starting";
			break;
		case ServerManagementAPIConstants.STATE_STOPPED:
			stateString = "stopped";
			break;
		case ServerManagementAPIConstants.STATE_STOPPING:
			stateString = "stopping";
			break;

		}
		return stateString.toUpperCase();
	}

	public static String getPublishTypeString(int type) {
		String stateString = "unknown";
		switch (type) {
		case ServerManagementAPIConstants.PUBLISH_AUTO:
			stateString = "auto";
			break;
		case ServerManagementAPIConstants.PUBLISH_FULL:
			stateString = "full";
			break;
		case ServerManagementAPIConstants.PUBLISH_INCREMENTAL:
			stateString = "incremental";
			break;
		case ServerManagementAPIConstants.PUBLISH_CLEAN:
			stateString = "clean";
			break;
		}
		return stateString.toUpperCase();
	}

	public static String getPublishStateString(int state) {
		String stateString = "unknown";
		switch (state) {
		case ServerManagementAPIConstants.PUBLISH_STATE_ADD:
			stateString = "add";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_FULL:
			stateString = "publish required (full)";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL:
			stateString = "publish required (incremental)";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_NONE:
			stateString = "synchronized";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_REMOVE:
			stateString = "remove";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN:
			stateString = "unknown";
			break;

		}
		return stateString.toUpperCase();
	}
}
