package org.jboss.ide.eclipse.as.test.defects;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.CreateProjectOperationsUtility;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;

public class DefectTest extends TestCase {
	public void testJBIDE6184_Odd_Republish_Error() throws Exception {
    	IDataModel dm = CreateProjectOperationsUtility.getEARDataModel("ear", "thatContent", null, null, JavaEEFacetConstants.EAR_5, true);
    	OperationTestCase.runAndVerify(dm);
    	IDataModel dyn1Model = CreateProjectOperationsUtility.getWebDataModel("d1", "ear", null, null, null, JavaEEFacetConstants.WEB_23, true);
    	OperationTestCase.runAndVerify(dyn1Model);
    	IDataModel dyn2Model = CreateProjectOperationsUtility.getWebDataModel("d2", "ear", null, null, null, JavaEEFacetConstants.WEB_23, true);
    	OperationTestCase.runAndVerify(dyn2Model);

    	// Create a temp server
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_AS_50, "server1", "default");
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.modifyModules(getModule(findProject("ear")), new IModule[]{}, new NullProgressMonitor());
		server = wc.save(true, new NullProgressMonitor());
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		
		// verify 
		DeployableServer ds = (DeployableServer)server.loadAdapter(DeployableServer.class, new NullProgressMonitor());
		String folder = ds.getDeployFolder();
		IPath earPath = new Path(folder).append("ear.ear");
		JBIDE6184EarHasDynProjs(earPath, true);
		
		// undeploy
		wc = server.createWorkingCopy();
		wc.modifyModules(new IModule[]{}, getModule(findProject("ear")), new NullProgressMonitor());
		server = wc.save(true, new NullProgressMonitor());
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		assertFalse(earPath.toFile().exists());
		
		final IProject projectA = findProject("d1");
		Job deleteJob = new Job("delete d1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					projectA.delete(true, new NullProgressMonitor());
				} catch(CoreException ce) {
					return ce.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		deleteJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		deleteJob.schedule();
		while(deleteJob.getResult() == null ) {
			// spin
			Thread.sleep(1000);
		}
		
		// republish the ear
		wc = server.createWorkingCopy();
		wc.modifyModules(getModule(findProject("ear")), new IModule[]{}, new NullProgressMonitor());
		server = wc.save(true, new NullProgressMonitor());
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		JBIDE6184EarHasDynProjs(earPath, false);
		
		
		// recreate the war
    	dyn1Model = CreateProjectOperationsUtility.getWebDataModel("d1", "ear", null, null, null, JavaEEFacetConstants.WEB_23, true);
    	OperationTestCase.runAndVerify(dyn1Model);
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		JBIDE6184EarHasDynProjs(earPath, true);
	}
	
	protected void JBIDE6184EarHasDynProjs(IPath earPath, boolean d1Present ) {
		assertTrue(earPath.toFile().exists());
		assertTrue(earPath.append("META-INF").toFile().exists());
		assertTrue(earPath.append("META-INF").append("application.xml").toFile().exists());
		assertTrue(earPath.toFile().isDirectory());
		
		if( d1Present ) {
			assertTrue(earPath.append("d1.war").toFile().exists());
			assertTrue(earPath.append("d1.war").append("WEB-INF").toFile().exists());
			assertTrue(earPath.append("d1.war").append("META-INF").toFile().exists());
			assertTrue(earPath.append("d1.war").append("META-INF").append("MANIFEST.MF").toFile().exists());
			assertTrue(earPath.append("d1.war").append("WEB-INF").append("web.xml").toFile().exists());
		} else {
			assertFalse(earPath.append("d1.war").toFile().exists());
		}

		assertTrue(earPath.append("d2.war").toFile().exists());
		assertTrue(earPath.append("d2.war").append("WEB-INF").toFile().exists());
		assertTrue(earPath.append("d2.war").append("META-INF").toFile().exists());
		assertTrue(earPath.append("d2.war").append("META-INF").append("MANIFEST.MF").toFile().exists());
		assertTrue(earPath.append("d2.war").append("WEB-INF").append("web.xml").toFile().exists());
		
		
	}
	
	protected IModule[] getModule(IProject p) {
		return new IModule[]{ServerUtil.getModule(p)};
	}
	protected IProject findProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}
}
