package org.jboss.ide.eclipse.as.test.publishing;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ServerPlugin;

public class JBIDE1104Test extends AbstractDeploymentTest {

	public JBIDE1104Test() {
		super("SimpleEar", "1104.properties");
	}
	
	public void testJBIDE1104() throws Exception {
		IModuleArtifact[] artifacts = ServerPlugin.getModuleArtifacts(workspaceProject);
		assertNotNull(artifacts);
		assertEquals(1, artifacts.length);
		assertNotNull(artifacts[0]);
		IModule module = artifacts[0].getModule();
		assertNotNull(module);
		IStatus result = publish(module);
		assertEquals(IStatus.OK, result.getSeverity());
		
		// now do my asserts regarding the output structure
		// TODO
		
	}
	
}
