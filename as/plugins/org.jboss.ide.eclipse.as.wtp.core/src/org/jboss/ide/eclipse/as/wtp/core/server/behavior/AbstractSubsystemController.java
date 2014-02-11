/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel.Subsystem;

/**
 * @since 3.0
 */
public abstract class AbstractSubsystemController implements ISubsystemController {
	static final String REQUIRED_PROPERTIES_ENV_KEY = SubsystemModel.REQUIRED_PROPERTIES_ENV_KEY;
	
	
	private Subsystem subsystem = null;
	private IServerAttributes server;
	private Map<String, Object> environment = null;
	
	/**
	 * Get the server this controller was instantiated with
	 * @return
	 */
	protected IServer getServer() {
		if( server instanceof IServerWorkingCopy)
			return ((IServerWorkingCopy)server).getOriginal();
		if( server instanceof IServer)
			return ((IServer)server);
		return null;
	}
	
	/**
	 * Convert the given server into a {@link ControllableServerBehavior} if possible
	 * @return
	 */
	protected IControllableServerBehavior getControllableBehavior() {
		if( server != null ) {
			ServerBehaviourDelegate del = (ServerBehaviourDelegate)server.loadAdapter(ServerBehaviourDelegate.class, null);
			if( del instanceof ControllableServerBehavior)
				return (IControllableServerBehavior)del;
		}
		return null;
	}
	
	protected Map<String, Object> getEnvironment() {
		return environment;
	}
	
	/**
	 * Find a dependency by calling the behavior and asking it for the proper controller. 
	 * This ensures that the most up-to-date dependency based on current server state is found. 
	 * 
	 * @param system
	 * @return
	 * @throws CoreException
	 */
	protected ISubsystemController findDependencyFromBehavior(String system) throws CoreException {
		return getControllableBehavior() == null ? null : getControllableBehavior().getController(system);
	}

	/**
	 * Find a dependency via the SubsystemModel using the environment flags to help
	 * locate one that matches all the requirements in terms of properties. 
	 * 
	 * @param system
	 * @return
	 * @throws CoreException
	 */
	protected ISubsystemController findDependency(String system) throws CoreException {
		return findDependency(system, server.getServerType().getId());
	}
	
	
	/**
	 * This method should not be used and is only exposed for testing purposes.
	 * This method is guaranteed to throw a CoreException if a dependency cannot be found. 
	 * This method will locate a dependency for your server type and the given system. 
	 * 
	 * @param system
	 * @param serverType
	 * @return
	 * @throws CoreException 
	 */
	protected ISubsystemController findDependency(String system, String serverType) throws CoreException {
		return findDependency(system, serverType, environment);
	}
	
	/**
	 * This method should not be used and is only exposed for testing purposes.
	 * This method is guaranteed to throw a CoreException if a dependency cannot be found. 
	 * This method will locate a dependency for your server type and the given system. 
	 * 
	 * @param system
	 * @param serverType
	 * @param environment
	 * @return
	 * @throws CoreException 
	 */
	protected ISubsystemController findDependency(String system, String serverType, Map<String, Object> environment) throws CoreException {

		// If we're declared to require a specific subsystem, pull that one first and ignore the environment
		Map<String, String> reqs = subsystem.getRequiredSubsystems();
		String defaultSubsystem = reqs.get(system);
		return SubsystemModel.getInstance().createSubsystemController(server, serverType, system, (Map<String,String>)null, defaultSubsystem, environment);
	}
	
	public void initialize(IServerAttributes server, Subsystem type, Map<String, Object> environment) {
		this.server = server;
		this.subsystem = type;
		this.environment = (environment == null ? new HashMap<String, Object>() : environment);
	}
	
	public String getSubsystemMappedId() {
		if( subsystem != null )
			return subsystem.getMappedId();
		return null;
	}

	public String getSystemId() {
		if( subsystem != null )
			return subsystem.getSystemId();
		return null;
	}

	public IStatus validate() {
		if( subsystem != null ) {
			if( subsystem.isValid()) {
				return Status.OK_STATUS;
			}
			return subsystem.getValidationError();
		}
		// This should never occur unless the framework did not instantiate the object
		return Status.OK_STATUS;
	}

	
	protected IServerAttributes getServerOrWC() {
		return server;
	}
	
	protected IServerWorkingCopy getWorkingCopy() {
		if( server instanceof IServerWorkingCopy)
			return (IServerWorkingCopy)server;
		
		return null;
	}
	
}
