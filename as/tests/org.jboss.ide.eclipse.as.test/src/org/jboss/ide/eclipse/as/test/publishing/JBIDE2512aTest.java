package org.jboss.ide.eclipse.as.test.publishing;

import java.io.File;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ServerPlugin;

// associated with eclipse bug id 241332
public class JBIDE2512aTest extends AbstractDeploymentTest {

	public JBIDE2512aTest() {
		super("SimpleEar", "2512a.properties");
	}
	
	/**
	 * @FailureExpected This bug upstream means this failure is expected
	 */
	public void testJBIDE2512a() throws Exception {
		IModuleArtifact[] artifacts = ServerPlugin.getModuleArtifacts(workspaceProject[0]);
		assertNotNull(artifacts);
		assertEquals(1, artifacts.length);
		assertNotNull(artifacts[0]);
		IModule module = artifacts[0].getModule();
		assertNotNull(module);
		IStatus result = publish(module);
		assertEquals(IStatus.OK, result.getSeverity());

		// now verify the deployment
		// now do my asserts regarding the output structure
		File depLoc = new File(deployLocation);
		File projLoc = new File(depLoc, "SimpleEar.ear");
		assertNotNull(projLoc);
		assertTrue(projLoc.exists());
		assertTrue(projLoc.isDirectory());
		
		// new stuff
		File shouldNotExist = new File(projLoc, "EJB3WithDescriptor.jar");
		File libFolder = new File(projLoc, "lib");
		File shouldExist = new File(libFolder, "EJB3WithDescriptor.jar");
		
		// Expected to fail currently so wrap
		assertTrue(shouldExist.exists());
		assertFalse(shouldNotExist.exists());
	}
}
