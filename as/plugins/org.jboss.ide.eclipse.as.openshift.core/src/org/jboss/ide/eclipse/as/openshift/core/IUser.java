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

import java.util.Collection;


/**
 * @author André Dietisheim
 */
public interface IUser {

	public String getRhlogin();

	public String getPassword();

	public IDomain getDomain() throws OpenshiftException;

	public ISSHPublicKey getSshKey() throws OpenshiftException;

	public Collection<ICartridge> getCartridges() throws OpenshiftException;

	public IApplication createApplication(String name, ICartridge cartridge) throws OpenshiftException;
	
	public Collection<IApplication> getApplications() throws OpenshiftException;

	public IApplication getApplicationByName(String name) throws OpenshiftException;

}