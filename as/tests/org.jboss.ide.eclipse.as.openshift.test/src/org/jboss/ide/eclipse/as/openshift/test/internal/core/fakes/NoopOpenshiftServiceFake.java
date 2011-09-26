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
package org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes;

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;

/**
 * @author André Dietisheim
 */
public class NoopOpenshiftServiceFake extends OpenshiftService {

	@Override
	public UserInfo getUserInfo(User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Cartridge> getCartridges(User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Application createApplication(String name, Cartridge cartridge, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void destroyApplication(String name, Cartridge cartridge, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Application startApplication(String name, Cartridge cartridge, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Application restartApplication(String name, Cartridge cartridge, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Application stopApplication(String name, Cartridge cartridge, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getStatus(String applicationName, Cartridge cartridge, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Domain changeDomain(String domainName, ISSHPublicKey sshKey, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Domain createDomain(String name, ISSHPublicKey keyPair, User user) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}
}
