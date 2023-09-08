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

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.IRspStateController;
import org.jboss.tools.as.rsp.ui.model.IRspStateControllerProvider;
import org.jboss.tools.as.rsp.ui.model.IRspType;
import org.jboss.tools.as.rsp.ui.model.IServerIconProvider;

/**
 * An implementation of an RSP type
 */
public class RspTypeImpl implements IRspType {

	public static final String SYSPROP_USER_HOME = "user.home"; //$NON-NLS-1$
	public static final String DATA_LOCATION_DEFAULT = ".rsp"; //$NON-NLS-1$
	public static final String INSTALLATIONS = ".rspInstalls"; //$NON-NLS-1$
	public static final String EXPANDED = "expanded"; //$NON-NLS-1$
	public static final String DOWNLOADS = "downloads"; //$NON-NLS-1$

	public static final String FILE_DOT_VERSION = ".distribution.version"; //$NON-NLS-1$

	private final IServerIconProvider iconProvider;
	private final String name;
	private final String id;
	private final IRspCore model;
	private IRspStateControllerProvider controllerProvider;

	public RspTypeImpl(IRspCore model, String id, String name, IServerIconProvider iconProvider,
			IRspStateControllerProvider controllerProvider) {
		this.model = model;
		this.id = id;
		this.name = name;
		this.iconProvider = iconProvider;
		this.controllerProvider = controllerProvider;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Image getIcon() {
		return iconProvider.getIcon();
	}

	@Override
	public Image getIcon(String serverTypeId) {
		Image rspIcon = iconProvider.getIcon(serverTypeId);
		if (rspIcon == null) {
			return RspUiActivator.getDefault().getSharedImages().image("server-dark-24x24.png"); //$NON-NLS-1$
		}
		return rspIcon;
	}

	public static File getServerTypeInstallLocation(IRspType type) {
		File home = new File(System.getProperty(SYSPROP_USER_HOME));
		File root = new File(home, DATA_LOCATION_DEFAULT);
		File installs = new File(root, INSTALLATIONS);
		File expanded = new File(installs, EXPANDED);
		File unzipLoc = new File(expanded, type.getId());
		return unzipLoc;
	}

	@Override
	public String getServerHome() {
		File unzipLoc = getServerTypeInstallLocation(this);
		if (unzipLoc.exists() && unzipLoc.listFiles().length == 1 && unzipLoc.listFiles()[0].isDirectory()) {
			return unzipLoc.listFiles()[0].getAbsolutePath();
		}
		return unzipLoc.getAbsolutePath();
	}

	@Override
	public IRsp createRsp(String version, String url) {
		return new RspImpl(model, this, version, url, createController());
	}

	@Override
	public IRsp createRsp() {
		return new RspImpl(model, this, null, null, createController());
	}

	protected IRspStateController createController() {
		return controllerProvider.createController(this);
	}
}
