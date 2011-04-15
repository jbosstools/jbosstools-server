package org.jboss.ide.eclipse.as.management.as7.deployment;

import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.ide.eclipse.as.management.as7.Activator;

public class Deployable {

	private String name;
	private File file;
	private Future<ServerDeploymentPlanResult> resultFuture;
	private long timeout;
	private DeploymentAction action;

	protected Deployable(String name, File file, long timeout) {
		this.name = name;
		this.file = file;
		this.timeout = timeout;
	}

	protected void setDeploymentAction(DeploymentAction action) {
		this.action = action;
	}

	protected void setResultFuture(Future<ServerDeploymentPlanResult> resultFuture) {
		this.resultFuture = resultFuture;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public IStatus getStatus() throws DeployerException {
		if (resultFuture == null
				|| action == null) {
			return null;
		}
		try {
			ServerDeploymentPlanResult result = resultFuture.get(timeout, TimeUnit.MILLISECONDS);
			ServerDeploymentActionResult actionResult = result.getDeploymentActionResult(action.getId());
			return createStatus(action, actionResult);
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	private IStatus createStatus(DeploymentAction action, ServerDeploymentActionResult actionResult) {
		if (actionResult == null) {
			return null;
		}

		IStatus status = null;
		switch (actionResult.getResult()) {
		case NOT_EXECUTED:
			status = createStatus(IStatus.ERROR, "The operation {0} was not executed on unit {1}", action
					.getType().name(), getName());
			break;
		case EXECUTED:
			status = Status.OK_STATUS;
			break;
		case FAILED:
			status = createStatus(IStatus.ERROR, "The operation {0} failed for unit {1}", action.getType()
					.name(), getName());
			break;
		case ROLLED_BACK:
			status = createStatus(IStatus.ERROR, "The operation {0} for unit {1} was rolled back", action
					.getType().name(), getName());
			break;
		case CONFIGURATION_MODIFIED_REQUIRES_RESTART:
			status = createStatus(
					IStatus.WARNING,
					"The operation {0} was not executed on unit {1}. The server configuration was changed though and the server needs to be restarted",
					action.getType().name(), getName());
			break;
		}
		return status;
	}

	private IStatus createStatus(int severity, String messagePattern, Object... messageArguments) {
		return new Status(severity, Activator.getContext().getBundle().getSymbolicName(), MessageFormat.format(
				messagePattern, messageArguments));
	}
}