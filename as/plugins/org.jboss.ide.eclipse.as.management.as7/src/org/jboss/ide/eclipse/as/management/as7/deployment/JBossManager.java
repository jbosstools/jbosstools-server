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

import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ADDRESS;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ENABLED;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.RESULT;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.protocol.StreamUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.management.as7.Activator;

/**
 * @author André Dietisheim
 */
public class JBossManager {

	private ModelControllerClient client;
	private ServerDeploymentManager manager;

	public JBossManager(String host, int port) throws UnknownHostException {
		this.client = ModelControllerClient.Factory.create(host, port);
		this.manager = ServerDeploymentManager.Factory.create(client);
	}

	public DeploymentPlanResult undeploySync(String name, IProgressMonitor monitor) throws DeployerException {
		DeploymentPlanResult result = undeploy(name);
		result.getStatus();
		return result;
	}

	public DeploymentPlanResult deploySync(String name, File file, IProgressMonitor monitor) throws DeployerException {
		DeploymentPlanResult result = deploy(name, file);
		result.getStatus();
		return result;
	}

	public DeploymentPlanResult undeploy(String name) throws DeployerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			builder = builder.undeploy(name).andRemoveUndeployed();
			return new DeploymentPlanResult(builder.getLastAction(), manager.execute(builder.build()));
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentPlanResult remove(String name) throws DeployerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			builder = builder.remove(name);
			return new DeploymentPlanResult(builder.getLastAction(), manager.execute(builder.build()));
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentPlanResult deploy(File file) throws DeployerException {
		return deploy(file.getName(), file);
	}

	public DeploymentPlanResult add(String name, File file) throws DeployerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file));
		} catch (IOException e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentPlanResult deploy(String name, File file) throws DeployerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file).andDeploy());
		} catch (IOException e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentPlanResult replace(File file) throws DeployerException {
		return replace(file.getName(), file);
	}

	public DeploymentPlanResult replace(String name, File file) throws DeployerException {
		try {
			return execute(manager.newDeploymentPlan().replace(name, file));
		} catch (IOException e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentState getDeploymentState(String name) throws DeployerException {
		ModelNode request = new ModelNode();
		request.get(OP).set(READ_RESOURCE_OPERATION);
		request.get(ADDRESS).add(DEPLOYMENT, name);
		ModelNode result = execute(request);
		return DeploymentState.getForResultNode(result);
	}

	public void dispose() {
		StreamUtils.safeClose(client);
	}
	
	private ModelNode execute(ModelNode node) throws DeployerException {
		try {
			ModelNode response = client.execute(node);
			if (!JBossManagementUtil.isSuccess(response)) {
				throw new DeployerException(
						MessageFormat.format("Could not execute {0} for {1}. Failure was {2}.", node.get(OP), node.get(ADDRESS), response.get(FAILURE_DESCRIPTION)));
			} 
			return response.get(RESULT);
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	private DeploymentPlanResult execute(DeploymentPlanBuilder builder) throws DeployerException {
		try {
			DeploymentAction action = builder.getLastAction();
			Future<ServerDeploymentPlanResult> planResult = manager.execute(builder.build());
			return new DeploymentPlanResult(action, planResult);
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	public static class DeploymentPlanResult {

		private Future<ServerDeploymentPlanResult> planResult;
		private DeploymentAction action;

		public DeploymentPlanResult(DeploymentAction action, Future<ServerDeploymentPlanResult> planResult) {
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

	public enum DeploymentState {
		ENABLEDSTATE {
			protected boolean matches(boolean enabled) {
				return enabled == true;
			}
		},
		DISABLEDSTATE {
			protected boolean matches(boolean enabled) {
				return enabled == false;
			}
		};
		
		public static DeploymentState getForResultNode(ModelNode resultNode) {
			Boolean enabled = JBossManagementUtil.getBooleanProperty(ENABLED, resultNode);
			if (enabled == null) {
				return null;
			}
			
			DeploymentState matchingState = null;
			for(DeploymentState state : values()) {
				if (state.matches(enabled)) {
					matchingState = state;
				}
			}
			return matchingState;
		}

		protected abstract boolean matches(boolean enabled);

	}
}
