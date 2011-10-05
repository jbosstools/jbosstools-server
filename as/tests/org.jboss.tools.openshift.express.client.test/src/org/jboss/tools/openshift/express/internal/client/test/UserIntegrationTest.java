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
package org.jboss.tools.openshift.express.internal.client.test;

import static org.jboss.tools.openshift.express.internal.client.test.utils.ApplicationAsserts.assertApplication;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.xml.datatype.DatatypeConfigurationException;

import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.ICartridge;
import org.jboss.tools.openshift.express.client.IDomain;
import org.jboss.tools.openshift.express.client.ISSHPublicKey;
import org.jboss.tools.openshift.express.client.NotFoundOpenshiftException;
import org.jboss.tools.openshift.express.client.OpenshiftEndpointException;
import org.jboss.tools.openshift.express.client.OpenshiftException;
import org.jboss.tools.openshift.express.client.OpenshiftService;
import org.jboss.tools.openshift.express.client.User;
import org.jboss.tools.openshift.express.internal.client.test.fakes.TestUser;
import org.jboss.tools.openshift.express.internal.client.test.utils.ApplicationUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserIntegrationTest {

	private User user;
	private TestUser invalidUser;
	private TestUser badUrlUser;
	private TestUser userWithoutDomain;

	@Before
	public void setUp() throws OpenshiftException, DatatypeConfigurationException {
		this.user = new TestUser();
		this.invalidUser = new TestUser("bogusPassword");
		this.badUrlUser = new TestUser(TestUser.RHLOGIN, TestUser.PASSWORD, "http://www.redhat.com");
		this.userWithoutDomain = new TestUser(TestUser.RHLOGIN_USER_WITHOUT_DOMAIN, TestUser.PASSWORD_USER_WITHOUT_DOMAIN);
	}

	@Test
	public void canCheckIfUserIsValid() throws OpenshiftException {
		assertTrue(user.isValid());
	}

	@Test
	public void throwsExceptionIfInvalidCredentials() throws OpenshiftException {
		assertFalse(invalidUser.isValid());
	}

	@Test(expected = NotFoundOpenshiftException.class)
	public void throwsExceptionIfBadUrl() throws OpenshiftException {
		badUrlUser.isValid();
	}

	@Test
	public void canGetUserUUID() throws OpenshiftException {
		String uuid = user.getUUID();
		assertNotNull(uuid);
		assertTrue(uuid.length() > 0);
	}

	@Test
	public void canGetPublicKey() throws OpenshiftException {
		ISSHPublicKey key = user.getSshKey();
		assertNotNull(key);
		assertNotNull(key.getPublicKey());
		assertTrue(key.getPublicKey().length() > 0);
	}

	@Test
	public void canGetDomain() throws OpenshiftException {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		assertNotNull(domain.getRhcDomain());
		assertTrue(domain.getRhcDomain().length() > 0);
		assertNotNull(domain.getNamespace());
		assertTrue(domain.getNamespace().length() > 0);
	}

	@Test(expected = OpenshiftEndpointException.class)
	public void cannotCreateDomainIfAlreadyExists() throws OpenshiftException {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		ISSHPublicKey key = user.getSshKey();
		assertNotNull(key);
		user.createDomain("newDomain", key);
	}

	@Test
	public void getNullIfNoDomainPresent() throws OpenshiftException {
		IDomain domain = userWithoutDomain.getDomain();
		assertNull(domain);
	}

	@Test
	public void canGetCartridges() throws OpenshiftException {
		Collection<ICartridge> cartridges = user.getCartridges();
		assertNotNull(cartridges);
		assertTrue(cartridges.size() >= 5);
	}

	@Test
	public void canGetApplications() throws OpenshiftException {
		Collection<IApplication> applications = user.getApplications();
		assertNotNull(applications);
	}

	@Test
	public void canCreateApplication() throws OpenshiftException {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Collection<IApplication> applications = user.getApplications();
			assertNotNull(applications);
			int numOfApplications = applications.size();
			IApplication application = user.createApplication(applicationName, ICartridge.JBOSSAS_7);
			assertEquals(numOfApplications + 1, applications.size());
			assertApplication(applicationName, ICartridge.JBOSSAS_7.getName(), application);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, new OpenshiftService());
		}
	}

	@Test
	public void canGetApplicationByName() throws OpenshiftException, DatatypeConfigurationException {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = user.createApplication(applicationName, ICartridge.JBOSSAS_7);
			IApplication applicationFound = user.getApplicationByName(applicationName);
			assertNotNull(applicationFound);
			assertEquals(application, applicationFound);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, new OpenshiftService());
		}
	}
}
