package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.publishers.SingleFilePublisher;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;

public class SingleFileDeploymentTester extends JSTDeploymentTester {
	public void testMain() throws CoreException, IOException {
		IFile file = createXMLFile();
		IModule[] mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 0);
		SingleDeployableFactory.makeDeployable(file);
		mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 1);
		verifyPublisher(mods[0]);
		addModule(mods[0]);
		IPath deployRoot = new Path(getDeployRoot(server));
		deployRoot.toFile().mkdirs();
		assertEquals(countFiles(deployRoot.toFile()), 0);
		assertEquals(countAllResources(deployRoot.toFile()), 1);
		publish();
		assertEquals(countFiles(deployRoot.toFile()), 1);
		assertEquals(countAllResources(deployRoot.toFile()), 2);
		assertContents(deployRoot.append("test.xml").toFile(), 
			"<test>done</test>");
		setContents(file, "<test>done2</test>");
		assertContents(deployRoot.append("test.xml").toFile(), 
		"<test>done</test>");
		publish();
		assertContents(deployRoot.append("test.xml").toFile(), 
			"<test>done2</test>");
		removeModule(mods[0]);
		assertContents(deployRoot.append("test.xml").toFile(), 
		"<test>done2</test>");
		assertEquals(countAllResources(deployRoot.toFile()), 2);
		publish();
		assertFalse(deployRoot.append("test.xml").toFile().exists());
		assertEquals(countAllResources(deployRoot.toFile()), 1);
	}
	
	protected void verifyPublisher(IModule module) {
		IModule[] mod = new IModule[] { module };
		IJBossServerPublisher publisher = ExtensionManager
			.getDefault().getPublisher(server, mod, "local");
		assertTrue(publisher instanceof SingleFilePublisher);
	}
	
	protected IFile createXMLFile() throws CoreException, IOException  {
		IFile xmlFile = project.getFile("test.xml");
		setContents(xmlFile, "<test>done</test>");
		return xmlFile;
	}
}
