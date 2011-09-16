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
package org.jboss.ide.eclipse.as.openshift.core;

import java.util.List;

/**
 * @author André Dietisheim
 */
public class UserInfo {

	private User user;
	private List<Application> applications;

	public UserInfo(User user, List<Application> applications) {
		this.user = user;
		this.applications = applications;
	}

	protected User getUser() {
		return user;
	}

	protected List<Application> getApplications() {
		return applications;
	}

}
