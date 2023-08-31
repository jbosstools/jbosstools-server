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
package org.jboss.tools.as.rsp.ui.model.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.download.DownloadUtility;
import org.jboss.tools.as.rsp.ui.download.UnzipUtility;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.IRspStartCallback;
import org.jboss.tools.as.rsp.ui.model.IRspStateController;
import org.jboss.tools.as.rsp.ui.model.IRspType;
import org.jboss.tools.as.rsp.ui.model.ServerConnectionInfo;
import org.jboss.tools.as.rsp.ui.model.StartupFailedException;
import org.jboss.tools.as.rsp.ui.util.ui.UIHelper;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;

/**
 * The implementation for an RSP. Most of this implementation should be fairly
 * standard, with the custom bits in the IRspStateController
 */
public class RspImpl implements IRsp, IRspStartCallback {
	private final IRspStateController controller;
	private final IRspCore model;
	private IRspType type;
	private String latestVersion;
	private String downloadUrl;
	private IRspCore.IJServerState currentState;
	private boolean launched;

	public RspImpl(IRspCore model, IRspType type, String latestVersion, String downloadUrl,
			IRspStateController controller) {
		this.model = model;
		this.type = type;
		this.latestVersion = latestVersion;
		this.downloadUrl = downloadUrl;
		this.controller = controller;
		this.currentState = exists() ? IRspCore.IJServerState.STOPPED : IRspCore.IJServerState.MISSING;
	}

	@Override
	public IRspCore getModel() {
		return model;
	}

	@Override
	public IRspType getRspType() {
		return type;
	}

	@Override
	public String getLatestVersion() {
		return latestVersion;
	}

	@Override
	public String getInstalledVersion() {
		String home = getRspType().getServerHome();
		File dotVersion = new File(home, RspTypeImpl.FILE_DOT_VERSION);
		if (dotVersion.exists()) {
			String ret = readFileContent(dotVersion.toPath());
			return ret == null ? null : ret.trim();
		}
		return null;
	}

	private String readFileContent(Path filePath) {
		try {
			return new String(Files.readAllBytes(filePath));
		} catch (IOException e) {
			RspUiActivator.getDefault().getLog().log(
					StatusFactory.errorStatus(RspUiActivator.PLUGIN_ID, "Error reading file " + filePath, e));
			return null;
		}
	}

	@Override
	public ServerConnectionInfo start() {
		try {
			return getController().start(this);
		} catch (StartupFailedException sfe) {
			UIHelper.executeInUI(() -> {
				RspUiActivator.log(StatusFactory.errorStatus(RspUiActivator.PLUGIN_ID, "Unable to start RSP", sfe));
			});
			return null;
		}
	}

	@Override
	public void terminate() {
		getController().terminate(this);
	}

	@Override
	public void stop() {
		updateRspState(IRspCore.IJServerState.STOPPING);
		RspClientLauncher client = model.getClient(this);
		if (client != null) {
			if (client.getServerProxy() != null) {
				client.getServerProxy().shutdown();
				return;
			}
		}
		terminate();
	}

	@Override
	public IRspCore.IJServerState getState() {
		return currentState;
	}

	@Override
	public boolean wasLaunched() {
		return this.launched;
	}

	@Override
	public boolean exists() {
		File f = new File(this.type.getServerHome());
		boolean b = f.exists();
		return b;
	}

	@Override
	public void download() {
		boolean b = exists();
		if (!b && latestVersion != null && downloadUrl != null) {
			final String serverHome = this.type.getServerHome();
			String name = "Downloading " + getRspType().getName();
			new Job(name) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					File toDl = getRspDownloadLocation();
					toDl.getParentFile().mkdirs();
					File toExtract = new File(serverHome);
					try {
						new DownloadUtility().download(downloadUrl, toDl.toPath(), monitor);
						if (toDl.exists()) {
							UnzipUtility util = new UnzipUtility(toDl);
							util.extract(toExtract);
							String root = util.getRoot();
							File extractedRoot = toExtract.toPath().resolve(root).toFile();
							File dotVersion = new File(extractedRoot, RspTypeImpl.FILE_DOT_VERSION);
							if (!dotVersion.exists()) {
								Files.write(dotVersion.toPath(), getLatestVersion().getBytes());
							}
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					if (exists()) {
						updateRspState(IRspCore.IJServerState.STOPPED);
					} else {
						updateRspState(IRspCore.IJServerState.MISSING);
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	protected File getRspDownloadLocation() {
		File home = new File(System.getProperty(RspTypeImpl.SYSPROP_USER_HOME));
		File root = new File(home, RspTypeImpl.DATA_LOCATION_DEFAULT);
		File installs = new File(root, RspTypeImpl.INSTALLATIONS);
		File downloads = new File(installs, RspTypeImpl.DOWNLOADS);
		File dlFile = new File(downloads, getRspType().getId() + "-" + getLatestVersion() + ".zip");
		return dlFile;
	}

	protected IRspStateController getController() {
		return controller;
	}

	public void updateRspState(IRspCore.IJServerState state) {
		this.currentState = state;
		model.stateUpdated(this);
	}

	@Override
	public void updateRspState(IRspCore.IJServerState state, boolean launched) {
		this.currentState = state;
		this.launched = launched;
		model.stateUpdated(this);
	}
}
