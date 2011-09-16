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

import java.util.Date;


public class Application {

	private String name;
	private Cartridge cartridge;
	private String uuid;
	private Date creationTime;
	private String embedded;
	private IOpenshiftService service;
	private ApplicationLogReader logReader;

	public Application(String name, Cartridge cartridge, IOpenshiftService service) {
		this(name, null, cartridge, null, null, service);
	}
	
	public Application(String name, String uuid, Cartridge cartridge, String embedded, Date creationTime, IOpenshiftService service) {
		this.name = name;
		this.cartridge = cartridge;
		this.uuid = uuid;
		this.embedded = embedded;
		this.creationTime = creationTime;
		this.service = service;
	}

	public String getName() {
		return name;
	}

	public String getUUID() {
		return uuid;
	}

	public Cartridge getCartridge() {
		return cartridge;
	}

	public String getEmbedded() {
		return embedded;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void destroy() throws OpenshiftException {
		service.destroyApplication(name, cartridge);
	}
	
	public void start() throws OpenshiftException {
		service.startApplication(name, cartridge);
	}

	public void restart() throws OpenshiftException {
		service.restartApplication(name, cartridge);
	}

	public void stop() throws OpenshiftException {
		service.stopApplication(name, cartridge);
	}
	
	public ApplicationLogReader getLog() throws OpenshiftException {
		if (logReader == null) {
			this.logReader = new ApplicationLogReader(this, service);
		}
		return logReader;
	}
}
