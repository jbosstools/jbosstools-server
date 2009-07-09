package org.jboss.ide.eclipse.as.test.publishing;

import junit.framework.AssertionFailedError;

import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.internal.ServerPlugin;

// associated with eclipse bug id 241466
public class JBIDE2512bTest extends AbstractDeploymentTest {

	public JBIDE2512bTest() {
		super(new String[] { "JBIDE2512b-ear", "JBIDE2512b-ejb"}, new String[] {null, null});
	}
	
	public void testJBIDE2512b() throws Exception {
		IModuleArtifact[] earArtifacts = ServerPlugin.getModuleArtifacts(workspaceProject[0]);
		assertNotNull(earArtifacts);
		assertEquals(1, earArtifacts.length);
		assertNotNull(earArtifacts[0]);
		IModule earModule = earArtifacts[0].getModule();

		IModuleArtifact[] ejbArtifacts = ServerPlugin.getModuleArtifacts(workspaceProject[1]);
		assertNotNull(ejbArtifacts);
		assertEquals(1, ejbArtifacts.length);
		assertNotNull(ejbArtifacts[0]);
		IModule ejbModule = ejbArtifacts[0].getModule();
		
		IModuleType moduleType = earModule.getModuleType();
		assertTrue("jst.ear".equals(moduleType.getId()));
		IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) earModule
					.loadAdapter(IEnterpriseApplication.class, null);
		assertNotNull(enterpriseApplication);
		
		String uri = enterpriseApplication.getURI(ejbModule);
		assertNotNull("URI is null", uri);
		assertTrue("URI does not have the expected value", "lib/JBIDE2512b-ejb.jar".equals(uri));
	}
}
