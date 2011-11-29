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
package org.jboss.ide.eclipse.as.internal.management.as7;

import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.ADDRESS;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.ENABLED;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.NAME;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.RESULT;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.ide.eclipse.as.internal.management.as7.ModelDescriptionConstants.SHUTDOWN;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.server.v7.management.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ServerState;
import org.jboss.ide.eclipse.as.internal.management.as7.util.AS7ManagerUtil;

/**
 * @author André Dietisheim
 */
public class AS7Manager {

	public static final int MGMT_PORT = 9999;

	private ModelControllerClient client;
	private ServerDeploymentManager manager;
	private AS7ManagementDetails details;

	public AS7Manager(String host) throws UnknownHostException {
		this(new AS7ManagementDetails(host, MGMT_PORT));
	}

	public AS7Manager(String host, int port) throws UnknownHostException {
		this(new AS7ManagementDetails(host, port));
	}

	public AS7Manager(AS7ManagementDetails details) throws UnknownHostException {
		this.details = details;
		this.client = ModelControllerClient.Factory.create(details.getHost(), details.getManagementPort());
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

	/**
	 * Get the deployment state
	 * This API may cause additional management requests, but it prevents exceptions
	 * from being thrown on the server. 
	 * 
	 * @param name
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public JBoss7DeploymentState getDeploymentStateSafe(String name) throws JBoss7ManangerException {
		if( hasDeployment(name)) {
			return JBoss7DeploymentState.NOT_FOUND;
		}
		return getDeploymentState(name);
	}
	
	/**
	 * Get the deployment state. 
	 * This may cause exceptions to be thrown on the server side if the deployment has not yet 
	 * been found or started to register. 
	 * 
	 *  
	 * @param name
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public JBoss7DeploymentState getDeploymentState(String name) throws JBoss7ManangerException {
		ModelNode request = new ModelNode();
		request.get(OP).set(READ_RESOURCE_OPERATION);
		request.get(ADDRESS).add(DEPLOYMENT, name);
		ModelNode result = null;
		try {
			result = execute(request);
		} catch( JBoss7ManangerException j7me ) {
			return JBoss7DeploymentState.NOT_FOUND;
		}

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

	public void addDeploymentDirectory(String path) {
		
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

	public boolean isRunning() {
		try {
			return getServerState() == JBoss7ServerState.RUNNING;
		} catch (Exception e) {
			return false;
		}
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
	
    public static ModelNode getEmptyOperation(String operationName, ModelNode address) {
        ModelNode op = new ModelNode();
        op.get(OP).set(operationName);
        if (address != null) {
            op.get(OP_ADDR).set(address);
        }
        else {
            // Just establish the standard structure; caller can fill in address later
            op.get(OP_ADDR);
        }
        return op;
    }
    
    public boolean hasDeployment(String name) throws JBoss7ManangerException {
    	return getDeploymentNames().contains(name);
    }
    
    private Set<String> getDeploymentNames() throws JBoss7ManangerException {
        final ModelNode op = getEmptyOperation(READ_CHILDREN_NAMES_OPERATION, new ModelNode());
        op.get(CHILD_TYPE).set(DEPLOYMENT);
        ModelNode response = execute(op);
        ModelNode result = response.get(RESULT);
        Set<String> deploymentNames = new HashSet<String>();
        if (result.isDefined()) {
        	final List<ModelNode> deploymentNodes = result.asList();
            for (ModelNode node : deploymentNodes) {
                deploymentNames.add(node.asString());
            }
        }
        return deploymentNames;
    }


}