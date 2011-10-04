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

import org.jboss.ide.eclipse.as.openshift.core.IDomain;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.IUser;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;

/**
 * @author André Dietisheim
 */
public class NewDomainWizardPageModel extends ObservableUIPojo {

	public static final String PROPERTY_NAMESPACE = "namespace";
	public static final String PROPERTY_SSHKEY = "sshKey";

	private String namespace;
	private IDomain domain;
	private String sshKey;
	private ServerAdapterWizardModel wizardModel;

	public NewDomainWizardPageModel(ServerAdapterWizardModel wizardModel) {
		this.wizardModel = wizardModel;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public void createDomain() throws OpenshiftException {
		IUser user = wizardModel.getUser();
		this.domain = user.createDomain(namespace, loadSshKey());
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

	public void setNamespace(String namespace) throws OpenshiftException {
		firePropertyChange(PROPERTY_NAMESPACE, this.namespace, this.namespace = namespace);
	}

	public boolean hasDomain() throws OpenshiftException {
		if (!hasUser()) {
			return false;
		}
		return getUser().hasDomain();
	}

	public void loadDomain() throws OpenshiftException {
		if (!hasUser()) {
			return;
		}
		IDomain domain = getUser().getDomain();
		if (domain != null) {
			this.domain = domain;
			setNamespace(domain.getNamespace());
		}
	}

	public boolean hasUser() {
		return wizardModel.getUser() != null;
	}
	
	public IUser getUser() {
		return wizardModel.getUser();
	}
}
