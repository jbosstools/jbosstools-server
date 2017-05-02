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
package org.jboss.ide.eclipse.as.internal.management.wf11;

import java.text.MessageFormat;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;

/**
 * A class that holds the status of a deployment operation.
 * 
 * @author André Dietisheim
 *
 */
public class DeploymentOperationResult implements IJBoss7DeploymentResult {
	
	private Future<ServerDeploymentPlanResult> planResult;
	private DeploymentAction action;
	private long timeout;
	private TimeUnit unit;

	DeploymentOperationResult(DeploymentAction action, Future<ServerDeploymentPlanResult> planResult) {
		Assert.isNotNull(action);
		this.action = action;
		Assert.isNotNull(planResult);
		this.planResult = planResult;
	}
	DeploymentOperationResult(DeploymentAction action, Future<ServerDeploymentPlanResult> planResult, long timeout, TimeUnit unit) {
		this(action, planResult);
		this.timeout = timeout;
		this.unit = unit;
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult#getStatus()
	 */
	@Override
	public IStatus getStatus() throws JBoss7ManangerException {
		try {
			ServerDeploymentActionResult actionResult = null;
			if( unit == null )
				actionResult = planResult.get().getDeploymentActionResult(action.getId());
			else
				actionResult = planResult.get(timeout, unit).getDeploymentActionResult(action.getId());
			return createStatus(action.getDeploymentUnitUniqueName(), action.getType().name(), actionResult);
		} catch (Exception e) {
			throw new JBoss7ManangerException(e);
		}
	}

	private IStatus createStatus(String deploymentName, String actionName, ServerDeploymentActionResult actionResult) {
		if (actionResult == null) {
			return null;
		}

		IStatus status = null;
		switch (actionResult.getResult()) {
		case NOT_EXECUTED:
			status = createStatus(IStatus.ERROR, NLS.bind(
					Messages.OperationOnUnitNotExecuted, 
					actionName, deploymentName));
			break;
		case EXECUTED:
			status = Status.OK_STATUS;
			break;
		case FAILED:
			status = createStatus(IStatus.ERROR, NLS.bind(Messages.OperationOnUnitFailed, 
					actionName, deploymentName));
			break;
		case ROLLED_BACK:
			status = createStatus(IStatus.ERROR, 
					NLS.bind(Messages.OperationOnUnitRolledBack,
							actionName, deploymentName));
			break;
		case CONFIGURATION_MODIFIED_REQUIRES_RESTART:
			status = createStatus(
					IStatus.WARNING,
					NLS.bind(Messages.OperationNotExecConfigRequiresRestart,
							actionName, deploymentName));
			break;
		}
		return status;
	}

	private IStatus createStatus(int severity, String messagePattern, Object... messageArguments) {
		return new Status(severity, WildFly11ManagementActivator.PLUGIN_ID, MessageFormat.format(
				messagePattern, messageArguments));
	}
	
	/*
	 * Candidate for API
	 */
	public boolean isDone() {
		return planResult.isDone();
	}
	
	/*
	 * Candidate for API
	 */
	public void cancel() {
		planResult.cancel(true);
	}
}