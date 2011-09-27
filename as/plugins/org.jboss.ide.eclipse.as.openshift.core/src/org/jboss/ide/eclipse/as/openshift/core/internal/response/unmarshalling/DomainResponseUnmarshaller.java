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
package org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.IDomain;
import org.jboss.ide.eclipse.as.openshift.core.internal.Domain;
import org.jboss.ide.eclipse.as.openshift.core.internal.InternalUser;

/**
 * @author André Dietisheim
 */
public class DomainResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<IDomain> {

	private String domainName;
	private InternalUser user;
	
	public DomainResponseUnmarshaller(String domainName, InternalUser user) {
		this.domainName = domainName;
		this.user = user;
	}

	@Override
	protected IDomain createOpenshiftObject(ModelNode node) {
		return new Domain(domainName, user);
	}
}
