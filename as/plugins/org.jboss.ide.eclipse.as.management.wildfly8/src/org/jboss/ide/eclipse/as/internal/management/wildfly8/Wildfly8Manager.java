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
package org.jboss.ide.eclipse.as.internal.management.wildfly8;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;

import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.ADDRESS;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.ENABLED;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.SHUTDOWN;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.NAME;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.RESULT;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP_ADDR;

/**
 * @author André Dietisheim
 */
public class Wildfly8Manager {
	public static final int DEFAULT_REQUEST_TIMEOUT = 5000;
	private ModelControllerClient client;
	private ServerDeploymentManager manager;
	private IAS7ManagementDetails details;

	public Wildfly8Manager(IAS7ManagementDetails details) throws JBoss7ManangerException {
		try {
			this.details = details;
			Object timeout = details.getProperty(IAS7ManagementDetails.PROPERTY_TIMEOUT);
			int timeout2 = !(timeout instanceof Integer) ? DEFAULT_REQUEST_TIMEOUT : 
							((Integer)timeout).intValue();
			this.client = ModelControllerClient.Factory.create(
					details.getHost(), details.getManagementPort(),
					getCallbackHandler(), null, timeout2);
			this.manager = ServerDeploymentManager.Factory.create(client);
		} catch(UnknownHostException uhe) {
			throw new JBoss7ManangerException(uhe);
		}
	}

	protected CallbackHandler getCallbackHandler() {
		return new Tools71CallbackHandler();
	}
	
	protected class Tools71CallbackHandler implements CallbackHandler {
		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
            if (callbacks.length == 1 && callbacks[0] instanceof NameCallback) {
                ((NameCallback) callbacks[0]).setName("anonymous JBossTools user");
                return;
            }

            NameCallback name = null;
            PasswordCallback pass = null;
            for (Callback current : callbacks) {
	            if (current instanceof RealmCallback) {
	            	RealmCallback rcb = (RealmCallback) current;
                    String defaultText = rcb.getDefaultText();
                    rcb.setText(defaultText); // For now just use the realm suggested.
	            }
                if (current instanceof NameCallback) {
                    name = (NameCallback) current;
                } else if (current instanceof PasswordCallback) {
                    pass = (PasswordCallback) current;
                } 
            }
            
            String[] results = details.handleCallbacks(new String[] { name.getPrompt(), pass.getPrompt()});
            if( results != null && results.length >= 2 ) {
	            name.setName(results[0]);
	            pass.setPassword(results[1].toCharArray());
            }
		}
	}
	
	public IJBoss7DeploymentResult undeploySync(String name, boolean removeFile, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		IJBoss7DeploymentResult result = undeploy(name, removeFile);
		result.getStatus();
		return result;
	}

	public IJBoss7DeploymentResult deploySync(String name, File file, boolean add, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		IJBoss7DeploymentResult result = deploy(name, file, add);
		result.getStatus();
		return result;
	}

	public IJBoss7DeploymentResult undeploy(String name, boolean removeFile) throws JBoss7ManangerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			if( removeFile )
				builder = builder.undeploy(name).andRemoveUndeployed();
			else 
				builder = builder.undeploy(name);
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

	/**
	 * Add and deploy the provided file
	 * @param file
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public IJBoss7DeploymentResult deploy(File file) throws JBoss7ManangerException {
		return deploy(file.getName(), file, true);
	}

	public IJBoss7DeploymentResult add(String name, File file) throws JBoss7ManangerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file));
		} catch (IOException e) {
			throw new JBoss7ManangerException(e);
		}
	}

	public IJBoss7DeploymentResult deploy(String name, File file, boolean add) throws JBoss7ManangerException {
		try {
			if( add )
				return execute(manager.newDeploymentPlan().add(name, file).andDeploy());
			else
				return execute(manager.newDeploymentPlan().deploy(name));
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
		if( !hasDeployment(name)) {
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

		Boolean enabled = Wildfly8ManagerUtil.getBooleanProperty(ENABLED, result);
		if (enabled == null) {
			throw new JBoss7ManangerException(
					NLS.bind(Messages.ModuleStateEvaluationFailed, name));
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
		if( client != null ) {
			closeClient(client);
		}
	}
	// Launch a thread to handle the close to ensure dispose() is immediate
	private void closeClient(final ModelControllerClient client) {
		Thread t = new Thread("Closing client") {
			public void run() {
				closeClientJoin(client);
			}
		};
		t.start();
	}
	
	// Launch a new thread with max duration 5s to handle the actual close
	private void closeClientJoin(final ModelControllerClient client) {
		Runnable r = new Runnable() {
			  public void run() {
			    try {
			        client.close();
			    } catch (Exception e) {
			       // trace
			    }
			  }
			};

			Thread t = new Thread(r);
			try {
			  t.start();
			  t.join(5000);
			} catch (InterruptedException e) {
			} finally {
			  t.interrupt();
			}
	}

	/*package*/ ModelNode execute(ModelNode node) throws JBoss7ManangerException {
		try {
			ModelNode response = client.execute(node);
			if (!Wildfly8ManagerUtil.isSuccess(response)) {
				throw new JBoss7ManangerException(
						NLS.bind(Messages.OperationOnAddressFailed,
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

	// this should only be used when doing shutdown and restart operations.
	private void quietlyExecute(ModelNode node) throws JBoss7ManangerException {
		try {
			client.execute(node);
		} catch (Exception e) {
			// During shutdown, some connections may be closed prematurely
			// It is acceptable to ignore these errors. 
			if (!isConnectionCloseException(e)) {
				throw new JBoss7ManangerException(e);
			}
		}
	}

	private boolean isConnectionCloseException(Exception e) {
		return (e instanceof IOException
				&& e.getMessage() != null
				&& (e.getMessage().indexOf("Channel closed") > -1) // pre-AS 7.1 client lib 
				|| e.getMessage().indexOf("Operation failed") > -1); // AS 7.1 client lib 
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
        ModelNode result = execute(op);
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