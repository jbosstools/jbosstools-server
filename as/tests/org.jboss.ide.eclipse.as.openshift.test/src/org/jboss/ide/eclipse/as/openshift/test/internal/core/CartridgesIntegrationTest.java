package org.jboss.ide.eclipse.as.openshift.test.internal.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CartridgesIntegrationTest {

	private OpenshiftService openshiftService;

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
	}

	@Ignore
	@Test
	public void canRequestListCartridges() throws Exception {
		List<Cartridge> cartridges = openshiftService.getCartridges();
		assertNotNull(cartridges);
		assertTrue(cartridges.size() > 0);
	}
}
