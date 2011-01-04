package org.jboss.ide.eclipse.as.archives.integration.test;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.webtools.modules.WTPZippedPublisher;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.publishing.v2.SingleFileDeploymentTester;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class SingleFileZippedDeploymentIntegrationTest extends SingleFileDeploymentTester {
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
		File f0 = deployRoot.toFile();
		assertFalse(f0.exists());
		assertTrue(deployRoot.toFile().mkdirs());
		assertEquals(IOUtil.countFiles(deployRoot.toFile()), 0);
		assertEquals(IOUtil.countAllResources(deployRoot.toFile()), 1);
		
		// publish and verify deployment
		File publishLog = ServerLogger.getDefault().getServerLogFile(server);
		publishLog.delete();
		assertTrue(!publishLog.exists());
		IJBossServerPublisher publisher = ExtensionManager.getDefault().getPublisher(server, mods, "local");
		assertNotNull(publisher);
		assertTrue(publisher.getClass().getName() + " not equal to WTPZippedPublisher", 
				publisher.getClass().equals(WTPZippedPublisher.class));
		ServerRuntimeUtils.publish(server);
		assertTrue(publishLog.exists());
		System.out.println(IOUtil.getContents(publishLog));
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
