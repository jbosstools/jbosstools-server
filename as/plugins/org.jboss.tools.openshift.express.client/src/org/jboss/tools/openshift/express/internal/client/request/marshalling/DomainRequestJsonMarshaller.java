/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.openshift.express.internal.client.request.marshalling;

import org.jboss.dmr.ModelNode;
import org.jboss.tools.openshift.express.client.OpenshiftException;
import org.jboss.tools.openshift.express.internal.client.IOpenshiftJsonConstants;
import org.jboss.tools.openshift.express.internal.client.request.AbstractDomainRequest;

/**
 * @author André Dietisheim
 */
public class DomainRequestJsonMarshaller extends AbstractJsonMarshaller<AbstractDomainRequest> {

	@Override
	protected void setJsonDataProperties(ModelNode node, AbstractDomainRequest request) throws OpenshiftException {
		node.get(IOpenshiftJsonConstants.PROPERTY_NAMESPACE).set(request.getName());
		node.get(IOpenshiftJsonConstants.PROPERTY_ALTER).set(String.valueOf(request.isAlter()));
		node.get(IOpenshiftJsonConstants.PROPERTY_SSH).set(request.getSshKey().getPublicKey());
	}
}
