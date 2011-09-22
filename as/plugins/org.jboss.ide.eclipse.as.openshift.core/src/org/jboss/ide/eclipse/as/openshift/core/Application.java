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

import java.text.MessageFormat;
import java.util.Date;

import org.jboss.ide.eclipse.as.openshift.core.internal.utils.Assert;

/**
 * @author André Dietisheim
 */
public class Application {

	private static final String GIT_URI_PATTERN = "ssh://{0}@{1}-{2}.{3}/~/git/{1}.git/";

	private String name;
	private Cartridge cartridge;
	private String uuid;
	private Date creationTime;
	private String embedded;
	private IOpenshiftService service;
	private ApplicationLogReader logReader;
	
	private User user;

	private boolean userInfoQueried;

	public Application(String name, Cartridge cartridge, IOpenshiftService service) {
		this(name, null, cartridge, null, null, null, service);

		this.userInfoQueried = false;
	}

	public Application(String name, String uuid, Cartridge cartridge, String embedded, Date creationTime, User user,
			IOpenshiftService service) {
		this.name = name;
		this.cartridge = cartridge;
		this.uuid = uuid;
		this.embedded = embedded;
		this.creationTime = creationTime;
		this.user = user;
		this.service = service;

		this.userInfoQueried = true;
	}

	public String getName() {
		return name;
	}

	public String getUUID() throws OpenshiftException {
		updateFromUserInfoIfNeeded();
		return uuid;
	}

	public Cartridge getCartridge() {
		return cartridge;
	}

	public String getEmbedded() throws OpenshiftException {
		updateFromUserInfoIfNeeded();
		return embedded;
	}

	public Date getCreationTime() throws OpenshiftException {
		updateFromUserInfoIfNeeded();
		return creationTime;
	}

	protected User getUser() throws OpenshiftException {
		updateFromUserInfoIfNeeded();
		return user;
	}

	public void destroy() throws OpenshiftException {
		service.destroyApplication(name, cartridge);
	}

	public void start() throws OpenshiftException {
		service.startApplication(name, cartridge);
	}

	public void restart() throws OpenshiftException {
		service.restartApplication(name, cartridge);
	}

	public void stop() throws OpenshiftException {
		service.stopApplication(name, cartridge);
	}

	public ApplicationLogReader getLog() throws OpenshiftException {
		if (logReader == null) {
			this.logReader = new ApplicationLogReader(this, service);
		}
		return logReader;
	}

	public String getGitUri() throws OpenshiftException {
		String namespace = null;
		String rhcDomain = null;
		Domain domain = getDomain();
		if (domain != null) {
			namespace = domain.getNamespace();
			rhcDomain = domain.getRhcDomain();
		}

		return MessageFormat.format(GIT_URI_PATTERN, getUUID(), getName(), namespace, rhcDomain);
	}

	private Domain getDomain() throws OpenshiftException {
		return Assert.assertNotNull(getUser()).getDomain();
	}

	private void updateFromUserInfoIfNeeded() throws OpenshiftException {
		if (!userInfoQueried) {
			updateFrom(service.getUserInfo());
			this.userInfoQueried = true;
		}
	}
	
	private void updateFrom(UserInfo userInfo) {
		updateFrom(userInfo.getApplicationByName(getName()));
		this.user = userInfo.getUser();
	}

	private void updateFrom(Application application) {
		this.cartridge = application.cartridge;
		this.creationTime = application.creationTime;
		this.name = application.name;
		this.uuid = application.uuid;
		
		this.userInfoQueried = false;
	}
}
