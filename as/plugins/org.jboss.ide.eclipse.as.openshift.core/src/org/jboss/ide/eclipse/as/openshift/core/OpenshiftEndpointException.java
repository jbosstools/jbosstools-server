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
public class OpenshiftEndpointException extends OpenshiftException {

	private static final long serialVersionUID = 1L;
	private String url;

	public OpenshiftEndpointException(String url, Throwable cause, String message, Object... arguments) {
		super(cause, message, arguments);
		this.url = url;
	}

	protected String getUrl() {
		return url;
	}

}
