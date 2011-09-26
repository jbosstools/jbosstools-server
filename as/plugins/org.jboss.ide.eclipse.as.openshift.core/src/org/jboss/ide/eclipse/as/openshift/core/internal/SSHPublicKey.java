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

import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;

/**
 * @author André Dietisheim
 */
public class SSHPublicKey implements ISSHPublicKey {

	private String publicKey;

	public SSHPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPublicKey() {
		return publicKey;
	}
	
	void update(String publicKey) {
	}

	public void update(ISSHPublicKey sshPublicKey) throws OpenshiftException {
		this.publicKey = sshPublicKey.getPublicKey();
	}
}
