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
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.publishing.AbstractDeploymentTest;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;

public class JSTZippedDeploymentTester extends AbstractJSTDeploymentTester {
	
	public void setUp() throws Exception {
		project = createProject();
		server = ServerRuntimeUtils.createMockDeployOnlyServer();
		setZipFlag();
	}
	
	private void setZipFlag() throws IOException, CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(server, wc);
		helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, true);
		server = wc.save(true, new NullProgressMonitor());
	}
	
	@Override
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getEARDataModel(getModuleName(), getContentDir(), null, null, JavaEEFacetConstants.EAR_5, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getModuleName());
		assertTrue(p.exists());
		
		File srcFile = AbstractDeploymentTest.getFileLocation("projectPieces/mvel2.jar");
		IPath contentDir = p.getFolder(getContentDir()).getLocation();
		File destFile = new File(contentDir.toFile(), "mvel2.jar");
		FileUtil.fileSafeCopy(srcFile, destFile);
		p.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		return p;
	}
		
	protected void verifyJSTZippedPublisher(IModule[] module) {
		IJBossServerPublisher publisher = ExtensionManager
			.getDefault().getPublisher(server, module, "local");
		assertTrue(publisher.getClass().getName().contains("WTPZippedPublisher"));
	}
	
	private IModule findModule() {
		IModule[] mods = ServerUtil.getModules(project);
		IModule mod = null;
		// find hte right module ugh
		for( int i = 0; i < mods.length && mod == null; i++ ) {
			if( mods[i].getModuleType().getId().equals("jst.ear"))
				mod = mods[i];
		}
		assertNotNull(mod);
		return mod;
	}
	
	
	public void testZippedDeploymentLocal() throws CoreException, IOException {
		IModule mod = findModule();
		IModule[] module = new IModule[] { mod };
		verifyJSTZippedPublisher(module);
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		IPath projLoc = project.getLocation();
		System.out.println(projLoc);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		IPath zipped = deployRoot.append(getModuleName() + ".ear");
		assertTrue(zipped.toFile().exists());
		assertTrue(zipped.toFile().isFile());
		IPath unzip1 = ASTest.getDefault().getStateLocation().append("unzip1");
		IPath unzip2 = ASTest.getDefault().getStateLocation().append("unzip2");
		IOUtil.unzipFile(zipped,unzip1);
		assertTrue(unzip1.toFile().list().length == 1);
		IOUtil.unzipFile(unzip1.append("mvel2.jar"), unzip2);
		assertTrue(unzip2.toFile().list().length > 1);
		System.out.println("end");
		
		server = ServerRuntimeUtils.removeModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertFalse(zipped.toFile().exists());
	}

	public void testZippedDeploymentMock() throws CoreException, IOException {
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		setZipFlag();
		MockPublishMethod.reset();
		IModule mod = findModule();
		testZippedDeploymentMock(mod,1,1);
	}
	
	public void testZippedDeploymentMockAS7() throws CoreException, IOException {
		// Same as without previous AS versions, as the server can tell when
		// a zipped file is done being transfered or not
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		setZipFlag();
		MockPublishMethod.reset();
		IModule mod = findModule();
		testZippedDeploymentMock(mod,2,1);
	}
	
	private void testZippedDeploymentMock(IModule mod, int pubCount, int removeCount) throws IOException, CoreException {
		MockPublishMethod.reset();
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		int changed = MockPublishMethod.getChanged().length;
		int deleted = MockPublishMethod.getRemoved().length;
		assertEquals(changed, pubCount);
		MockPublishMethod.reset();
		
		server = ServerRuntimeUtils.removeModule(server, mod);
		ServerRuntimeUtils.publish(server);
		deleted = MockPublishMethod.getRemoved().length;
		assertEquals(deleted, removeCount);
		MockPublishMethod.reset();
	}
}
