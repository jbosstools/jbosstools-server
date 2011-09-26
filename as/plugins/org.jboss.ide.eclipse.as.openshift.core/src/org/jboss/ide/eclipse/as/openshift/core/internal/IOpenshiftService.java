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

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;

/**
 * @author André Dietisheim
 */
public interface IOpenshiftService {

	public List<Cartridge> getCartridges(User user) throws OpenshiftException;  

	public Application createApplication(String name, ICartridge cartridge, User user) throws OpenshiftException;

	public void destroyApplication(String name, ICartridge cartridge, User user) throws OpenshiftException;

	public IApplication startApplication(String name, ICartridge cartridge, User user) throws OpenshiftException;
	
	public IApplication restartApplication(String name, ICartridge cartridge, User user) throws OpenshiftException;

	public IApplication stopApplication(String name, ICartridge cartridge, User user) throws OpenshiftException;

	public String getStatus(String name, ICartridge cartridge, User user) throws OpenshiftException;

	public Domain changeDomain(String name, ISSHPublicKey sshKey, User user) throws OpenshiftException;

	public Domain createDomain(String name, ISSHPublicKey sshKey, User user) throws OpenshiftException;

	public UserInfo getUserInfo(User user) throws OpenshiftException;
}
