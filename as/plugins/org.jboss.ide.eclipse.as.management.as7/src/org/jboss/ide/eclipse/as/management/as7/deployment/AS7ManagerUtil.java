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
package org.jboss.ide.eclipse.as.management.as7.deployment;

import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.OUTCOME;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.RESULT;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.SUCCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManangementException;
import org.jboss.ide.eclipse.as.management.as7.internal.DefaultOperationRequestBuilder;
import org.jboss.ide.eclipse.as.management.as7.internal.OperationFormatException;

/**
 * Various utility methods to deal with the as7 management api.
 * 
 * @author Alexey Loubyansky
 * @author André Dietisheim
 */
public class AS7ManagerUtil {

	public static boolean isSuccess(ModelNode operationResult) {
		if (operationResult != null) {
			ModelNode outcome = operationResult.get(OUTCOME);
			return outcome != null && outcome.asString().equals(SUCCESS);
		}
		return false;
	}

	public static String getFailureDescription(ModelNode operationResult) {
		if (operationResult == null) {
			return null;
		}

		ModelNode descr = operationResult.get(FAILURE_DESCRIPTION);
		if (descr == null) {
			return null;
		}

		return descr.asString();
	}

	public static List<String> getList(ModelNode operationResult) {
		if (!operationResult.hasDefined(RESULT))
			return Collections.emptyList();

		List<ModelNode> nodeList = operationResult.get(RESULT).asList();
		if (nodeList.isEmpty())
			return Collections.emptyList();

		List<String> list = new ArrayList<String>(nodeList.size());
		for (ModelNode node : nodeList) {
			list.add(node.asString());
		}
		return list;
	}

	public static List<String> getRequestPropertyNames(ModelNode operationResult) {
		if (!operationResult.hasDefined(RESULT))
			return Collections.emptyList();

		ModelNode result = operationResult.get(RESULT);
		if (!result.hasDefined(REQUEST_PROPERTIES))
			return Collections.emptyList();

		List<Property> nodeList = result.get(REQUEST_PROPERTIES).asPropertyList();
		if (nodeList.isEmpty())
			return Collections.emptyList();

		List<String> list = new ArrayList<String>(nodeList.size());
		for (Property node : nodeList) {
			list.add(node.getName());
		}
		return list;
	}

	public static boolean isDeployed(String name, ModelControllerClient client) {
		return getDeployments(client).contains(name);
	}

	public static List<String> getDeployments(ModelControllerClient client) {

		DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
		final ModelNode request;
		try {
			builder.operationName(READ_CHILDREN_NAMES_OPERATION);
			builder.addProperty(CHILD_TYPE, "deployment");
			request = builder.buildRequest();
		} catch (OperationFormatException e) {
			throw new IllegalStateException("Failed to build operation", e);
		}

		try {
			ModelNode outcome = client.execute(request);
			if (isSuccess(outcome)) {
				return getList(outcome);
			}
		} catch (Exception e) {
		}

		return Collections.emptyList();
	}

	public static Boolean getBooleanProperty(String propertyName, ModelNode node) {
		if (node == null) {
			return null;
		}
		ModelNode valueNode = node.get(propertyName);
		if (valueNode == null) {
			return null;
		}
		String value = valueNode.toString();
		if (value == null
				|| valueNode.getType() != ModelType.BOOLEAN) {
			return null;
		}
		return Boolean.valueOf(value);
	}

	public static ModelNode execute(Operation operation, ModelControllerClient client) throws JBoss7ManangementException {
		try {
			ModelNode result = client.execute(operation);
			if (result.hasDefined(OUTCOME)
					&& SUCCESS.equals(result.get("outcome").asString())) {
				return result.get(RESULT);
			}
			else if (result.hasDefined(FAILURE_DESCRIPTION)) {
				throw new JBoss7ManangementException(result.get(FAILURE_DESCRIPTION).toString());
			}
			else {
				throw new JBoss7ManangementException("Operation outcome is " + result.get(OUTCOME).asString());
			}
		} catch (IOException e) {
			throw new JBoss7ManangementException(e);
		}
	}
}
