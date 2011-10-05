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
package org.jboss.tools.openshift.express.client;

import java.util.Collection;
import java.util.List;

/**
 * @author André Dietisheim
 */
public interface IUser {

	public String getRhlogin();

	public String getPassword();

	public boolean isValid() throws OpenshiftException;

	public String getUUID() throws OpenshiftException;

	public IDomain createDomain(String name, ISSHPublicKey key) throws OpenshiftException;

	/**
	 * Returns the domain that this user created previously. Returns
	 * <code>null</code> if no domain was created.
	 * 
	 * @return the domain that this user created
	 * @throws OpenshiftException
	 * 
	 * @see #createDomain
	 */
	public IDomain getDomain() throws OpenshiftException;

	public boolean hasDomain() throws OpenshiftException;

	public ISSHPublicKey getSshKey() throws OpenshiftException;

	public List<ICartridge> getCartridges() throws OpenshiftException;

	public ICartridge getCartridgeByName(String name) throws OpenshiftException;

	public IApplication createApplication(String name, ICartridge cartridge) throws OpenshiftException;

	public Collection<IApplication> getApplications() throws OpenshiftException;

	public IApplication getApplicationByName(String name) throws OpenshiftException;

	public void refresh() throws OpenshiftException;

}