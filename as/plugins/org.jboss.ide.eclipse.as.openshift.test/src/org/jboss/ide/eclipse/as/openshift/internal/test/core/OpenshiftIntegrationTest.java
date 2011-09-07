package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import org.jboss.ide.eclipse.as.openshift.core.Openshift;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.internal.core.Cartridge;
import org.junit.Before;
import org.junit.Test;

public class OpenshiftIntegrationTest {

	private Openshift openshift;
	
	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q3e5t7u";

	@Before
	public void setUp() {
		this.openshift = new Openshift(USERNAME, PASSWORD);
	}

	@Test(expected=OpenshiftException.class)
	public void cannotGetUserInfoIfNotAppNorDomainCreated() throws OpenshiftException {
		openshift.getUserInfo();
	}

	@Test
	public void canRequestListCartridges() throws Exception {
		openshift.getCartridges();		
	}

	@Test
	public void canCreateApplication() throws Exception {
		openshift.createApplication("test-application", new Cartridge("jbossas-7.0"));		
	}
	
}
