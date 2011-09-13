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
public interface IOpenshiftService {

	public UserInfo getUserInfo() throws OpenshiftException;

	public List<Cartridge> getCartridges() throws OpenshiftException;  

	public Application createApplication(String name, Cartridge cartridge) throws OpenshiftException;

	public Application destroyApplication(String name, Cartridge cartridge) throws OpenshiftException;

	public Application startApplication(String name, Cartridge cartridge) throws OpenshiftException;
	
	public Application restartApplication(String name, Cartridge cartridge) throws OpenshiftException;

	public Application stopApplication(String name, Cartridge cartridge) throws OpenshiftException;

	public Status getStatus(Application application) throws OpenshiftException;

	public Domain changeDomain(String domainName, SSHKey sshKey) throws OpenshiftException;

	public Domain createDomain(String name, SSHKey keyPair) throws OpenshiftException;

	public SSHKey createKey(String passPhrase, String privateKeyPath, String publicKeyPath) throws OpenshiftException;
	
	public SSHKey loadKey(String privateKeyPath, String publicKeyPath) throws OpenshiftException;
}
