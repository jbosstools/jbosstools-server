package org.jboss.ide.eclipse.as.archives.integration.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.patterns.PublishFilterDirectoryScanner;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.publishing.v2.Mock2BehaviourDelegate;
import org.jboss.ide.eclipse.as.test.publishing.v2.MockPublishMethod;
import org.jboss.ide.eclipse.as.test.publishing.v2.PublishFilterDirectoryScannerTest;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

public class ZippedFilterTest extends TestCase {
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
	}
	
	private IProject createProject(String name) throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel("module1", null, null, 
				"myContent", null, JavaEEFacetConstants.WEB_25, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		IFile f = p.getFile(new Path("myContent/includeme.txt"));
		IOUtil.setContents(f, "includeMe");
		IFile f2 = p.getFile(new Path("myContent/DONTincludeme.txt"));
		IOUtil.setContents(f2, "leave me out");
		return p;
	}

	public void testServerIntegration() throws CoreException, IOException, Exception {
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_AS_60, 
				"name1", "default");
		server = ServerRuntimeUtils.useMock2PublishMethod(server);
		server = ServerRuntimeUtils.setZipped(server, true);
		IProject project = createProject("module1");
		MockPublishMethod.reset();
		
		ServerPreferences.getInstance().setAutoPublishing(false);
		IModule mod = ServerUtil.getModule(project);
		server = ServerRuntimeUtils.addModule(server, mod);
		ModuleDelegate md = (ModuleDelegate)mod.loadAdapter(ModuleDelegate.class, null);
		
		DelegatingServerBehavior beh = (DelegatingServerBehavior)server.loadAdapter(DelegatingServerBehavior.class, null);
		Mock2BehaviourDelegate del = (Mock2BehaviourDelegate)beh.getDelegate();
		del.setUseSuperclassBehaviour(true);
		IModulePathFilter filter = del.getPathFilter(new IModule[]{mod});
		IModuleResource[] originalMembers = md.members();
		IModuleResource[] filteredMembers = filter == null ? originalMembers : filter.getFilteredMembers();
		int oCount = PublishFilterDirectoryScannerTest.countAllResources(originalMembers);
		int fCount = PublishFilterDirectoryScannerTest.countAllResources(filteredMembers);
		assertEquals(oCount, fCount);
		
		IVirtualComponent vc = ComponentCore.createComponent(mod.getProject());

		vc.setMetaProperty("component.inclusion.patterns", "**/*");
		vc.setMetaProperty("component.exclusion.patterns", "**/*.txt");
		
		filter = del.getPathFilter(new IModule[]{mod});
		originalMembers = md.members();
		filteredMembers = filter.getFilteredMembers();
		oCount = PublishFilterDirectoryScannerTest.countAllResources(originalMembers);
		fCount = PublishFilterDirectoryScannerTest.countAllResources(filteredMembers);
		assertEquals(oCount, fCount+2);

		vc.setMetaProperty("component.inclusion.patterns", "**/*");
		vc.setMetaProperty("component.exclusion.patterns", "**/DON*");
		
		filter = del.getPathFilter(new IModule[]{mod});
		originalMembers = md.members();
		filteredMembers = filter.getFilteredMembers();
		oCount = PublishFilterDirectoryScannerTest.countAllResources(originalMembers);
		fCount = PublishFilterDirectoryScannerTest.countAllResources(filteredMembers);
		assertEquals(oCount, fCount+1);

		
		IModuleResource r = PublishFilterDirectoryScanner.findResource(originalMembers, null, 
				new Path("DONTincludeme.txt"));
		assertNotNull(r);
		assertFalse(filter.shouldInclude(r));
		r = PublishFilterDirectoryScanner.findResource(originalMembers, null, 
				new Path("includeme.txt"));
		assertNotNull(r);
		assertTrue(filter.shouldInclude(r));
		
		MockPublishMethod.reset();
		server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
	
		IModuleFile[] copied = MockPublishMethod.getChangedFiles();
		assertTrue(copied.length == 1);
		IModuleFile f1 = copied[0];
		File f2 = (File)f1.getAdapter(File.class);
		
		IPath unzip3 = ASTest.getDefault().getStateLocation().append("unzip3");
		IOUtil.unzipFile(new Path(f2.getAbsolutePath()),unzip3);
		assertEquals(IOUtil.countAllResources(unzip3.toFile()), fCount+1);

	}

}
