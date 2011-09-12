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
package org.jboss.ide.eclipse.as.openshift.internal.core.response;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftJsonConstants;
import org.jboss.ide.eclipse.as.openshift.core.User;

/**
 * @author André Dietisheim
 */
public class DomainResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<Domain> {

	private String domainName;

	public DomainResponseUnmarshaller(String domainName) {
		this.domainName = domainName;
	}

	@Override
	protected Domain createOpenshiftObject(ModelNode node) {
		User user = createUser(node);
		return new Domain(domainName, user);
	}

	protected User createUser(ModelNode node) {
		String username = getString(IOpenshiftJsonConstants.PROPERTY_RHLOGIN, node);
		String uuid = getString(IOpenshiftJsonConstants.PROPERTY_UUID, node);
		
		return new User(username, uuid);
	}
}
