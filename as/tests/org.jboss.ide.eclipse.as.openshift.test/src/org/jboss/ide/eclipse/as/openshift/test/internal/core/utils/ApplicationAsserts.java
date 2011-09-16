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
package org.jboss.ide.eclipse.as.openshift.test.internal.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.utils.RFC822DateUtils;

/**
 * @author André Dietisheim
 */
public class ApplicationAsserts {

	public static void assertThatContainsApplication(String applicationName, String embedded, String applicationUUID,
			String cartridgeName, String creationTime, List<Application> applications) {
		Application application = getApplication(applicationName, applications);
		if (application == null) {
			fail(MessageFormat.format("Could not find application with name \"{0}\"", applicationName));
		}
		assertApplication(embedded, applicationUUID, cartridgeName, creationTime, application);
	}

	public static void assertThatContainsApplication(String applicationName, List<Application> applications) {
		assertNotNull(getApplication(applicationName, applications));
	}

	private static Application getApplication(String name, List<Application> applications) {
		Application matchingApplication = null;
		for (Application application : applications) {
			if (name.equals(application.getName())) {
				matchingApplication = application;
				break;
			}
		}
		return matchingApplication;
	}
	
	private static void assertApplication(String embedded, String uuid, String cartridgeName,
			String creationTime, Application application) {
		assertEquals(embedded, application.getEmbedded());
		assertEquals(uuid, application.getUUID());
		assertNotNull(application.getCartridge());
		assertEquals(cartridgeName, application.getCartridge().getName());
		try {
			assertEquals(RFC822DateUtils.getDate(creationTime), application.getCreationTime());
		} catch (DatatypeConfigurationException e) {
			fail(e.getMessage());
		}
	}

}
