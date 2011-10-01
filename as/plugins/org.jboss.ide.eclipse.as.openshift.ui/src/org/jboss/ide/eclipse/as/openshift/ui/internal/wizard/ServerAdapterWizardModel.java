/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.ui.internal.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.IUser;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftUIActivator;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;

/**
 * @author André Dietisheim
 */
public class ServerAdapterWizardModel extends ObservableUIPojo {

	public static final String PROPERTY_SERVER_URL = "serverUrl";
	public static final String PROPERTY_RHLOGIN = "rhLogin";
	public static final String PROPERTY_PASSWORD = "password";
	public static final String PROPERTY_CREDENTIALSVALIDITY = "credentialsValidity";

	private String serverUrl;
	private String rhLogin;
	private String password;
	private IStatus credentialsValidity;
	private IUser user;

	public ServerAdapterWizardModel() {
		this.serverUrl = IOpenshiftService.BASE_URL;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		firePropertyChange(PROPERTY_SERVER_URL, this.serverUrl, this.serverUrl = serverUrl);
	}

	public String getRhLogin() {
		return rhLogin;
	}

	public void setRhLogin(String rhLogin) {
		firePropertyChange(PROPERTY_RHLOGIN, this.rhLogin, this.rhLogin = rhLogin);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		firePropertyChange(PROPERTY_PASSWORD, this.password, this.password = password);
	}

	private void setCredentialsStatus(IStatus status) {
		firePropertyChange(PROPERTY_CREDENTIALSVALIDITY, this.credentialsValidity,
				this.credentialsValidity = status);
	}

	public IStatus getCredentialsValidity() {
		return credentialsValidity;
	}

	public void validateCredentials() {
		IStatus status = new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID, "Your credentails are not valid.");
		try {
			this.user = new User(getRhLogin(), getPassword());
			if(user.isValid()) {
				status = Status.OK_STATUS;
			}
		} catch (OpenshiftException e) {
			this.user = null;
		}
		setCredentialsStatus(status);
	}

	public IUser getUser() {
		return user;
	}
}
