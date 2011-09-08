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

/**
 * @author André Dietisheim
 */
public class User {

	private String rhlogin;
	private String uuid;

	public User(String rhlogin, String uuid) {
		this.rhlogin = rhlogin;
		this.uuid = uuid;
	}

	public String getRhlogin() {
		return rhlogin;
	}

	public String getUuid() {
		return uuid;
	}
}
