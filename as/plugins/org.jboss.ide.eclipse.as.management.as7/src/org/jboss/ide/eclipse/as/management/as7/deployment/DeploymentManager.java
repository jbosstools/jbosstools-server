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

import static org.jboss.as.protocol.StreamUtils.safeClose;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;

/**
 * Used to deploy/undeploy deployments to a running <b>standalone</b>
 * application server
 * 
 * @author <a href="adietish@redhat.com">André Dietisheim</a>
 */
public class DeploymentManager {

	public static final long DEFAULT_TIMEOUT = 15000;

	private final List<IDeploymentPlanBuilderOperation> deployments = new ArrayList<IDeploymentPlanBuilderOperation>();
	private final ModelControllerClient client;
	private final ServerDeploymentManager manager;
	private long timeout = DEFAULT_TIMEOUT;

	public DeploymentManager(String host, int port) throws UnknownHostException {
		client = ModelControllerClient.Factory.create(host, port);
		manager = ServerDeploymentManager.Factory.create(client);
	}

	public synchronized DeploymentManager deploy(File file) {
		deployments.add(new DeployOperation(file));
		return this;
	}

	public synchronized DeploymentManager deploy(String name, File file) {
		deployments.add(new DeployOperation(name, file));
		return this;
	}

	public synchronized DeploymentManager undeploy(String name) {
		deployments.add(new UndeployOperation(name));
		return this;
	}

	public synchronized DeploymentManager undeploy(File file) {
		deployments.add(new UndeployOperation(file));
		return this;
	}

	public synchronized void execute() throws DeployerException {
		try {
			DeploymentPlanBuilder builder = manager.newDeploymentPlan();
			for (IDeploymentPlanBuilderOperation deployment : deployments) {
				builder = deployment.addTo(builder);
			}
			manager.execute(builder.build()).get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new DeployerException(e);
		}
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void dispose() {
		safeClose(client);
	}

	private static class DeployOperation extends FileOperation {

		private DeployOperation(File file) {
			super(file);
		}

		private DeployOperation(String name, File file) {
			super(name, file);
		}

		public synchronized DeploymentPlanBuilder addTo(DeploymentPlanBuilder builder) throws Exception {
			String name = getName();
			return builder.add(name, getFile()).deploy(name);
		}		
	}
	
	private static class UndeployOperation extends FileOperation {

		private UndeployOperation(File file) {
			super(file);
		}

		private UndeployOperation(String name) {
			super(name, null);
		}

		public synchronized DeploymentPlanBuilder addTo(DeploymentPlanBuilder builder) throws Exception {
			String name = getName();
			return builder.undeploy(name).undeploy(name);
		}
	}
	
	private abstract static class FileOperation extends NamedOperation {

		private File file;

		private FileOperation(File file) {
			this(null, file);
		}

		private FileOperation(String name, File file) {
			super(name);
			this.file = file;
		}

		protected File getFile() {
			return file;
		}

		protected String getName() {
			if (name != null) {
				return name;
			} else {
				return file.getName();
			}
		}

	}

	private abstract static class NamedOperation implements IDeploymentPlanBuilderOperation {

		protected String name;

		private NamedOperation(String name) {
			this.name = name;
		}
	}

	private interface IDeploymentPlanBuilderOperation {

		public DeploymentPlanBuilder addTo(DeploymentPlanBuilder builder) throws Exception;

	}
}
