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
package org.jboss.ide.eclipse.as.openshift.internal.core.request;

import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.IOpenshiftRequest;

/**
 * @author André Dietisheim
 */
public class AbstractOpenshiftRequest implements IOpenshiftRequest {

	private String rhlogin;
	private boolean debug;

	public AbstractOpenshiftRequest(String username) {
		this(username, false);
	}

	public AbstractOpenshiftRequest(String username, boolean debug) {
		this.rhlogin = username;
		this.debug = debug;
	}

	public String getRhLogin() {
		return rhlogin;
	}

	public boolean isDebug() {
		return debug;
	}
	
}
