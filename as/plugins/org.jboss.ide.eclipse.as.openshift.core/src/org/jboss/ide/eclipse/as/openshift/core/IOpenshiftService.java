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

import org.jboss.ide.eclipse.as.openshift.internal.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.internal.core.UserInfo;

public interface IOpenshiftService {

	public UserInfo getUserInfo() throws OpenshiftException;

	public List<Cartridge> getCartridges() throws OpenshiftException;  

	public Application createApplication(String name, Cartridge cartridge) throws OpenshiftException;

	public Application destroyApplication(String name, Cartridge cartridge) throws OpenshiftException;
}
