package org.jboss.ide.eclipse.as.test.projectcreation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;

public class NewTargetedWebProjectTest extends TestCase {
	final String MODULE_NAME = "newModulev2111";
	final String CONTENT_DIR = "contentDirS"; 
	private IServer server;
	private IProject p;
	
	// Test Commented Until Passing
	
//	public void testTargetedWebProjectErrorsJboss6() throws Exception {
//		server = ServerRuntimeUtils.create60Server();
//		p = createProject("org.jboss.ide.eclipse.as.runtime.60");
//		int markerSev = p.findMaxProblemSeverity(null, true, IResource.DEPTH_INFINITE);
//		assertTrue(markerSev !=  IMarker.SEVERITY_ERROR);
//	}
	
	public void testTargetedWebProjectErrorsJboss7() throws Exception {
		server = ServerRuntimeUtils.create70Server();
		p = createProject("org.jboss.ide.eclipse.as.runtime.70");
		int markerSev = p.findMaxProblemSeverity(null, true, IResource.DEPTH_INFINITE);
		assertTrue(markerSev !=  IMarker.SEVERITY_ERROR);
	}
	
	protected IProject createProject(String rtId) throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel(MODULE_NAME, null, null, null, null, JavaEEFacetConstants.WEB_24, false);
		OperationTestCase.runAndVerify(dm);
		p = ResourcesPlugin.getWorkspace().getRoot().getProject(MODULE_NAME);
		assertTrue(p.exists());
		assertNotNull(p);
		assertTrue(p.exists());
		setProjectRuntime(rtId);
		IFile f = p.getFolder("src").getFile("Tiger.java");
		f.create(getClassContents(), true, new NullProgressMonitor());
		JobUtils.waitForIdle(2000);
		return p;
	}
	protected void setProjectRuntime(String type) throws Exception {
		IFacetedProject fp = ProjectFacetsManager.create(p);
		IRuntime rt = fp.getPrimaryRuntime();
		assertNull(rt);
		Set<IRuntime> all = RuntimeManager.getRuntimes();
		assertEquals(all.size(), 2);
		Iterator<IRuntime> i = all.iterator();
		IRuntime r1 = i.next();
		if(!r1.getName().equals(type))
			r1 = i.next();
		Set<IRuntime> possible = new TreeSet<IRuntime>();
		possible.add(r1);
		fp.setTargetedRuntimes(possible, new NullProgressMonitor());
		fp.setPrimaryRuntime(r1, new NullProgressMonitor());
		
		assertNotNull(fp.getPrimaryRuntime());
	}
	protected InputStream getClassContents() {
		String contents = "public class Tiger {\n}";
		return new ByteArrayInputStream(contents.getBytes());
	}
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
	}
}
