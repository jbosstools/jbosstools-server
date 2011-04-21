/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.management.as7.deployment;

import java.text.MessageFormat;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.ide.eclipse.as.management.as7.Activator;

/**
 * A class that holds the status of a deployment operation.
 * 
 * @author André Dietisheim
 *
 */
public class DeploymentOperationResult {

	private Future<ServerDeploymentPlanResult> planResult;
	private DeploymentAction action;

	DeploymentOperationResult(DeploymentAction action, Future<ServerDeploymentPlanResult> planResult) {
		Assert.isNotNull(action);
		this.action = action;
		Assert.isNotNull(planResult);
		this.planResult = planResult;
	}

	public IStatus getStatus() throws DeployerException {
		try {
			ServerDeploymentActionResult actionResult = planResult.get().getDeploymentActionResult(action.getId());
			return createStatus(action.getDeploymentUnitUniqueName(), action.getType().name(), actionResult);
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	private IStatus createStatus(String deploymentName, String actionName, ServerDeploymentActionResult actionResult) {
		if (actionResult == null) {
			return null;
		}

		IStatus status = null;
		switch (actionResult.getResult()) {
		case NOT_EXECUTED:
			status = createStatus(IStatus.ERROR, "The operation {0} was not executed on unit {1}",
					actionName, deploymentName);
			break;
		case EXECUTED:
			status = Status.OK_STATUS;
			break;
		case FAILED:
			status = createStatus(IStatus.ERROR, "The operation {0} failed for unit {1}",
					actionName, deploymentName);
			break;
		case ROLLED_BACK:
			status = createStatus(IStatus.ERROR, "The operation {0} for unit {1} was rolled back",
					actionName, deploymentName);
			break;
		case CONFIGURATION_MODIFIED_REQUIRES_RESTART:
			status = createStatus(
					IStatus.WARNING,
					"The operation {0} was not executed on unit {1}. The server configuration was changed though and the server needs to be restarted",
					actionName, deploymentName);
			break;
		}
		return status;
	}

	private IStatus createStatus(int severity, String messagePattern, Object... messageArguments) {
		return new Status(severity, Activator.getContext().getBundle().getSymbolicName(), MessageFormat.format(
				messagePattern, messageArguments));
	}
}