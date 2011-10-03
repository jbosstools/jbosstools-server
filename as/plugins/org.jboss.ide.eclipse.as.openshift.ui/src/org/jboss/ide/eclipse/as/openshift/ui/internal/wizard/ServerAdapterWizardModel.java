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
import org.jboss.ide.eclipse.as.openshift.core.IDomain;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.IUser;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.UserConfiguration;
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftUIActivator;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.common.ui.preferencevalue.StringPreferenceValue;

/**
 * @author André Dietisheim
 */
public class ServerAdapterWizardModel extends ObservableUIPojo {

	private static final String RHLOGIN_PREFS_KEY = "org.jboss.ide.eclipse.as.openshift.ui.internal.wizard.ServerAdapterWizardModel_RHLOGIN";

	public static final String PROPERTY_SERVER_URL = "serverUrl";
	public static final String PROPERTY_RHLOGIN = "rhLogin";
	public static final String PROPERTY_PASSWORD = "password";
	public static final String PROPERTY_CREDENTIALS_VALIDITY = "credentialsValidity";
	public static final String PROPERTY_NAMESPACE = "namespace";
	public static final String PROPERTY_SSHKEY = "sshKey";

	private String serverUrl;
	private String rhLogin;
	private String password;
	private IStatus credentialsValidity;
	private IUser user;
	private String namespace;
	private StringPreferenceValue rhLoginPreferenceValue;
	private IDomain domain;
	private String sshKey;

	public ServerAdapterWizardModel() {
		this.serverUrl = IOpenshiftService.BASE_URL;
		this.rhLoginPreferenceValue = new StringPreferenceValue(RHLOGIN_PREFS_KEY, OpenshiftUIActivator.PLUGIN_ID);
		this.rhLogin = initRhLogin();
	}

	protected String initRhLogin() {
		String rhLogin = null;
		rhLogin = rhLoginPreferenceValue.get();
		if (rhLogin == null) {
			rhLogin = getUserConfiguration();
		}
		return rhLogin;
	}

	protected String getUserConfiguration() {
		String configuredUsername = null;
		try {
			configuredUsername = new UserConfiguration().getRhlogin();
		} catch (Exception e) {
			// do nothing
		}
		return configuredUsername;
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
		rhLoginPreferenceValue.store(rhLogin);
		firePropertyChange(PROPERTY_RHLOGIN, this.rhLogin, this.rhLogin = rhLogin);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		firePropertyChange(PROPERTY_PASSWORD, this.password, this.password = password);
	}

	private void setCredentialsStatus(IStatus status) {
		firePropertyChange(PROPERTY_CREDENTIALS_VALIDITY, this.credentialsValidity,
				this.credentialsValidity = status);
	}

	public IStatus getCredentialsValidity() {
		return credentialsValidity;
	}

	public void validateCredentials() {
		IStatus status = new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID, "Your credentails are not valid.");
		try {
			this.user = new User(getRhLogin(), getPassword());
			if (user.isValid()) {
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

	public String getNamespace() {
		return this.namespace;
	}

	public void createDomain() throws OpenshiftException {
		this.domain = getUser().createDomain(namespace, loadSshKey());
	}

	public String getSshKey() {
		return sshKey;
	}

	public void setSshKey(String sshKey) {
		firePropertyChange(PROPERTY_SSHKEY, this.sshKey, this.sshKey = sshKey);
	}

	private ISSHPublicKey loadSshKey() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void renameDomain() throws OpenshiftException {
		IDomain domain = getUser().getDomain();
		domain.setNamespace(namespace);
	}

	public void setNamespace(String namespace) throws OpenshiftException {
		firePropertyChange(PROPERTY_NAMESPACE, this.namespace, this.namespace = namespace);
	}

	public void loadDomain() throws OpenshiftException {
		this.domain = getUser().getDomain();
		setNamespace(domain.getNamespace());
	}
	
}
