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

import java.io.File;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.impl.RspTypeImpl;
import org.jboss.tools.as.rsp.ui.util.VersionComparatorUtil;

public class DownloadRspAction extends AbstractTreeAction {

	public DownloadRspAction(ISelectionProvider provider) {
		super(provider, "Download / Update RSP");
	}

	@Override
	protected boolean isEnabled(Object[] o2) {
		Object o = (o2 != null && o2.length == 1 ? o2[0] : null);
		boolean isRSP = o instanceof IRsp;
		if (!isRSP)
			return false;

		boolean downloadMissing = ((IRsp) o).getState() == IRspCore.IJServerState.MISSING;
		if (downloadMissing)
			return true;

		IRsp server = (IRsp) o;
		String installed = server.getInstalledVersion();
		String latest = server.getLatestVersion();
		if (server.getLatestVersion() == null)
			return false;

		if (!server.exists() || installed == null || VersionComparatorUtil.isGreaterThan(latest, installed.trim())) {
			return true;
		}

		return false;
	}

	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, IRsp.class);
	}

	@Override
	protected void singleSelectionActionPerformed(Object selected) {
		if (selected instanceof IRsp) {
			IRsp server = (IRsp) selected;
			String installed = server.getInstalledVersion();
			String latest = server.getLatestVersion();
			if (!server.exists() || installed == null
					|| VersionComparatorUtil.isGreaterThan(latest, installed.trim())) {
				String home = server.getRspType().getServerHome();
				new Thread("Updating RSP " + server.getRspType().getName()) {
					public void run() {
						deleteDirectory(RspTypeImpl.getServerTypeInstallLocation(server.getRspType()));
//                        TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_DOWNLOAD_RSP,
//                                server.getRspType().getId(), (Status)null, null, new String[]{"version"}, new String[]{latest});
						server.download();
					}
				}.start();
			}
		}
	}

	boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}
