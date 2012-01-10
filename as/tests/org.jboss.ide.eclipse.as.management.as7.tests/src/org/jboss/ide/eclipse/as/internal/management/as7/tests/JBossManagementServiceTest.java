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
package org.jboss.ide.eclipse.as.internal.management.as7.tests;

import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author André Dietisheim
 */
public class JBossManagementServiceTest {

	private static final String DS_BUNDLEID = "org.eclipse.equinox.ds";
	private static final String AS7MANAGER_PLUGINID = "org.jboss.ide.eclipse.as.management.as71";

	@Before
	public void setUp() throws UnknownHostException {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void serviceIsReachable() throws BundleException {
		ensureDSIsRunning();
		ensureServiceBundleIsRunning();
		BundleContext context = Activator.getContext();
		ServiceReference<IJBoss7ManagerService> reference =
				context.getServiceReference(IJBoss7ManagerService.class);
		assertNotNull(reference);
		IJBoss7ManagerService service = context.getService(reference);
		assertNotNull(service);
	}

	private void ensureServiceBundleIsRunning() throws BundleException {
		BundleContext context = Activator.getContext();
		assertNotNull("bundle of this test is not active", context);
		Bundle bundle = getBundle(AS7MANAGER_PLUGINID);
		assertNotNull(AS7MANAGER_PLUGINID + " not installed", bundle);
		startBundle(bundle);
	}

	private void ensureDSIsRunning() throws BundleException {
		BundleContext context = Activator.getContext();
		assertNotNull("bundle of this test is not active", context);
		Bundle bundle = getBundle(DS_BUNDLEID);
		assertNotNull(
				DS_BUNDLEID + " not installed. You have to install the declarative services daemon so that "
						+ IJBoss7ManagerService.class + " service is registered"
				, bundle);
		startBundle(bundle);
	}

	private void startBundle(Bundle bundle) throws BundleException {
		if (bundle.getState() != Bundle.ACTIVE) {
			bundle.start();
		}
	}

	private Bundle getBundle(String id) {
		Bundle bundleFound = null;
		for (Bundle bundle : Activator.getContext().getBundles()) {
			if (id.equals(bundle.getSymbolicName())) {
				bundleFound = bundle;
				break;
			}
		}
		return bundleFound;
	}
}
