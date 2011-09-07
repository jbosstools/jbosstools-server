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
public class InvalidCredentialsOpenshiftException extends OpenshiftEndpointException {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;

	public  InvalidCredentialsOpenshiftException(String url, Throwable cause, String username, String password, String message, String... arguments) {
		super(url, cause, message);
		this.username = username;
		this.password = password;
	}

	protected String getUsername() {
		return username;
	}

	protected String getPassword() {
		return password;
	}
}
