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
import org.jboss.ide.eclipse.as.openshift.internal.core.request.AbstractOpenshiftRequest;

/**
 * @author André Dietisheim
 */
public abstract class AbstractJsonMarshaller<REQUEST extends AbstractOpenshiftRequest> implements IOpenshiftMarshaller<REQUEST> {

	public String marshall(REQUEST request) {
		ModelNode node = new ModelNode();
		node.get(IOpenshiftJsonConstants.PROPERTY_RHLOGIN).set(request.getRhLogin());
		node.get(IOpenshiftJsonConstants.PROPERTY_DEBUG).set(String.valueOf(request.isDebug()));
		setNodeProperties(node, request);
		return node.toJSONString(true);
	}

	protected void setNodeProperties(ModelNode node, REQUEST request) {
		// empty default implementation
	}
}
