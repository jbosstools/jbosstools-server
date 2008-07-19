package org.jboss.ide.eclipse.as.test.publishing;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ServerPlugin;

public class JBIDE1104Test extends AbstractDeploymentTest {

	public JBIDE1104Test() {
		super("SimpleEar", "1104.properties");
	}
	
	public void testJBIDE1104() throws Exception {
		IModuleArtifact[] artifacts = ServerPlugin.getModuleArtifacts(workspaceProject[0]);
		assertNotNull(artifacts);
		assertEquals(1, artifacts.length);
		assertNotNull(artifacts[0]);
		IModule module = artifacts[0].getModule();
		assertNotNull(module);
		IStatus result = publish(module);
		assertEquals(IStatus.OK, result.getSeverity());
		
		// now do my asserts regarding the output structure
		File depLoc = new File(deployLocation);
		File projLoc = new File(depLoc, "SimpleEar.ear");
		assertNotNull(projLoc);
		assertTrue(projLoc.exists());
		assertTrue(projLoc.isDirectory());
		File sarFile = new File(projLoc, "directory-monitor.sar");
		File ejbFile = new File(projLoc, "EJB3WithDescriptor.jar");
		File metainfFile = new File(projLoc, "META-INF");
		assertTrue(sarFile != null);
		assertTrue(sarFile.exists());
		assertTrue(sarFile.isFile());
		assertTrue(ejbFile != null);
		assertTrue(ejbFile.exists());
		assertTrue(ejbFile.isFile());
		assertTrue(metainfFile != null);
		assertTrue(metainfFile.exists());
		assertTrue(metainfFile.isDirectory());
		File appxmlFile = new File(metainfFile, "application.xml");
		File jbossappxmlFile = new File(metainfFile, "jboss-app.xml");
		assertTrue(appxmlFile != null);
		assertTrue(appxmlFile.exists());
		assertTrue(appxmlFile.isFile());
		assertTrue(jbossappxmlFile != null);
		assertTrue(jbossappxmlFile.exists());
		assertTrue(jbossappxmlFile.isFile());
	}
	
}
