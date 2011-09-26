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

import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;


/**
 * @author André Dietisheim
 */
public class Domain {

	private String namespace;
	private String rhcDomain;
	private User user;

	public Domain(String namespace, User user) {
		this(namespace, null, user);
	}

	public Domain(String namespace, String rhcDomain, User user) {
		this.namespace = namespace;
		this.rhcDomain = rhcDomain;
		this.user = user;
	}

	public String getNamespace() throws OpenshiftException {
		return namespace;
	}

	public String getRhcDomain() throws OpenshiftException {
		user.loadLazyValues();
		return rhcDomain;
	}

	void update(UserInfo userInfo) {
		this.rhcDomain = userInfo.getRhcDomain();
		this.namespace = userInfo.getNamespace();
	}
}
