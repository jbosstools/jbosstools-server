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
package org.jboss.tools.openshift.express.internal.client;

import org.jboss.tools.openshift.express.client.OpenshiftException;

public class UserInfoAware {

	private UserInfo userInfo;
	private InternalUser user;
	
	protected UserInfoAware(InternalUser user) {
		this.user = user;
	}

	protected UserInfo getUserInfo() throws OpenshiftException {
		if (userInfo == null) {
			this.userInfo = user.refreshUserInfo();
		}
		return userInfo;
	}

	protected InternalUser getUser() {
		return user;
	}
	
}
