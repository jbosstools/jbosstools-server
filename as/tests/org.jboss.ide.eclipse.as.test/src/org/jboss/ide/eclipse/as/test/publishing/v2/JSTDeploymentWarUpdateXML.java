/******************************************************************************* 
 * Copyright (c) 2010 - 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;

public class JSTDeploymentWarUpdateXML extends AbstractJSTDeploymentTester {
	
	@Override
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel(MODULE_NAME, null, null, CONTENT_DIR, null, JavaEEFacetConstants.WEB_25, true);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(MODULE_NAME);
		assertTrue(p.exists());
		return p;
	}
		
	public void testMain() throws CoreException, IOException {
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		verifyJSTPublisher(module);
		IFile f = project.getFolder(CONTENT_DIR).getFolder("WEB-INF").getFile("web.xml");

		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		IPath rootFolder = deployRoot.append(MODULE_NAME + ".war");
		assertTrue(rootFolder.toFile().exists());

		long workspaceModified = project.getFolder(CONTENT_DIR).getFolder("WEB-INF").getFile("web.xml").getLocation().toFile().lastModified();
		long publishedModified = rootFolder.append("WEB-INF").append("web.xml").toFile().lastModified();
		
		// FULL PUBLISH and verify web xml's timestamp
		ServerRuntimeUtils.publish(IServer.PUBLISH_FULL, server);
		try {
			Thread.sleep(400);
		} catch(InterruptedException ie) {}
		
		long publishedModified2 = rootFolder.append("WEB-INF").append("web.xml").toFile().lastModified();
		assertNotSame(publishedModified, publishedModified2);
		
		server = ServerRuntimeUtils.removeModule(server, mod);
		assertTrue(rootFolder.toFile().exists());
		ServerRuntimeUtils.publish(server);
		assertFalse(rootFolder.toFile().exists());
	}
	
	public void testWarUpdateMockPublishMethod() throws CoreException, IOException {
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		testMockPublishMethod(7,"newModule.war");
	}
	
	public void testWarUpdateMockPublishMethodJBoss7() throws CoreException, IOException {
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		/*
		 * removing module newModule.war will remove 2 markers:
		 * <ul>
		 * 	<li>newModule.war.deployed</li>
		 *  <li>newModule.war.failed</li>
		 * </ul>
		 */
		testMockPublishMethod(8,"newModule.war" + DeploymentMarkerUtils.FAILED_DEPLOY,"newModule.war" + DeploymentMarkerUtils.DEPLOYED);
	}
	
	private void testMockPublishMethod(int initial, String... filesToRemove) throws CoreException, IOException {
		// add
		MockPublishMethod.reset();
		IModule mod = ServerUtil.getModule(project);
		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		assertEquals(initial, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		
		// remove
		server = ServerRuntimeUtils.removeModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertEquals(filesToRemove.length, MockPublishMethod.getRemoved().length);
		IPath[] removedFiles = MockPublishMethod.getRemoved();
		for(int i = 0; i < removedFiles.length; i++) {
			assertEquals(filesToRemove[i], removedFiles[i].toString());
		}
		MockPublishMethod.reset();
	}
}
