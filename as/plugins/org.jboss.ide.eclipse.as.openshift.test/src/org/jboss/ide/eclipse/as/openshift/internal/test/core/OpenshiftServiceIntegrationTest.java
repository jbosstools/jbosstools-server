package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.InvalidCredentialsOpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.internal.core.Cartridge;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class OpenshiftServiceIntegrationTest {

	private OpenshiftService openshiftService;
	private OpenshiftService invalidCredentialsOpenshiftService;

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
		this.invalidCredentialsOpenshiftService = new OpenshiftService(USERNAME, "bogus");
	}

	@Ignore
	@Test(expected = OpenshiftException.class)
	public void cannotGetUserInfoIfNotAppNorDomainCreated() throws OpenshiftException {
		openshiftService.getUserInfo();
	}

	@Ignore
	@Test
	public void canRequestListCartridges() throws Exception {
		List<Cartridge> cartridges = openshiftService.getCartridges();
		assertNotNull(cartridges);
		assertTrue(cartridges.size() > 0);
	}

	@Test(expected = InvalidCredentialsOpenshiftException.class)
	public void createApplicationWithInvalidCredentialsThrowsException() throws Exception {
		invalidCredentialsOpenshiftService.createApplication(createRandomApplicationName(), Cartridge.JBOSSAS_7);
	}

	@Test
	public void canCreateApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			Cartridge cartridge = Cartridge.JBOSSAS_7;
			Application application = openshiftService.createApplication(applicationName, cartridge);
			assertNotNull(application);
			assertEquals(applicationName, application.getName());
			assertEquals(cartridge, application.getCartridge());
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
		}
	}

	@Test
	public void canDestroyApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
		openshiftService.destroyApplication(applicationName, Cartridge.JBOSSAS_7);
	}

	@Test(expected = OpenshiftException.class)
	public void createDuplicateApplicationThrowsException() throws Exception {
		String applicationName = createRandomApplicationName();
		openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
		openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
	}

	private String createRandomApplicationName() {
		return String.valueOf(System.currentTimeMillis());
	}

	private void silentlyDestroyApplication(String name, IOpenshiftService service) {
		try {
			service.destroyApplication(name, Cartridge.JBOSSAS_7);
		} catch (OpenshiftException e) {
			e.printStackTrace();
		}

	}
}
