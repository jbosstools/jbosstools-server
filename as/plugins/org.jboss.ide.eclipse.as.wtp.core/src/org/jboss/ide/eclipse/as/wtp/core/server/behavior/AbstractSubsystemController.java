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
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel.SubsystemType;

/**
 * @since 3.0
 */
public abstract class AbstractSubsystemController implements ISubsystemController {
	static final String REQUIRED_PROPERTIES_ENV_KEY = ".RESERVED_requiredProperties";
	
	
	private SubsystemType subsystemType = null;
	private IServer server;
	private Map<String, Object> environment = null;
	
	/**
	 * Get the server this controller was instantiated with
	 * @return
	 */
	protected IServer getServer() {
		return server;
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
	
	protected ISubsystemController findDependency(String system) throws CoreException {
		return findDependency(system, server.getServerType().getId());
	}
	
	/**
	 * This method should not be used and is only exposed for testing purposes
	 * 
	 * @param system
	 * @param serverType
	 * @return
	 * @throws CoreException
	 */
	protected ISubsystemController findDependency(String system, String serverType) throws CoreException {
		// If we're declared to require a specific subsystem, pull that one first and ignore the environment
		Map<String, String> reqs = subsystemType.getRequiredSubsystems();
		String defaultSubsystem = reqs.get(system);
		return SubsystemModel.getInstance().createSubsystemController(server, serverType, system, (Map<String,String>)null, defaultSubsystem, environment);
	}
	
	public void initialize(IServer server, SubsystemType type, Map<String, Object> environment) {
		this.server = server;
		this.subsystemType = type;
		this.environment = environment;
	}
	
	public String getSubsystemId() {
		return subsystemType.getId();
	}

	public String getSystemId() {
		return subsystemType.getSystem();
	}

	public IStatus validate() {
		if( subsystemType.isValid()) {
			return Status.OK_STATUS;
		}
		return subsystemType.getValidationError();
	}

}
