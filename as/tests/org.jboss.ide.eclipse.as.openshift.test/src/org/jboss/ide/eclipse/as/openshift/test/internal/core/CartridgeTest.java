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

import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.CartridgeAsserts.assertThatContainsCartridge;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.ListCartridgesResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.CartridgeResponseFake;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class CartridgeTest {

	@Test
	public void canUnmarshallApplicationResponse() throws OpenshiftException {
		String response = JsonSanitizer.sanitize(CartridgeResponseFake.RESPONSE);
		OpenshiftResponse<List<Cartridge>> openshiftResponse =
				new ListCartridgesResponseUnmarshaller().unmarshall(response);
		List<Cartridge> cartridges = openshiftResponse.getOpenshiftObject();
		assertNotNull(cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_JBOSSAS70, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_PERL5, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_PHP53, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_RACK11, cartridges);
		assertThatContainsCartridge(CartridgeResponseFake.CARTRIDGE_WSGI32, cartridges);
	}
}
