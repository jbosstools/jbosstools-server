/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.client;

import java.text.MessageFormat;
import java.util.Date;

import org.jboss.tools.openshift.express.client.ApplicationLogReader;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.ICartridge;
import org.jboss.tools.openshift.express.client.IDomain;
import org.jboss.tools.openshift.express.client.IOpenshiftService;
import org.jboss.tools.openshift.express.client.OpenshiftException;

/**
 * @author André Dietisheim
 */
public class Application extends UserInfoAware implements IApplication {

	private static final String GIT_URI_PATTERN = "ssh://{0}@{1}-{2}.{3}/~/git/{1}.git/";
	private static final String APPLICATION_URL_PATTERN = "http://{0}-{1}.{2}/";

	private String name;
	private ICartridge cartridge;
	private IOpenshiftService service;
	private ApplicationLogReader logReader;
	private ApplicationInfo applicationInfo;

	public Application(String name, ICartridge cartridge, InternalUser user, IOpenshiftService service) {
		this(name, cartridge, null, user, service);
	}

	public Application(String name, ICartridge cartridge, ApplicationInfo applicationInfo, InternalUser user,
			IOpenshiftService service) {
		super(user);
		this.name = name;
		this.cartridge = cartridge;
		this.applicationInfo = applicationInfo;
		this.service = service;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getUUID() throws OpenshiftException {
		return getApplicationInfo().getUuid();
	}

	@Override
	public ICartridge getCartridge() {
		return cartridge;
	}

	@Override
	public String getEmbedded() throws OpenshiftException {
		return getApplicationInfo().getEmbedded();
	}

	@Override
	public Date getCreationTime() throws OpenshiftException {
		 ApplicationInfo applicationInfo = getApplicationInfo();
		 if (applicationInfo == null) {
			 throw new OpenshiftException("Could not find info for application {0}", getName());
		 }
		 return applicationInfo.getCreationTime();
	}

	@Override
	public void destroy() throws OpenshiftException {
		service.destroyApplication(name, cartridge, getUser());
	}

	@Override
	public void start() throws OpenshiftException {
		service.startApplication(name, cartridge, getUser());
	}

	@Override
	public void restart() throws OpenshiftException {
		service.restartApplication(name, cartridge, getUser());
	}

	@Override
	public void stop() throws OpenshiftException {
		service.stopApplication(name, cartridge, getUser());
	}

	@Override
	public ApplicationLogReader getLogReader() throws OpenshiftException {
		if (logReader == null) {
			this.logReader = new ApplicationLogReader(this, getUser(), service);
		}
		return logReader;
	}

	@Override
	public String getGitUri() throws OpenshiftException {
		IDomain domain = getUser().getDomain();
		if (domain == null) {
			return null;
		}
		return MessageFormat
				.format(GIT_URI_PATTERN, getUUID(), getName(), domain.getNamespace(), domain.getRhcDomain());
	}

	@Override
	public String getApplicationUrl() throws OpenshiftException {
		IDomain domain = getUser().getDomain();
		if (domain == null) {
			return null;
		}
		return MessageFormat.format(APPLICATION_URL_PATTERN, name, domain.getNamespace(), domain.getRhcDomain());
	}

	protected IOpenshiftService getService() {
		return service;
	}
	
	protected ApplicationInfo getApplicationInfo() throws OpenshiftException {
		if (applicationInfo == null) {
			this.applicationInfo = getUserInfo().getApplicationInfoByName(getName());
		}
		return applicationInfo;
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
