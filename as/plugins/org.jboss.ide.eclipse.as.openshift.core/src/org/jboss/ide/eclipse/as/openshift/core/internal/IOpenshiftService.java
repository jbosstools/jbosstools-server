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

import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.IDomain;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;

/**
 * @author André Dietisheim
 */
public interface IOpenshiftService {

	public static final String BASE_URL = "https://openshift.redhat.com/broker";

	/**
	 * List all cartridges that are available on the Openshift Express platform.
	 * 
	 * @param user the user account that shall be used 
	 * @return the list of cartridges available on the platform
	 * @throws OpenshiftException
	 * 
	 * @see InternalUser
	 */
	public List<ICartridge> getCartridges(InternalUser user) throws OpenshiftException;  

	/**
	 * Creates an application with the given name and cartridge for the given user.
	 * 
	 * @param name the application name
	 * @param cartridge the cartridge to use
	 * @param user the user account
	 * @return the application that was created
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 * @see IApplication
	 */
	public IApplication createApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException;

	/**
	 * Destroys the application with the given name and cartridge for the given user.
	 * 
	 * @param name the name of the application that shall be destroyed
	 * @param cartridge the cartridge that the application is running on
	 * @param user the user account
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 */
	public void destroyApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException;

	/**
	 * Starts the application with the given name and cartridge for the given user account. 
	 * Starting an application that is already started has no effect.
	 * 
	 * @param name of the application that shall be started
	 * @param cartridge the cartridge the application is running on
	 * @param user the user account to use
	 * @return the application that was started
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 * @see IApplication
	 */
	public IApplication startApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException;
	
	/**
	 * Restarts the application with the given name and cartridge for the given user account.
	 * 
	 * @param name the name of the application that shall be restarted
	 * @param cartridge the cartridge the application is running on
	 * @param user the user account to use
	 * @return the application that was started
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 * @see IApplication
	 */
	public IApplication restartApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException;

	/**
	 * Stops the application with the given name and cartridge for the given user account. Stopping an application
	 * that is already stopped has no effect.
	 * 
	 * @param name the name of the application that shall be restarted
	 * @param cartridge the cartridge the application is running on
	 * @param user the user account to use
	 * @return the application that was stopped
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 * @see IApplication
	 */
	public IApplication stopApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException;

	/**
	 * Returns the log of the application with the given name and cartridge. Returns the whole log if 
	 * no new log entry was created since the last call. Returns the new entries otherwise.
	 * 
	 * @param name of the application that the log shall be returned of
	 * @param cartridge the cartridge the application is running on
	 * @param user the user account to use
	 * @return the log of the application
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 */
	public String getStatus(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException;

	/**
	 * Changes the current domain (namespace) to the given name.
	 * 
	 * @param name the new domain name(-space)
	 * @param sshKey the ssh key that shall be used.
	 * @param user the user account to use
	 * @return the domain that was changed
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 * @see SSHKeyPair
	 * @see ISSHPublicKey
	 * @see IDomain
	 */
	public IDomain changeDomain(String name, ISSHPublicKey sshKey, InternalUser user) throws OpenshiftException;

	/**
	 * Creates a domain (namespace) with the given name for the given user account with the given ssh public key.
	 * If a domain already exists an OpenshiftEndpointException is thrown.
	 * 
	 * @param name the new domain name(-space)
	 * @param sshKey the ssh key that shall be used.
	 * @param user the user account to use
	 * @return the domain that was changed
	 * @throws OpenshiftException
	 * 
	 * @see ICartridge
	 * @see InternalUser
	 * @see SSHKeyPair
	 * @see ISSHPublicKey
	 * @see IDomain
	 */
	public IDomain createDomain(String name, ISSHPublicKey sshKey, InternalUser user) throws OpenshiftException;

	/**
	 * Returns all informations for the given user and its applications.  
	 * 
	 * @param user the user account to use
	 * @return all user informations (user related info and applications)
	 * @throws OpenshiftException
	 * 
	 * @see InternalUser
	 * @see UserInfo
	 * @see ApplicationInfo
	 */
	public UserInfo getUserInfo(InternalUser user) throws OpenshiftException;
}
