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
package org.jboss.ide.eclipse.as.openshift.core.internal;

import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;

public class UserInfoAware {

	private UserInfo userInfo;
	private User user;
	
	protected UserInfoAware(User user) {
		this.user = user;
	}

	protected UserInfo getUserInfo() throws OpenshiftException {
		if (userInfo == null) {
			this.userInfo = user.getUserInfo();
		}
		return userInfo;
	}

	protected User getUser() {
		return user;
	}
	
}
