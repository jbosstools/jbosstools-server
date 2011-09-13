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
package org.jboss.ide.eclipse.as.openshift.core.internal.marshalling;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftJsonConstants;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.AbstractDomainRequest;

/**
 * @author André Dietisheim
 */
public class DomainRequestJsonMarshaller extends AbstractJsonMarshaller<AbstractDomainRequest> {

	@Override
	protected void setJsonDataProperties(ModelNode node, AbstractDomainRequest request) throws OpenshiftException {
		node.get(IOpenshiftJsonConstants.PROPERTY_NAMESPACE).set(request.getName());
		node.get(IOpenshiftJsonConstants.PROPERTY_ALTER).set(String.valueOf(request.isAlter()));
		node.get(IOpenshiftJsonConstants.PROPERTY_SSH).set(request.getSshKey().getPublicKeyContent());
	}
}
