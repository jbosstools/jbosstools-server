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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.test.publishing.AbstractDeploymentTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;

public class JSTModuleDeployFoldersTest extends AbstractJSTDeploymentTester {
	protected String getModuleName() {
		return super.getModuleName() + "JSTModuleDeployFoldersTest";
	}
	public void tearDown() throws Exception {
		super.tearDown();
		MockPublishMethod.reset();
	}
	
	@Override
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel(getModuleName(), null, null, null, null, JavaEEFacetConstants.WEB_24, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getModuleName());
		assertTrue(p.exists());
		File srcFile = AbstractDeploymentTest.getFileLocation("projectPieces/EJB3NoDescriptor.jar");
		String proj = p.getLocation().toOSString();
		p.getFolder("WebContent").getFolder("WEB-INF")
			.getFolder("lib").getFile("test.jar").create(
				new FileInputStream(srcFile), true, new NullProgressMonitor());
		p.refreshLocal(0, new NullProgressMonitor());
		return p;
	}

	
	public void testStandardDeployAndTempFolders() throws CoreException, IOException {
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		MockPublishMethod.reset();
		
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertEquals(9,MockPublishMethod.getChanged().length);
		
		IPath[] temps = MockPublishMethod.getTempPaths();
		assertEquals(4, temps.length);
		for( int i = 0; i < temps.length; i++ ) {
			if( temps[i].lastSegment().equals("MANIFEST.MF"))
				assertTrue(temps[i].segment(0).equals("mockTempRoot"));
			if( temps[i].lastSegment().equals("web.xml"))
				assertTrue(temps[i].segment(0).equals("mockTempRoot"));
			if( temps[i].lastSegment().equals("test.jar"))
				assertTrue(temps[i].segment(0).equals("mockTempRoot"));
			
			// Markers don't need to be copied over in this way via a temp folder
			if( temps[i].lastSegment().endsWith(".dodeploy"))
				assertTrue(temps[i].segment(0).equals("mockRoot"));
		}
		MockPublishMethod.reset();
	}

	
	public void testPerModuleDeployAndTempFolders() throws CoreException, IOException {
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		MockPublishMethod.reset();
		
		// THIS NEEDS A REAL API. THIS IS HORRIBLE
		IModule mod = ServerUtil.getModule(project);
		DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferencesFromServer(server);
		DeploymentModulePrefs p = prefs.getOrCreatePreferences(LocalPublishMethod.LOCAL_PUBLISH_METHOD)
				.getOrCreateModulePrefs(mod);
		p.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, "/newRoot");
		p.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC, "/newTempRoot");
		IServerWorkingCopy wc = server.createWorkingCopy();
		DeploymentPreferenceLoader.savePreferencesToServerWorkingCopy(wc, prefs);
		server = wc.save(true, null);

		
		MockPublishMethod.reset();
		MockPublishMethod.setExpectedRoot("/newRoot");
		MockPublishMethod.setExpectedTempRoot("/newTempRoot");
		
		IModule[] module = new IModule[] { mod };
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertEquals(9,MockPublishMethod.getChanged().length);
		
		IPath[] temps = MockPublishMethod.getTempPaths();
		assertEquals(4, temps.length);
		for( int i = 0; i < temps.length; i++ ) {
			if( temps[i].lastSegment().equals("MANIFEST.MF"))
				assertTrue(temps[i].segment(0).equals("newTempRoot"));
			if( temps[i].lastSegment().equals("web.xml"))
				assertTrue(temps[i].segment(0).equals("newTempRoot"));
			if( temps[i].lastSegment().equals("test.jar"))
				assertTrue(temps[i].segment(0).equals("newTempRoot"));
			
			// Markers don't need to be copied over in this way via a temp folder
			if( temps[i].lastSegment().endsWith(".dodeploy"))
				assertTrue(temps[i].segment(0).equals("newRoot"));
		}
		MockPublishMethod.reset();
	}

}
