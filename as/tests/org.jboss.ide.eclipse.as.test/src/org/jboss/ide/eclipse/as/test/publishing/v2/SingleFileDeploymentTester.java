package org.jboss.ide.eclipse.as.test.publishing.v2;

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
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.publishers.SingleFilePublisher;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class SingleFileDeploymentTester extends JSTDeploymentTester {
	public void testSingleFile() throws CoreException, IOException {
		final String filename = "test.xml";
		IResource file = createFile(filename, "<test>done</test>");
		IModule[] mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 0);
		SingleDeployableFactory.makeDeployable(file);
		mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 1);
		verifyPublisher(mods[0], SingleFilePublisher.class);
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
		assertContents(deployRoot.append("test.xml").toFile(), 
		"<test>done</test>");
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
		IModule[] mod = new IModule[] { module };
		IJBossServerPublisher publisher = ExtensionManager
			.getDefault().getPublisher(server, mod, "local");
		assertTrue(publisher.getClass().equals(c));
	}
	
	protected IFile createFile(String filename, String contents) throws CoreException, IOException  {
		IFile resource = project.getFile(filename);
		IOUtil.setContents(resource, contents);
		return resource;
	}
	
	public void testSingleFolder() throws CoreException, IOException {
		final String folderName = "test";
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
		verifyPublisher(mods[0], SingleFilePublisher.class);
		server = ServerRuntimeUtils.addModule(server, mods[0]);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		deployRoot.toFile().mkdirs();
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 0);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 1);
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 3);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 4);
		File folder2 = deployRoot.toFile().listFiles()[0];
		assertTrue(folder2.getName().equals(folderName));
		File[] folderChildren = folder2.listFiles();
		assertTrue(folderChildren.length == 3);
		File three = new File(folder2, "3.txt");
		assertEquals(IOUtil.getContents(three), "3");
		IOUtil.setContents(folder.getFile("3.txt"), "3a");
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 3);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 4);
		folder2 = deployRoot.toFile().listFiles()[0];
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
	
	public void testSingleFolderZipped() throws CoreException, IOException {
		server = ServerRuntimeUtils.setZipped(server, true);
		try {
			singleFolderZippedInternal2();
		} finally {
			server = ServerRuntimeUtils.setZipped(server, false);
		}
	}
	
	public void singleFolderZippedInternal2() throws CoreException, IOException {
		// create proj and files
		final String folderName = "test";
		IFolder folder = project.getFolder(folderName);
		folder.create(true, true, new NullProgressMonitor());
		IOUtil.setContents(folder.getFile("1.txt"), "1");
		IOUtil.setContents(folder.getFile("2.txt"), "2");
		IOUtil.setContents(folder.getFile("3.txt"), "3");
		IModule[] mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 0);
		
		// make deployable, do checks
		SingleDeployableFactory.makeDeployable(folder);
		mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 1);
		server = ServerRuntimeUtils.addModule(server, mods[0]);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		deployRoot.toFile().mkdirs();
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 0);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 1);

		// publish and verify deployment
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 1);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 2);
		
		// unzip, verify 3.txt
		IPath unzip1 = ASTest.getDefault().getStateLocation().append("unzip1");
		IOUtil.unzipFile(deployRoot.append(folderName),unzip1);
		assertEquals(IOUtil.countFiles(unzip1.toFile()), 3);
		String deploy3txt1 = IOUtil.getContents(unzip1.append("3.txt").toFile());
		assertEquals("3", deploy3txt1);
		
		// make workspace change, repeat
		IOUtil.setContents(folder.getFile("3.txt"), "3a");
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 1);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 2);

		// verify new changes
		IPath unzip2 = ASTest.getDefault().getStateLocation().append("unzip2");
		IOUtil.unzipFile(deployRoot.append(folderName),unzip2);
		assertEquals(IOUtil.countFiles(unzip2.toFile()), 3);
		String deploy3txt2 = IOUtil.getContents(unzip2.append("3.txt").toFile());
		assertEquals("3a", deploy3txt2);

	}
}
