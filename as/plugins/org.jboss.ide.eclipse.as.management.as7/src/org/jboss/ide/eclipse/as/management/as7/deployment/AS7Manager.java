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
package org.jboss.ide.eclipse.as.management.as7.deployment;

import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ADDRESS;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ENABLED;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.NAME;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.RESULT;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.SHUTDOWN;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.protocol.old.StreamUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ServerState;
import org.jboss.ide.eclipse.as.management.as7.AS7Messages;

/**
 * @author André Dietisheim
 */
public class AS7Manager {

	public static final int MGMT_PORT = 9999;

	private ModelControllerClient client;
	private ServerDeploymentManager manager;

	public AS7Manager(String host) throws UnknownHostException {
		this(host, MGMT_PORT);
	}

	public AS7Manager(String host, int port) throws UnknownHostException {
		this.client = ModelControllerClient.Factory.create(host, port);
		this.manager = ServerDeploymentManager.Factory.create(client);
	}

	public IJBoss7DeploymentResult undeploySync(String name, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		IJBoss7DeploymentResult result = undeploy(name);
		result.getStatus();
		return result;
	}

	public IJBoss7DeploymentResult deploySync(String name, File file, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		IJBoss7DeploymentResult result = deploy(name, file);
		result.getStatus();
		return result;
	}

	public IJBoss7DeploymentResult undeploy(String name) throws JBoss7ManangerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			builder = builder.undeploy(name).andRemoveUndeployed();
			return new DeploymentOperationResult(builder.getLastAction(), manager.execute(builder.build()));
		} catch (Exception e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public IJBoss7DeploymentResult remove(String name) throws JBoss7ManangerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			builder = builder.remove(name);
			return new DeploymentOperationResult(builder.getLastAction(), manager.execute(builder.build()));
		} catch (Exception e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public IJBoss7DeploymentResult deploy(File file) throws JBoss7ManangerException {
		return deploy(file.getName(), file);
	}

	public IJBoss7DeploymentResult add(String name, File file) throws JBoss7ManangerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file));
		} catch (IOException e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public IJBoss7DeploymentResult deploy(String name, File file) throws JBoss7ManangerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file).andDeploy());
		} catch (IOException e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public IJBoss7DeploymentResult replace(File file) throws JBoss7ManangerException {
		return replace(file.getName(), file);
	}

	public IJBoss7DeploymentResult replace(String name, File file) throws JBoss7ManangerException {
		try {
			return execute(manager.newDeploymentPlan().replace(name, file));
		} catch (IOException e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public JBoss7DeploymentState getDeploymentState(String name) throws JBoss7ManangerException {
		ModelNode request = new ModelNode();
		request.get(OP).set(READ_RESOURCE_OPERATION);
		request.get(ADDRESS).add(DEPLOYMENT, name);
		ModelNode result = execute(request);

		Boolean enabled = AS7ManagerUtil.getBooleanProperty(ENABLED, result);
		if (enabled == null) {
			throw new JBoss7ManangerException(
					NLS.bind(AS7Messages.ModuleStateEvaluationFailed, name));
		} else if (enabled) {
			return JBoss7DeploymentState.STARTED;
		} else {
			return JBoss7DeploymentState.STOPPED;
		}
	}

	/**
	 * Shuts the server down.
	 * 
	 * @throws JBoss7ManangerException
	 */
	public void stopServer() throws JBoss7ManangerException {
		ModelNode request = new ModelNode();
		request.get(OP).set(SHUTDOWN);
		quietlyExecute(request);
	}

	public JBoss7ServerState getServerState() throws JBoss7ManangerException {
		ModelNode request = new ModelNode();
		request.get(OP).set(READ_ATTRIBUTE_OPERATION);
		request.get(NAME).set(SERVER_STATE);
		ModelNode response = execute(request);
		return toJBoss7ServerState(response);
	}

	private JBoss7ServerState toJBoss7ServerState(ModelNode response) throws JBoss7ManangerException {
		try {
			return JBoss7ServerState.valueOfIgnoreCase(response.asString());
		} catch (IllegalArgumentException e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public void dispose() {
		StreamUtils.safeClose(client);
	}

	private ModelNode execute(ModelNode node) throws JBoss7ManangerException {
		try {
			ModelNode response = client.execute(node);
			if (!AS7ManagerUtil.isSuccess(response)) {
				throw new JBoss7ManangerException(
						NLS.bind(AS7Messages.OperationOnAddressFailed,
								new Object[] { node.get(OP),
										node.get(ADDRESS),
										response.get(FAILURE_DESCRIPTION) }
								));
			}
			return response.get(RESULT);
		} catch (Exception e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public void quietlyExecute(ModelNode node) throws JBoss7ManangerException {
		try {
			client.execute(node);
		} catch (Exception e) {
			if (!isConnectionCloseException(e)) {
				throw new JBoss7ManangerException(e);
			}
		}
	}

	private boolean isConnectionCloseException(Exception e) {
		return e instanceof IOException
				&& e.getMessage() != null
				&& e.getMessage().indexOf("Channel closed") > -1;
	}

	private IJBoss7DeploymentResult execute(DeploymentPlanBuilder builder) throws JBoss7ManangerException {
		try {
			DeploymentAction action = builder.getLastAction();
			Future<ServerDeploymentPlanResult> planResult = manager.execute(builder.build());
			return new DeploymentOperationResult(action, planResult);
		} catch (Exception e) {
			throw new JBoss7ManangerException(e);
		}
	}
}