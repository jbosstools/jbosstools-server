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

import org.jboss.ide.eclipse.as.openshift.core.internal.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.Domain;

/**
 * @author André Dietisheim
 */
public interface IUser {

	public abstract String getRhlogin();

	public abstract String getPassword();

	public abstract Domain getDomain() throws OpenshiftException;

	public abstract ISSHPublicKey getSshKey() throws OpenshiftException;

	public abstract Collection<Cartridge> getCartridges() throws OpenshiftException;

	public Application createApplication(String name, Cartridge cartridge) throws OpenshiftException;
	
	public abstract Collection<Application> getApplications() throws OpenshiftException;

}