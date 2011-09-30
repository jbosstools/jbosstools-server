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
package org.jboss.ide.eclipse.as.openshift.test.internal.core;

import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationAsserts.assertApplication;
import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.CartridgeAsserts.assertThatContainsCartridge;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.IDomain;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.ApplicationInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.InternalUser;
import org.jboss.ide.eclipse.as.openshift.core.internal.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.utils.RFC822DateUtils;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.CartridgeResponseFake;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.NoopOpenshiftServiceFake;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.UserInfoResponseFake;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserTest {

	private OpenshiftService userInfoservice;
	private InternalUser user;

	@Before
	public void setUp() throws OpenshiftException, DatatypeConfigurationException {
		UserInfo userInfo = createUserInfo();
		this.userInfoservice = createUserInfoService(userInfo);
		this.user = new InternalUser(UserInfoResponseFake.RHLOGIN, UserInfoResponseFake.PASSWORD, userInfoservice);
	}

	@Test
	public void canGetUserUUID() throws OpenshiftException {
		assertEquals(UserInfoResponseFake.UUID, user.getUUID());
	}
	
	@Test
	public void canGetPublicKey() throws OpenshiftException {
		ISSHPublicKey key = user.getSshKey();
		assertNotNull(key);
		assertEquals(UserInfoResponseFake.SSH_KEY, key.getPublicKey());
	}

	@Test
	public void canGetDomain() throws OpenshiftException {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		assertEquals(UserInfoResponseFake.RHC_DOMAIN, domain.getRhcDomain());
		assertEquals(UserInfoResponseFake.NAMESPACE, domain.getNamespace());
	}

	@Test
	public void canGetCartridges() throws OpenshiftException {
		OpenshiftService cartridgeListService = new NoopOpenshiftServiceFake() {

			@Override
			public List<ICartridge> getCartridges(InternalUser user) throws OpenshiftException {
				ArrayList<ICartridge> cartridges = new ArrayList<ICartridge>();
				cartridges.add(new Cartridge(CartridgeResponseFake.CARTRIDGE_JBOSSAS70));
				cartridges.add(new Cartridge(CartridgeResponseFake.CARTRIDGE_PERL5));
				cartridges.add(new Cartridge(CartridgeResponseFake.CARTRIDGE_PHP53));
				cartridges.add(new Cartridge(CartridgeResponseFake.CARTRIDGE_RACK11));
				cartridges.add(new Cartridge(CartridgeResponseFake.CARTRIDGE_WSGI32));
				return cartridges;
			}
		};
		InternalUser user = new InternalUser(UserInfoResponseFake.RHLOGIN, UserInfoResponseFake.PASSWORD, cartridgeListService);
		Collection<ICartridge> cartridges = user.getCartridges();
		assertNotNull(cartridges);
		assertEquals(5, cartridges.size());
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_JBOSSAS70, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_PERL5, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_PHP53, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_RACK11, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_WSGI32, cartridges);
	}

	@Test
	public void canGetApplications() throws OpenshiftException {
		/** response is UserInfoResponseFake */
		Collection<IApplication> applications = user.getApplications();
		assertNotNull(applications);
		assertEquals(2, applications.size());
	}

	@Test
	public void canGetApplicationByName() throws OpenshiftException, DatatypeConfigurationException {
		IApplication application = user.getApplicationByName(UserInfoResponseFake.APP2_NAME);
		assertApplication(
				UserInfoResponseFake.APP2_NAME
				, UserInfoResponseFake.APP2_UUID
				, UserInfoResponseFake.APP2_CARTRIDGE
				, UserInfoResponseFake.APP2_EMBEDDED
				, UserInfoResponseFake.APP2_CREATION_TIME
				, application);
	}

	private UserInfo createUserInfo() throws OpenshiftException, DatatypeConfigurationException {
		ApplicationInfo[] applicationInfos = new ApplicationInfo[] {
				new ApplicationInfo(UserInfoResponseFake.APP1_NAME
						, UserInfoResponseFake.APP1_UUID
						, UserInfoResponseFake.APP1_EMBEDDED
						, Cartridge.valueOf(UserInfoResponseFake.APP1_CARTRIDGE)
						, RFC822DateUtils.getDate(UserInfoResponseFake.APP1_CREATION_TIME))
				, new ApplicationInfo(UserInfoResponseFake.APP2_NAME
						, UserInfoResponseFake.APP2_UUID
						, UserInfoResponseFake.APP2_EMBEDDED
						, Cartridge.valueOf(UserInfoResponseFake.APP2_CARTRIDGE)
						, RFC822DateUtils.getDate(UserInfoResponseFake.APP2_CREATION_TIME))
		};
		return new UserInfo(
				UserInfoResponseFake.RHLOGIN
				, UserInfoResponseFake.UUID
				, UserInfoResponseFake.SSH_KEY
				, UserInfoResponseFake.RHC_DOMAIN
				, UserInfoResponseFake.NAMESPACE
				, Arrays.asList(applicationInfos));
	}

	private OpenshiftService createUserInfoService(final UserInfo userInfo) {
		return new NoopOpenshiftServiceFake() {

			@Override
			public UserInfo getUserInfo(InternalUser user) throws OpenshiftException {
				return userInfo;
			}
		};
	}
}
