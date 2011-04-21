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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentManager;

/**
 * @author Rob Stryker
 */
public class JBossDeploymentManager implements IJBoss7DeploymentManager {

	public DeploymentOperationResult deployAsync(String host, int port, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.deploy(deploymentName, file);
	}

	public DeploymentOperationResult deploySync(String host, int port, String deploymentName,
			File file, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.deploySync(deploymentName, file, monitor);
	}

	public DeploymentOperationResult undeployAsync(String host, int port, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.undeploy(deploymentName);
	}

	public DeploymentOperationResult syncUndeploy(String host, int port, String deploymentName,
			boolean removeFile, IProgressMonitor monitor) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.undeploySync(deploymentName, monitor);
	}

	public DeploymentState getDeploymentState(String host, int port, String deploymentName) throws Exception {
		AS7Manager manager = new AS7Manager(host, port);
		return manager.getDeploymentState(deploymentName);

	}
}
