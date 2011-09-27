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
public class Domain extends UserInfoAware implements IDomain {

	private String namespace;

	public Domain(String namespace, InternalUser internalUser) {
		this(namespace, null, internalUser);
	}

	public Domain(String namespace, String rhcDomain, InternalUser internalUser) {
		super(internalUser);
		this.namespace = namespace;
	}

	@Override
	public String getNamespace() throws OpenshiftException {
		return namespace;
	}

	@Override
	public String getRhcDomain() throws OpenshiftException {
		return getUserInfo().getRhcDomain();
	}
}
