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
public interface ISSHPublicKey {

	/**
	 * Returns the content of the public key (key content without ssh-rsa
	 * identifier nor comment) of the ssh key
	 * 
	 * @return the content of the public key (without signature, without
	 *         comment)
	 * @throws OpenshiftException
	 */
	public String getPublicKey() throws OpenshiftException ;
	
	public void update(ISSHPublicKey sshPublicKey) throws OpenshiftException;
}
