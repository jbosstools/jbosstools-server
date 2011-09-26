/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;

/**
 * @author André Dietisheim
 */
public class User implements IUser {

	private String rhlogin;
	private String password;
	private String uuid;
	private ISSHPublicKey sshKey;
	private Domain domain;
	private UserInfo userInfo;
	private List<Cartridge> cartridges;
	private List<Application> applications = new ArrayList<Application>();

	private OpenshiftService service;

	public User(String rhlogin, String password) {
		this(rhlogin, password, (ISSHPublicKey) null, new OpenshiftService());
	}

	public User(String rhlogin, String password, OpenshiftService service) {
		this(rhlogin, password, (ISSHPublicKey) null, service);
	}

	public User(String rhlogin, String password, ISSHPublicKey sshKey, OpenshiftService service) {
		this.rhlogin = rhlogin;
		this.password = password;
		this.sshKey = sshKey;
		this.service = service;
	}

	@Override
	public Domain getDomain() throws OpenshiftException {
		loadLazyValues();
		return domain;
	}

	@Override
	public ISSHPublicKey getSshKey() throws OpenshiftException {
		loadLazyValues();
		return sshKey;
	}

	@Override
	public String getRhlogin() {
		return rhlogin;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public String getUUID() throws OpenshiftException {
		loadLazyValues();
		return uuid;
	}

	@Override
	public List<Cartridge> getCartridges() throws OpenshiftException {
		if (cartridges == null) {
			this.cartridges = service.getCartridges(this);
		}
		return Collections.unmodifiableList(cartridges);
	}

	@Override
	public Collection<Application> getApplications() throws OpenshiftException {
		loadLazyValues();
		return Collections.unmodifiableList(applications);
	}

	public Application getApplicationByName(String name) throws OpenshiftException {
		loadLazyValues();
		return getApplicationByName(name, applications);
	}

	private Application getApplicationByName(String name, Collection<Application> applications) {
		Application matchingApplication = null;
		for (Application application : applications) {
			if (name.equals(application.getName())) {
				matchingApplication = application;
			}
		}
		return matchingApplication;
	}

	public void add(Application application) {
		applications.add(application);
	}

	public void remove(Application application) {
		applications.remove(application);
	}

	/**
	 * Loads the lazy values from the server if needed. Updates itself all
	 * referenced objects (applications, domain).
	 * 
	 * @throws OpenshiftException
	 *             if an error occurred while loading the values
	 */
	void loadLazyValues() throws OpenshiftException {
		if (userInfo == null) {
			refresh();
		}
	}

	public void refresh() throws OpenshiftException {
		this.userInfo = service.getUserInfo(this);
		update(userInfo);
	}

	private void update(UserInfo userInfo) throws OpenshiftException {
		this.uuid = userInfo.getUuid();
		updateDomain(userInfo);
		updateSshPublicKey(userInfo);
		update(userInfo.getApplicationInfos());
	}

	private void updateDomain(UserInfo userInfo) {
		if (domain == null) {
			this.domain = new Domain(userInfo.getNamespace(), userInfo.getRhcDomain(), this);
		} else {
			domain.update(userInfo);
		}
	}

	private void updateSshPublicKey(UserInfo userInfo) throws OpenshiftException {
		if (sshKey == null) {
			sshKey = userInfo.getSshPublicKey();
		} else {
			sshKey.update(userInfo.getSshPublicKey());
		}
	}

	private void update(List<ApplicationInfo> applicationInfos) {
		for (ApplicationInfo applicationInfo : applicationInfos) {
			Application application = getApplicationByName(applicationInfo.getName(), applications);
			if (application != null) {
				application.update(applicationInfo);
			} else {
				applications.add(createApplication(applicationInfo));
			}
		}
	}

	private Application createApplication(ApplicationInfo applicationInfo) {
		return new Application(applicationInfo.getName()
				, applicationInfo.getUuid()
				, applicationInfo.getCartridge()
				, applicationInfo.getEmbedded()
				, applicationInfo.getCreationTime()
				, this, service);
	}

	public void setSshPublicKey(ISSHPublicKey key) {
		this.sshKey = key;
	}
}
