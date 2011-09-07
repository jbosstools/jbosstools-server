package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.internal.core.Cartridge;
import org.junit.Before;
import org.junit.Test;

public class OpenshiftServiceIntegrationTest {

	private OpenshiftService openshiftService;
	
	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q3e5t7u";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
	}

	@Test(expected=OpenshiftException.class)
	public void cannotGetUserInfoIfNotAppNorDomainCreated() throws OpenshiftException {
		openshiftService.getUserInfo();
	}

	@Test
	public void canRequestListCartridges() throws Exception {
		List<Cartridge> cartridges = openshiftService.getCartridges();
		assertNotNull(cartridges);
		assertTrue(cartridges.size() > 0);
	}

	@Test
	public void canCreateApplication() throws Exception {
		Application application = openshiftService.createApplication("test-application", Cartridge.JBOSSAS_7);
		assertNotNull(application);
	}
	
}
