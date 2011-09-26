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

import org.jboss.ide.eclipse.as.openshift.core.internal.User;

/**
 * @author André Dietisheim
 */
public class TestUser extends User {

	private static final String RHLOGIN = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	public TestUser() {
		super(RHLOGIN, PASSWORD);
	}

	public TestUser(String password) {
		super(RHLOGIN, password);
	}
}
