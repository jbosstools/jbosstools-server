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

/**
 * @author André Dietisheim
 */
public class UserInfo implements IOpenshiftObject {

	private String rhlogin;
	private String uuId;
	private String sshKey;
	private String rhcDomain;
	private String namespace;

	public UserInfo(String rhlogin, String uuid, String sshKey, String rhcDomain, String namespace) {
		this.rhlogin = rhlogin;
		this.uuId = uuid;
		this.sshKey = sshKey;
		this.rhcDomain = rhcDomain;
		this.namespace = namespace;
	}

	public String getRhlogin() {
		return rhlogin;
	}

	public String getUuId() {
		return uuId;
	}

	public String getRhcDomain() {
		return rhcDomain;
	}

	public String getSSHKey() {
		return sshKey;
	}

	public String getNamespace() {
		return namespace;
	}
}
