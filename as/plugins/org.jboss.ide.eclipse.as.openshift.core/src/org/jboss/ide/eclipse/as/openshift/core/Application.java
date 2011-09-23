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

	public Application(String name, Cartridge cartridge, User user, IOpenshiftService service) {
		this(name, null, cartridge, null, null, user, service);
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

		// add ourselves to the user!
		user.add(this); 

	}

	public String getName() {
		return name;
	}

	public String getUUID() throws OpenshiftException {
		user.loadLazyValues();
		return uuid;
	}

	public Cartridge getCartridge() {
		return cartridge;
	}

	public String getEmbedded() throws OpenshiftException {
		user.loadLazyValues();
		return embedded;
	}

	public Date getCreationTime() throws OpenshiftException {
		user.loadLazyValues();
		return creationTime;
	}

	protected IUser getUser() throws OpenshiftException {
		return user;
	}

	public void destroy() throws OpenshiftException {
		service.destroyApplication(name, cartridge, user);
	}

	public void start() throws OpenshiftException {
		service.startApplication(name, cartridge, user);
	}

	public void restart() throws OpenshiftException {
		service.restartApplication(name, cartridge, user);
	}

	public void stop() throws OpenshiftException {
		service.stopApplication(name, cartridge, user);
	}

	public ApplicationLogReader getLog() throws OpenshiftException {
		if (logReader == null) {
			this.logReader = new ApplicationLogReader(this, user, service);
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

	void update(ApplicationInfo applicationInfo) {
		if (applicationInfo == null) {
			return;
		}
		this.cartridge = applicationInfo.getCartridge();
		this.creationTime = applicationInfo.getCreationTime();
		this.name = applicationInfo.getName();
		this.uuid = applicationInfo.getUuid();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		Application other = (Application) object;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
