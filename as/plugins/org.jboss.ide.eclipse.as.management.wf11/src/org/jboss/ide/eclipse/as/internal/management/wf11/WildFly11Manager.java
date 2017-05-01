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

import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.ADDRESS;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.ENABLED;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.NAME;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.RESULT;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.SHUTDOWN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlan;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IncrementalManagementModel;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.wildfly.security.auth.callback.CallbackUtil;
import org.wildfly.security.auth.callback.CredentialCallback;
import org.wildfly.security.auth.callback.OptionalNameCallback;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.password.interfaces.ClearPassword;
import org.wildfly.security.password.interfaces.DigestPassword;
import org.wildfly.security.util.CodePointIterator;

/**
 * @author André Dietisheim
 */
public class WildFly11Manager {
	public static final int DEFAULT_REQUEST_TIMEOUT = 5000;
	private ModelControllerClient client;
	private ServerDeploymentManager manager;
	private IAS7ManagementDetails details;

	public WildFly11Manager(IAS7ManagementDetails details) throws JBoss7ManangerException {
		try {
			this.details = details;
			Object timeout = details.getProperty(IAS7ManagementDetails.PROPERTY_TIMEOUT);
			int timeout2 = !(timeout instanceof Integer) ? DEFAULT_REQUEST_TIMEOUT : 
							((Integer)timeout).intValue();
			ModelControllerClientConfiguration.Builder b = 
					new ModelControllerClientConfiguration.Builder()
					.setHostName(details.getHost())
					.setPort(details.getManagementPort())
					.setHandler(getCallbackHandler())
					.setProtocol("http-remoting")
					.setConnectionTimeout(timeout2)
					.setSaslOptions(Collections.emptyMap());
			ModelControllerClientConfiguration config = b.build();
			this.client = ModelControllerClient.Factory.create(config);
			this.manager = ServerDeploymentManager.Factory.create(client);
		} catch(RuntimeException uhe) {
			throw new JBoss7ManangerException(uhe);
		}
	}

	protected CallbackHandler getCallbackHandler() {
		return new ToolsWF11CallbackHandler();
	}
	
	protected class ToolsWF11CallbackHandler implements CallbackHandler {
		private String realm;
		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
            if (callbacks.length == 2 &&
            		( callbacks[0] instanceof RealmCallback || callbacks[1] instanceof RealmCallback)
            		&& (callbacks[0] instanceof OptionalNameCallback || callbacks[1] instanceof OptionalNameCallback)) {
            	// No credentials need to be set... 
                return;
            }

            NameCallback nameCB = null;
            PasswordCallback passCB = null;
            CredentialCallback credCB = null;
            for (Callback current : callbacks) {
	            if (current instanceof RealmCallback) {
	            	RealmCallback rcb = (RealmCallback) current;
	            	realm = rcb.getPrompt();
                    String defaultText = rcb.getDefaultText();
                    rcb.setText(defaultText); // For now just use the realm suggested.
	            }
	            if (current instanceof NameCallback) {
                    nameCB = (NameCallback) current;
                } else if (current instanceof PasswordCallback) {
                    passCB = (PasswordCallback) current;
                } else if (current instanceof CredentialCallback) {
                    credCB = (CredentialCallback) current;
                }
            }
            
            if( nameCB instanceof OptionalNameCallback) {
                ((NameCallback) callbacks[0]).setName("anonymous JBossTools user");
            	return;
            }
            
            String passPrompt = (passCB == null ? "Password" : passCB.getPrompt());
            
            String[] results = details.handleCallbacks(new String[] { nameCB.getPrompt(), passPrompt});
            if( results != null && results.length >= 2 ) {
            	String u = results[0];
            	String p = results[1];
            	
	            nameCB.setName(results[0]);
	            if( passCB != null ) {
                    passCB.setPassword(p.toCharArray());
	            } else if( credCB != null ) {
                	String digest = null;
                    if (digest == null && credCB.isCredentialTypeSupported(PasswordCredential.class, ClearPassword.ALGORITHM_CLEAR)) {
                        credCB.setCredential(new PasswordCredential(ClearPassword.createRaw(ClearPassword.ALGORITHM_CLEAR, p.toCharArray())));
                    } else if (digest != null && credCB.isCredentialTypeSupported(PasswordCredential.class, DigestPassword.ALGORITHM_DIGEST_MD5)) {
                        // We don't support an interactive use of this callback so it must have been set in advance.
                        final byte[] bytes = CodePointIterator.ofString(digest).hexDecode().drain();
                        credCB.setCredential(new PasswordCredential(DigestPassword.createRaw(DigestPassword.ALGORITHM_DIGEST_MD5, u, realm, bytes)));
                    } else {
                        CallbackUtil.unsupported(credCB);
                    }
	            }
            }
		}
	}
	
	public IJBoss7DeploymentResult undeploySync(String name, boolean removeFile, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		String task = "Undeploy via management: " + name;
		monitor.beginTask(task, 100);
		DeploymentOperationResult result = (DeploymentOperationResult)undeploy(name, removeFile);
		monitor.worked(5);
		waitFor(result, task, new SubProgressMonitor(monitor, 95));
		monitor.done();
		return result;
	}
	
	private void waitFor(DeploymentOperationResult result, String task, IProgressMonitor monitor) throws JBoss7ManangerException {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Waiting for task to complete: " + task, IProgressMonitor.UNKNOWN);
		while(!monitor.isCanceled() && !result.isDone()) {
            progress.worked(1);
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {
				// Ignore
			}
		}
		if( monitor.isCanceled()) {
			result.cancel();
			throw new JBoss7ManangerException("Operation canceled: " + task);
		}
		result.getStatus();
		monitor.done();
	}

	public IJBoss7DeploymentResult deploySync(String name, File file, 
			boolean add, String[] explodedPaths, IProgressMonitor monitor)
			throws JBoss7ManangerException {
		String task = "Deploy via management: " + name;
		monitor.beginTask(task, 100);
		DeploymentOperationResult result = (DeploymentOperationResult)deploy(name, file, explodedPaths, add);
		monitor.worked(5);
		waitFor(result, task, new SubProgressMonitor(monitor, 95));
		monitor.done();
		return result;
	}

	public IJBoss7DeploymentResult undeploy(String name, boolean removeFile) throws JBoss7ManangerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			if( removeFile ) {
				builder = builder.undeploy(name).andRemoveUndeployed();
			} else { 
				builder = builder.undeploy(name);
			}
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
		return deploy(file.getName(), file, new String[] {file.getName()}, true);
	}

	public IJBoss7DeploymentResult add(String name, File file) throws JBoss7ManangerException {
		try {
			return execute(manager.newDeploymentPlan().add(name, file));
		} catch (IOException e) {
			throw new JBoss7ManangerException(e);
		}
	}
	
	private ArrayList<String> getZippedPathsForDeployment(String deploymentName) {
		ModelNode request = new ModelNode();
		request.get(OP).set("browse-content");
		request.get(ADDRESS).add(DEPLOYMENT, deploymentName);
		request.get("archive").set(true);
		ModelNode result = null;
		try {
			result = execute(request);
		} catch( JBoss7ManangerException j7me ) {
			// Ignore
		}
		
		ArrayList<String> zipped = null;
		if(result.getType() == ModelType.LIST) {
			zipped = new ArrayList<String>();
			for(ModelNode content : result.asList()) {
				if(content.hasDefined("path")) {
					ModelNode path = content.get("path");
					String pathStr = path == null ? null : path.asString();
					zipped.add(pathStr);
				}
			}
		} else if( result.getType() == ModelType.UNDEFINED) {
			zipped = new ArrayList<String>();
		}
		return zipped;
	}
	
	public void explode(String deploymentName, String[] paths) throws JBoss7ManangerException {
		if( paths.length == 0 )
			return;
		
		ArrayList<String> zipped = getZippedPathsForDeployment(deploymentName);
		try {
	        DeploymentPlanBuilder b = manager.newDeploymentPlan()
	                .undeploy(deploymentName);
	        for( int i = 0; i < paths.length; i++ ) {
	        	if( zipped == null || (zipped.contains(paths[i]))) {
	        		b = b.explodeDeploymentContent(deploymentName, paths[i]);
	        	}
	        }
	        
	        b = b.deploy(deploymentName);
	        DeploymentOperationResult dor = new DeploymentOperationResult(b.getLastAction(), manager.execute(b.build()));
	        waitFor(dor, "Deploying Module " + deploymentName, new NullProgressMonitor());
		} catch(Exception e) {
			throw new JBoss7ManangerException(e);
		}
    }	

	public IJBoss7DeploymentResult deploy(String name, File file, String[] explodedPaths, boolean add) throws JBoss7ManangerException {
		try {
			DeploymentPlanBuilder b = manager.newDeploymentPlan();
			if( add ) {
				b = b.add(name, file);
			}
			
			for( int i = 0; i < explodedPaths.length; i++ ) {
				if (name.equals(explodedPaths[i])) {
					b = b.explodeDeployment(explodedPaths[i]);
				} else {
					b.explodeDeploymentContent(name, explodedPaths[i]);
				}
			}
			return execute(b.deploy(name));
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

		Boolean enabled = WildFly11ManagerUtil.getBooleanProperty(ENABLED, result);
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
			if (!WildFly11ManagerUtil.isSuccess(response)) {
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
			DeploymentPlan plan = builder.build();
			Future<ServerDeploymentPlanResult> planResult = manager.execute(plan);
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

    
	public IJBoss7DeploymentResult incrementalPublish(
			IAS7ManagementDetails details, String deploymentName, 
			IncrementalManagementModel model,
			boolean redeploy, IProgressMonitor monitor) throws JBoss7ManangerException {
		
		// First we need to explode child modules
		String[] children = model.getChildrenToExplode(deploymentName);
		
		explode(deploymentName, children);
		
		
		
		DeploymentPlanBuilder b = manager.newDeploymentPlan();
		try {
			String[] deployments = model.getDeploymentIds();
			for (String deployment : deployments) {
				Map<String, String> changed = model.getChanged(deployment);
				List<String> removed = model.getRemoved(deployment);
				if( changed.size() > 0 || removed.size() > 0)
					b = addChanges(deployment, changed, removed, b);
			}
			
			if( redeploy) {
				b = b.redeploy(deploymentName);
			}
			
			DeploymentAction action = b.getLastAction();
			DeploymentPlan plan = b.build();
			Future<ServerDeploymentPlanResult> future = manager.execute(plan);
			DeploymentOperationResult res = new DeploymentOperationResult(action, future, DEFAULT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
			waitFor(res, "Incremental Deployment", new SubProgressMonitor(monitor, 95));
			return res;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JBoss7ManangerException(e);
		}
	}
	
	private DeploymentPlanBuilder addChanges(String deploymentName, 
			Map<String, String> changedContent, 
			List<String> removedContent,
			DeploymentPlanBuilder bd) throws IOException {

		Map<String, InputStream> addStreams = new HashMap<>();
		Iterator<String> changedIt = changedContent.keySet().iterator();
		ArrayList<String> failedChanges = new ArrayList<String>();
		while(changedIt.hasNext()) {
			String k = changedIt.next();
			String v = changedContent.get(k);
			try {
				addStreams.put(k, new FileInputStream(new File(v)));
			} catch (FileNotFoundException e) {
				failedChanges.add(k);
			}
		}
		
		if( addStreams.size() > 0 ) {
			bd = bd.addContentToDeployment(deploymentName, addStreams);
		}
		if( removedContent.size() > 0 ) {
			bd = bd.removeContenFromDeployment(deploymentName, removedContent); 
		}
		return bd;
	}
}
