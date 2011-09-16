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
package org.jboss.ide.eclipse.as.openshift.core.internal.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftJsonConstants;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;

/**
 * @author André Dietisheim
 */
public abstract class AbstractOpenshiftJsonResponseUnmarshaller<OPENSHIFTOBJECT> {

	private String response;

	public OpenshiftResponse<OPENSHIFTOBJECT> unmarshall(String response) throws OpenshiftException {
		try {
			ModelNode node = ModelNode.fromJSONString(response);
			boolean debug = node.get(IOpenshiftJsonConstants.PROPERTY_DEBUG).asBoolean();
			String messages = getString(IOpenshiftJsonConstants.PROPERTY_MESSAGES, node);
			String result = getString(IOpenshiftJsonConstants.PROPERTY_RESULT, node);
			int exitCode = node.get(IOpenshiftJsonConstants.PROPERTY_EXIT_CODE).asInt();
			OPENSHIFTOBJECT openshiftObject = createOpenshiftObject(node);
			return new OpenshiftResponse<OPENSHIFTOBJECT>(debug, messages, result, openshiftObject, exitCode);
		} catch (IllegalArgumentException e) {
			throw new OpenshiftException(e, "Could not parse response \"{0}\"", response);
		} catch (Exception e) {
			throw new OpenshiftException(e, "Could not unmarshall response \"{0}\"", response);
		}
	}

	protected abstract OPENSHIFTOBJECT createOpenshiftObject(ModelNode responseNode) throws Exception;

	protected String getResponse() {
		return response;
	}

	protected String getString(String property, ModelNode node) {
		ModelNode propertyNode = node.get(property);
		if (!isSet(node)) {
			// replace "undefined" by null
			return null;
		}
		return propertyNode.asString();
	}

	protected boolean isSet(ModelNode node) {
		return node != null
				&& node.getType() != ModelType.UNDEFINED;
	}

	protected Date getDate(String property, ModelNode node) throws DatatypeConfigurationException {
		ModelNode propertyNode = node.get(property);
		// SimpleDateFormat can't handle RFC822 (-04:00 instead of GMT-04:00)
		// date formats
		// SimpleDateFormat dateFormat = new
		// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		// return dateFormat.parse(propertyNode.asString());
		GregorianCalendar calendar =
				DatatypeFactory.newInstance().newXMLGregorianCalendar(propertyNode.asString()).toGregorianCalendar();
		return calendar.getTime();
	}

	protected long getLong(String property, ModelNode node) {
		ModelNode propertyNode = node.get(property);
		return propertyNode.asLong(-1);
	}
}
