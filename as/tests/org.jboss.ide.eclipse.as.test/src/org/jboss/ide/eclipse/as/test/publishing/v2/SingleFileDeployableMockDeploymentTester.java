package org.jboss.ide.eclipse.as.test.publishing.v2;

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
import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class SingleFileDeployableMockDeploymentTester extends AbstractJSTDeploymentTester {
	public void setUp() throws Exception {
		super.setUp();
		ServerPreferences.getInstance().setAutoPublishing(false);
	}
	public void tearDown() throws Exception {
		ServerPreferences.getInstance().setAutoPublishing(true);
		super.tearDown();
	}
	
	public void testSingleFile() throws CoreException, IOException {
		final String filename = "test.xml";
		IResource file = createFile(filename, "<test>done</test>");
		IModule[] mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 0);
		SingleDeployableFactory.makeDeployable(file);
		mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 1);
		server = ServerRuntimeUtils.addModule(server, mods[0]);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		deployRoot.toFile().mkdirs();
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 0);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 1);
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 1);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 2);
		assertContents(deployRoot.append("test.xml").toFile(), 
			"<test>done</test>");
		IOUtil.setContents(project.getFile(filename), "<test>done2</test>");
		assertContents(deployRoot.append("test.xml").toFile(), "<test>done</test>");
		ServerRuntimeUtils.publish(server);
		assertContents(deployRoot.append("test.xml").toFile(), 
			"<test>done2</test>");
		server = ServerRuntimeUtils.removeModule(server, mods[0]);
		assertContents(deployRoot.append("test.xml").toFile(), 
		"<test>done2</test>");
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 2);
		ServerRuntimeUtils.publish(server);
		assertFalse(deployRoot.append("test.xml").toFile().exists());
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 1);
	}
	
	protected void verifyPublisher(IModule module, Class c) {
//		IModule[] mod = new IModule[] { module };
//		IJBossServerPublisher publisher = ExtensionManager
//			.getDefault().getPublisher(server, mod, "local");
//		assertTrue(publisher.getClass().equals(c));
	}
	
	protected IFile createFile(String filename, String contents) throws CoreException, IOException  {
		IFile resource = project.getFile(filename);
		IOUtil.setContents(resource, contents);
		return resource;
	}
	
	public void testSingleFolder() throws CoreException, IOException {
		IPath moduleDeployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		final String folderName = "test";
		moduleDeployRoot.toFile().mkdirs();
		IModule[] mods = singleFolderCreateModules(folderName);
		singleFolderPublishAndVerify(moduleDeployRoot, folderName, mods);
	}
	
	public void testSingleFolderCustomDeployFolderRelative() throws CoreException, IOException {
		IPath serverDeployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		final String folderName = "test";
		String relativeFolder = "something.foo";
		IPath moduleDeployRoot = serverDeployRoot.append(relativeFolder);
		moduleDeployRoot.toFile().mkdirs();
		IModule[] mods = singleFolderCreateModules(folderName);
		DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferencesFromServer(server);
		DeploymentModulePrefs p = prefs.getOrCreatePreferences().getOrCreateModulePrefs(mods[0]);
		p.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, relativeFolder);
		ServerAttributeHelper helper = new ServerAttributeHelper(server, server.createWorkingCopy());
		DeploymentPreferenceLoader.savePreferencesToServerWorkingCopy(helper, prefs);
		server = helper.save(true, new NullProgressMonitor());
		singleFolderPublishAndVerify(moduleDeployRoot, folderName, mods);
	}

	public void testSingleFolderCustomDeployFolderAbsolute() throws CoreException, IOException {
		IPath serverDeployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		final String folderName = "test";
		IPath moduleDeployRoot = ServerRuntimeUtils.getBaseDir().append("absoluteFolder.place");
		moduleDeployRoot.toFile().mkdirs();
		IModule[] mods = singleFolderCreateModules(folderName);
		DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferencesFromServer(server);
		DeploymentModulePrefs p = prefs.getOrCreatePreferences().getOrCreateModulePrefs(mods[0]);
		p.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, moduleDeployRoot.toOSString());
		ServerAttributeHelper helper = new ServerAttributeHelper(server, server.createWorkingCopy());
		DeploymentPreferenceLoader.savePreferencesToServerWorkingCopy(helper, prefs);
		server = helper.save(true, new NullProgressMonitor());
		singleFolderPublishAndVerify(moduleDeployRoot, folderName, mods);
	}

	
	private IModule[] singleFolderCreateModules(String folderName) throws CoreException, IOException {
		IFolder folder = project.getFolder(folderName);
		folder.create(true, true, new NullProgressMonitor());
		IOUtil.setContents(folder.getFile("1.txt"), "1");
		IOUtil.setContents(folder.getFile("2.txt"), "2");
		IOUtil.setContents(folder.getFile("3.txt"), "3");
		IModule[] mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 0);
		SingleDeployableFactory.makeDeployable(folder);
		mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 1);
		return mods;
	}

	private void singleFolderPublishAndVerify(IPath moduleDeployRoot, String folderName, IModule[] mods) throws CoreException, IOException {
		server = ServerRuntimeUtils.addModule(server, mods[0]);
		assertEquals(IOUtil.countFiles(moduleDeployRoot.toFile()), 0);
		assertEquals(IOUtil.countAllResources(moduleDeployRoot.toFile()), 1);
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(moduleDeployRoot.toFile()), 3);
		assertEquals(IOUtil.countAllResources(moduleDeployRoot.toFile()), 5);
		File folder2 = moduleDeployRoot.toFile().listFiles()[0];
		assertTrue(folder2.getName().equals(folderName));
		File[] folderChildren = folder2.listFiles();
		assertTrue(folderChildren.length == 3);
		File three = new File(folder2, "3.txt");
		assertEquals(IOUtil.getContents(three), "3");
		IFolder folder = project.getFolder(folderName);
		IOUtil.setContents(folder.getFile("3.txt"), "3a");
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(moduleDeployRoot.toFile()), 3);
		assertEquals(IOUtil.countAllResources(moduleDeployRoot.toFile()), 5);
		folder2 = moduleDeployRoot.toFile().listFiles()[0];
		assertTrue(folder2.getName().equals(folderName));
		folderChildren = folder2.listFiles();
		assertTrue(folderChildren.length == 3);
		three = new File(folder2, "3.txt");
		assertEquals(IOUtil.getContents(three), "3a");
	}
	
	public void testSingleFileZipped() throws CoreException, IOException {
		server = ServerRuntimeUtils.setZipped(server, true);
		try {
			testSingleFile();
		} finally {
			server = ServerRuntimeUtils.setZipped(server, false);
		}
	}
}
