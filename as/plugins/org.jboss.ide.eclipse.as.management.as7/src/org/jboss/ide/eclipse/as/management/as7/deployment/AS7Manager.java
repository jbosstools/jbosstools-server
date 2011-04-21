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
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.RESULT;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.protocol.StreamUtils;
import org.jboss.dmr.ModelNode;

/**
 * @author André Dietisheim
 */
public class AS7Manager {

	private ModelControllerClient client;
	private ServerDeploymentManager manager;

	public AS7Manager(String host, int port) throws UnknownHostException {
		this.client = ModelControllerClient.Factory.create(host, port);
		this.manager = ServerDeploymentManager.Factory.create(client);
	}

	public DeploymentOperationResult undeploySync(String name, IProgressMonitor monitor) throws DeployerException {
		DeploymentOperationResult result = undeploy(name);
		result.getStatus();
		return result;
	}

	public DeploymentOperationResult deploySync(String name, File file, IProgressMonitor monitor)
			throws DeployerException {
		DeploymentOperationResult result = deploy(name, file);
		result.getStatus();
		return result;
	}

	public DeploymentOperationResult undeploy(String name) throws DeployerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			builder = builder.undeploy(name).andRemoveUndeployed();
			return new DeploymentOperationResult(builder.getLastAction(), manager.execute(builder.build()));
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentOperationResult remove(String name) throws DeployerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			builder = builder.remove(name);
			return new DeploymentOperationResult(builder.getLastAction(), manager.execute(builder.build()));
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentOperationResult deploy(File file) throws DeployerException {
		return deploy(file.getName(), file);
	}

	public DeploymentOperationResult add(String name, File file) throws DeployerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file));
		} catch (IOException e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentOperationResult deploy(String name, File file) throws DeployerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file).andDeploy());
		} catch (IOException e) {
			throw new DeployerException(e);
		}
	}

	public DeploymentOperationResult replace(File file) throws DeployerException {
		return replace(file.getName(), file);
	}

	public DeploymentOperationResult replace(String name, File file) throws DeployerException {
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
			if (!AS7ManagerUtil.isSuccess(response)) {
				throw new DeployerException(
						MessageFormat.format("Could not execute {0} for {1}. Failure was {2}.", node.get(OP),
								node.get(ADDRESS), response.get(FAILURE_DESCRIPTION)));
			}
			return response.get(RESULT);
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	private DeploymentOperationResult execute(DeploymentPlanBuilder builder) throws DeployerException {
		try {
			DeploymentAction action = builder.getLastAction();
			Future<ServerDeploymentPlanResult> planResult = manager.execute(builder.build());
			return new DeploymentOperationResult(action, planResult);
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}
}
