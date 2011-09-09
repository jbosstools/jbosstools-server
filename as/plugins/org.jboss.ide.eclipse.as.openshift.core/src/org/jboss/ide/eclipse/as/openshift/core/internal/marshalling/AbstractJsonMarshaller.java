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
import org.jboss.ide.eclipse.as.openshift.internal.core.request.AbstractOpenshiftRequest;

/**
 * @author André Dietisheim
 */
public abstract class AbstractJsonMarshaller<REQUEST extends AbstractOpenshiftRequest> implements IOpenshiftMarshaller<REQUEST> {

	public String marshall(REQUEST request) throws OpenshiftException {
		ModelNode node = new ModelNode();
		setStringProperty(IOpenshiftJsonConstants.PROPERTY_RHLOGIN, request.getRhLogin(), node);
		setStringProperty(IOpenshiftJsonConstants.PROPERTY_DEBUG, request.isDebug(), node);
		setJsonDataProperties(node, request);
		return node.toJSONString(true);
	}

	protected void setJsonDataProperties(ModelNode node, REQUEST request) throws OpenshiftException {
		// empty default implementation
	}
	
	protected void setStringProperty(String propertyName, Object value, ModelNode node) {
		if (!isSet(value)) {
			if (value instanceof String) {
				setStringProperty((String) value, propertyName, node);
			}
		}
	}
	
	protected void setStringProperty(String propertyName, String value, ModelNode node) {
		if (!isSet(value)) {
			return;
		}
				
		node.get(propertyName).set(value);
	}

	protected boolean isSet(String value) {
		return value != null
				&& value.length() > 0;
	}

	protected boolean isSet(Object value) {
		return value != null;
	}

}
