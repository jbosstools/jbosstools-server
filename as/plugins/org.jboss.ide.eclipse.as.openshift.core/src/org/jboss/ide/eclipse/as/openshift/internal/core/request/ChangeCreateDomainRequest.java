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

import org.jboss.ide.eclipse.as.openshift.core.SSHKey;


/**
 * @author André Dietisheim
 */
public class ChangeCreateDomainRequest extends AbstractDomainRequest {

	public ChangeCreateDomainRequest(String name, String namespace, SSHKey sshKey, ApplicationAction action, String username) {
		this(name, namespace, sshKey, username, false);
	}

	public ChangeCreateDomainRequest(String name, String namespace, SSHKey sshKey, String username, boolean debug) {
		super(name, sshKey, username, debug);
	}

	public boolean isAlter() {
		return true;
	}
}
