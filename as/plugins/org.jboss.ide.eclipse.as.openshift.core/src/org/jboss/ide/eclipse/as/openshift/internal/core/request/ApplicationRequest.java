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
package org.jboss.ide.eclipse.as.openshift.internal.core.request;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;

/**
 * @author André Dietisheim
 */
public class ApplicationRequest extends AbstractOpenshiftRequest {

	private String name;
	private Cartridge cartridge ;
	private ApplicationAction action;

	public ApplicationRequest(String name, Cartridge cartridge, ApplicationAction action, String username) {
		this(name, cartridge, action, username, false);
	}

	public ApplicationRequest(String name, Cartridge cartridge, ApplicationAction action, String username, boolean debug) {
		super(username, debug);
		this.name = name;
		this.cartridge = cartridge;
		this.action = action;
	}

	public ApplicationAction getAction() {
		return action;
	}

	public String getName() {
		return name;
	}

	public Cartridge getCartridge() {
		return cartridge;
	}

	@Override
	public String getResourcePath() {
		return "cartridge";
	}
}
