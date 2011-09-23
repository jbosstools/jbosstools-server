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

	public UserInfo getUserInfo(User user) throws OpenshiftException;

	public List<Cartridge> getCartridges(User user) throws OpenshiftException;  

	public Application createApplication(String name, Cartridge cartridge, User user) throws OpenshiftException;

	public Application destroyApplication(String name, Cartridge cartridge, User user) throws OpenshiftException;

	public Application startApplication(String name, Cartridge cartridge, User user) throws OpenshiftException;
	
	public Application restartApplication(String name, Cartridge cartridge, User user) throws OpenshiftException;

	public Application stopApplication(String name, Cartridge cartridge, User user) throws OpenshiftException;

	public String getStatus(String name, Cartridge cartridge, User user) throws OpenshiftException;

	public Domain changeDomain(String name, ISSHPublicKey sshKey, User user) throws OpenshiftException;

	public Domain createDomain(String name, ISSHPublicKey sshKey, User user) throws OpenshiftException;
}
