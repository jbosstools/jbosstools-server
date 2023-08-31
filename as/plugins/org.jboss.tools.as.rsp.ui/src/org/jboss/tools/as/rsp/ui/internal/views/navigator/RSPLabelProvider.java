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
			return ((IRsp) element).getRspType().getName() + " [" + getRspState((IRsp) element) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
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

		return element.getDeployableState().getReference().getLabel() + "   [" //$NON-NLS-1$
				+ getRunStateString(element.getDeployableState().getState()) + ", " //$NON-NLS-1$
				+ getPublishStateString(element.getDeployableState().getPublishState()) + "]"; //$NON-NLS-1$
	}

	private static String getServerStateString(ServerStateWrapper element) {
		return element.getServerState().getServer().getId() + "   [" //$NON-NLS-1$
				+ getRunStateString(element.getServerState().getState()) + ", " //$NON-NLS-1$
				+ getPublishStateString(element.getServerState().getPublishState()) + "]"; //$NON-NLS-1$
	}

	public static String getRspState(IRsp rsp) {
		IRspCore.IJServerState state = rsp.getState();
		if (state == IRspCore.IJServerState.STARTED && !rsp.wasLaunched()) {
			return Messages.RSPLabelProvider_8;
		}
		return rsp.getState().toString().toUpperCase();
	}

	public static String getRunStateString(int state) {
		String stateString = Messages.RSPLabelProvider_9;
		switch (state) {
		case ServerManagementAPIConstants.STATE_UNKNOWN:
			stateString = Messages.RSPLabelProvider_10;
			break;
		case ServerManagementAPIConstants.STATE_STARTED:
			stateString = Messages.RSPLabelProvider_11;
			break;
		case ServerManagementAPIConstants.STATE_STARTING:
			stateString = Messages.RSPLabelProvider_12;
			break;
		case ServerManagementAPIConstants.STATE_STOPPED:
			stateString = Messages.RSPLabelProvider_13;
			break;
		case ServerManagementAPIConstants.STATE_STOPPING:
			stateString = Messages.RSPLabelProvider_14;
			break;

		}
		return stateString.toUpperCase();
	}

	public static String getPublishTypeString(int type) {
		String stateString = Messages.RSPLabelProvider_15;
		switch (type) {
		case ServerManagementAPIConstants.PUBLISH_AUTO:
			stateString = Messages.RSPLabelProvider_16;
			break;
		case ServerManagementAPIConstants.PUBLISH_FULL:
			stateString = Messages.RSPLabelProvider_17;
			break;
		case ServerManagementAPIConstants.PUBLISH_INCREMENTAL:
			stateString = Messages.RSPLabelProvider_18;
			break;
		case ServerManagementAPIConstants.PUBLISH_CLEAN:
			stateString = Messages.RSPLabelProvider_19;
			break;
		}
		return stateString.toUpperCase();
	}

	public static String getPublishStateString(int state) {
		String stateString = Messages.RSPLabelProvider_20;
		switch (state) {
		case ServerManagementAPIConstants.PUBLISH_STATE_ADD:
			stateString = Messages.RSPLabelProvider_21;
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_FULL:
			stateString = Messages.RSPLabelProvider_22;
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL:
			stateString = Messages.RSPLabelProvider_23;
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_NONE:
			stateString = Messages.RSPLabelProvider_24;
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_REMOVE:
			stateString = Messages.RSPLabelProvider_25;
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN:
			stateString = Messages.RSPLabelProvider_26;
			break;

		}
		return stateString.toUpperCase();
	}
}
