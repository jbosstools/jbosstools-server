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


public class Application implements IOpenshiftObject {

	private String name;
	private Cartridge cartridge;
	private IOpenshiftService service;

	public Application(String name, Cartridge cartridge, IOpenshiftService service) {
		this.name = name;
		this.cartridge = cartridge;
		this.service = service;
	}

	public String getName() {
		return name;
	}

	public Cartridge getCartridge() {
		return cartridge;
	}

	public void destroy() throws OpenshiftException {
		service.destroyApplication(name, cartridge);
	}
	
	public void start() throws OpenshiftException {
		service.startApplication(name, cartridge);
	}

	public void stop() throws OpenshiftException {
		service.stopApplication(name, cartridge);
	}

}
