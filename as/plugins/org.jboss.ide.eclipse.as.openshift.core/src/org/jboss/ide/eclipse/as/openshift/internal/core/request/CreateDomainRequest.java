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


/**
 * @author André Dietisheim
 */
public class CreateDomainRequest extends AbstractOpenshiftRequest {

	private String name;
	private String sshKey;
	private String namespace;

	public CreateDomainRequest(String name, String namespace, String sshKey, ApplicationAction action, String username) {
		this(name, namespace, sshKey, username, false);
	}

	public CreateDomainRequest(String name, String namespace, String sshKey, String username, boolean debug) {
		super(username, debug);
		this.name = name;
		this.namespace = namespace;
		this.sshKey = sshKey;
	}

	public String getName() {
		return name;
	}

	protected String getSshKey() {
		return sshKey;
	}

	protected String getNamespace() {
		return namespace;
	}

	@Override
	public String getResourcePath() {
		return "domain";
	}
}
