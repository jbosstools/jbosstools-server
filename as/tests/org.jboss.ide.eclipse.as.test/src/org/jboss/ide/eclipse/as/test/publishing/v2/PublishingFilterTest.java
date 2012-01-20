package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.publishers.patterns.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.publishers.patterns.PublishFilterDirectoryScanner;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

/**
 *  Test the integration between components, filters, and servers
 */
public class PublishingFilterTest extends TestCase {
	public void tearDown() throws Exception {
		ServerRuntimeUtils.deleteAllServers();
		ServerRuntimeUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASTest.clearStateLocation();
	}

	public void testPublishingFilter() throws Exception {
		publishingFilterTest(createProject("module1"), "**/*", "**/DONTincludeme.txt", 1);
	}
	public void testPublishingFilter2() throws Exception {
		publishingFilterTest(createProject("module1"), "**/*", "**/*.txt", 2);
	}
	public void testPublishingFilterChange() throws Exception {
		// Make sure changes to an existing project also work
		IProject p = createProject("module1");
		publishingFilterTest(p, "**/*", "**/*.txt", 2);
		publishingFilterTest(p, "**/*", "**/DON*.txt", 1);
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
	private void publishingFilterTest(IProject p, String inc, String exc, int difference) throws Exception {
		IVirtualComponent vc = ComponentCore.createComponent(p);
		vc.setMetaProperty("component.inclusion.patterns", inc);
		vc.setMetaProperty("component.exclusion.patterns", exc);
		
		IModule module = ServerUtil.getModule(p);
		IModulePathFilter filter = ResourceModuleResourceUtil.findDefaultModuleFilter(module);
		assertNotNull(filter);
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, null);
		IModuleResource[] originalMembers = md.members();
		IModuleResource[] filteredMembers = filter.getFilteredMembers();
		
		int oCount = PublishFilterDirectoryScannerTest.countAllResources(originalMembers);
		int fCount = PublishFilterDirectoryScannerTest.countAllResources(filteredMembers);
		
		assertEquals(oCount, fCount + difference);
	}
	
	public void testServerIntegration() throws CoreException, IOException, Exception {
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_AS_60, 
				"name1", "default");
		server = ServerRuntimeUtils.useMock2PublishMethod(server);
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
		IModuleResource[] filteredMembers = filter.getFilteredMembers();
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
	}
}
