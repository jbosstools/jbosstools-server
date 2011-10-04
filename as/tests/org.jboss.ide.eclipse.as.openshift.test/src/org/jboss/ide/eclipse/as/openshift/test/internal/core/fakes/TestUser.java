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

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationUtils;

/**
 * @author André Dietisheim
 */
public class TestUser extends User {

	public static final String RHLOGIN_USER_WITHOUT_DOMAIN = "toolsjboss.no.domain2@gmail.com";
	public static final String PASSWORD_USER_WITHOUT_DOMAIN = "1q2w3e";

	public static final String RHLOGIN = "toolsjboss@gmail.com";
	public static final String PASSWORD = "1q2w3e";

	public TestUser() {
		super(RHLOGIN, PASSWORD);
	}

	public TestUser(String password) {
		super(RHLOGIN, password);
	}

	public TestUser(String rhlogin, String password) {
		super(rhlogin, password);
	}

	public TestUser(String rhlogin, String password, String url) {
		super(rhlogin, password, url);
	}
	
	public IApplication createTestApplication() throws OpenshiftException {
		return createApplication(ApplicationUtils.createRandomApplicationName(), Cartridge.JBOSSAS_7);
	}

	public void silentlyDestroyApplication(IApplication application) {
		try {
			getService().destroyApplication(application.getName(), application.getCartridge(), this);
		} catch (OpenshiftException e) {
			e.printStackTrace();
		}
	}
}
