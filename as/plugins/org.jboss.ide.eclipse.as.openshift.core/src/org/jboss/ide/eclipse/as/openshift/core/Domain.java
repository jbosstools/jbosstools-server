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
public class Domain {

	private String namespace;
	private String rhcDomain;

	public Domain(String namespace) {
		this(namespace, null);
	}

	public Domain(String namespace, String rhcDomain) {
		this.namespace = namespace;
		this.rhcDomain = rhcDomain;
	}

	public String getRhcDomain() {
		return rhcDomain;
	}

	public String getNamespace() {
		return namespace;
	}
}
