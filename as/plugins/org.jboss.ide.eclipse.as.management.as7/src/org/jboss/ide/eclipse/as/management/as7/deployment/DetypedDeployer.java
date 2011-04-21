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

import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ADD;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ADDRESS;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ENABLED;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.INPUT_STREAM_INDEX;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.REMOVE;
import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.UNDEPLOY;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.protocol.StreamUtils;
import org.jboss.dmr.ModelNode;

/**
 * @author André Dietisheim
 */
public class DetypedDeployer {

	public static void undeploy(String name, String host, int port) throws DeployerException {
		ModelControllerClient client = null;
		try {
			client = ModelControllerClient.Factory.create(host, port);
			// undeploy
			ModelNode request = new ModelNode();
			request.get(OP).set(UNDEPLOY);
			request.get(ADDRESS).add(DEPLOYMENT, name);
			ModelNode result = client.execute(request);
			throwOnFailure(result);

			remove(name, host, port);
			
		} catch (Exception e) {
			throw new DeployerException(e);
		} finally {
			StreamUtils.safeClose(client);
		}
	}

	public static void remove(String name, String host, int port) throws DeployerException {
		ModelControllerClient client = null;
		try {
			client = ModelControllerClient.Factory.create(host, port);

			// remove
			ModelNode request = new ModelNode();
			request.get(OP).set(REMOVE);
			request.get(ADDRESS).add(DEPLOYMENT, name);
			client.execute(request);
		} catch (Exception e) {
			throw new DeployerException(e);
		} finally {
			StreamUtils.safeClose(client);
		}
	}

	public static void deploy(File file, String host, int port) throws DeployerException {
		deploy(file.getName(), file, host, port);
	}

	public static void deploy(String name, File file, String host, int port) throws DeployerException {
		ModelControllerClient client = null;
		try {
			client = ModelControllerClient.Factory.create(host, port);

			ModelNode request = new ModelNode();
			request.get(OP).set(ADD);
			request.get(ADDRESS).add(DEPLOYMENT, name);
			request.get(ENABLED).set(true);

			OperationBuilder builder = OperationBuilder.Factory.create(request);
			builder.addInputStream(new BufferedInputStream(new FileInputStream(file)));
			Operation operation = builder.build();
			request.get(INPUT_STREAM_INDEX).set(0);

			ModelNode result = client.execute(operation);
			System.out.println(result);

			throwOnFailure(result);
		} catch (Exception e) {
			throw new DeployerException(e);
		} finally {
			StreamUtils.safeClose(client);
		}
	}

	public static void replace(String name, File file, String host, int port) throws DeployerException {
		ModelControllerClient client = null;
		try {
			client = ModelControllerClient.Factory.create(host, port);

			ModelNode request = new ModelNode();
			request.get("operation").set("full-replace-deployment");
			request.get("name").set(name);

			OperationBuilder builder = OperationBuilder.Factory.create(request);
			builder.addInputStream(new BufferedInputStream(new FileInputStream(file)));
			Operation operation = builder.build();
			request.get("input-stream-index").set(0);

			ModelNode result = client.execute(operation);

			throwOnFailure(result);
		} catch (Exception e) {
			throw new DeployerException(e);
		} finally {
			StreamUtils.safeClose(client);
		}
	}

	public static boolean isDeployed(String name, String host, int port) throws CancellationException, IOException {
		ModelControllerClient client = ModelControllerClient.Factory.create(host, port);
		try {
			return AS7ManagerUtil.isDeployed(name, client);
		} finally {
			StreamUtils.safeClose(client);
		}
	}

	public static List<String> getDeployments(String host, int port) throws UnknownHostException {
		ModelControllerClient client = ModelControllerClient.Factory.create(host, port);
		return AS7ManagerUtil.getDeployments(client);
	}

	private static void throwOnFailure(ModelNode result) throws DeployerException {
		if (!AS7ManagerUtil.isSuccess(result)) {
			throw new DeployerException(AS7ManagerUtil.getFailureDescription(result));
		}
	}

	private DetypedDeployer() {
		// inhibit instantiation
	}
}
