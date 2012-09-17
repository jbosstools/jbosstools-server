package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;
import java.util.ArrayList;

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
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.publishers.patterns.PublishFilterDirectoryScanner;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
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
		IDataModel dm = ProjectCreationUtil.getWebDataModel(name, null, null, 
				"myContent", null, JavaEEFacetConstants.WEB_25, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		IFile f = p.getFile(new Path("myContent/includeme.txt"));
		IOUtil.setContents(f, "includeMe");
		IFile f2 = p.getFile(new Path("myContent/DONTincludeme.txt"));
		IOUtil.setContents(f2, "leave me out");
		return p;
	}
	
	private IProject[] createProjectWithNested(String earName, String webName) throws Exception {
		IDataModel dm = ProjectCreationUtil.getEARDataModel(earName, "earContent", null, null, JavaEEFacetConstants.EAR_5, false);
		IDataModel dm2 = ProjectCreationUtil.getWebDataModel(webName, earName, null, "webContent", null, JavaEEFacetConstants.WEB_25, false);
		
		OperationTestCase.runAndVerify(dm);
		OperationTestCase.runAndVerify(dm2);
		
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(earName);
		IFile f = p.getFile(new Path("earContent/includeme.txt"));
		IOUtil.setContents(f, "includeMe");
		IFile f2 = p.getFile(new Path("earContent/DONTincludeme.txt"));
		IOUtil.setContents(f2, "leave me out");

		IProject p2 = ResourcesPlugin.getWorkspace().getRoot().getProject(webName);
		IFile f3 = p2.getFile(new Path("webContent/includeme.txt"));
		IOUtil.setContents(f3, "includeMe");
		IFile f4 = p2.getFile(new Path("webContent/DONTincludeme.txt"));
		IOUtil.setContents(f4, "leave me out");
		return new IProject[]{p, p2};
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
		
		// Test the actual publish
		MockPublishMethod.reset();
		server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
		ArrayList<IPath> changed = MockPublishMethod.changed;
		assertEquals(changed.size(),fCount+1);  // addition of 'root' 
	}

	public void testServerIntegration_NestedProject() throws CoreException, IOException, Exception {
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_AS_60, 
				"name1", "default");
		server = ServerRuntimeUtils.useMock2PublishMethod(server);
		IProject[] projects = createProjectWithNested("MyEar", "MyWeb");
		
		ServerPreferences.getInstance().setAutoPublishing(false);
		IModule earMod = ServerUtil.getModule(projects[0]);
		server = ServerRuntimeUtils.addModule(server, earMod);
		ModuleDelegate earDel = (ModuleDelegate)earMod.loadAdapter(ModuleDelegate.class, null);
		
		DelegatingServerBehavior beh = (DelegatingServerBehavior)server.loadAdapter(DelegatingServerBehavior.class, null);
		Mock2BehaviourDelegate del = (Mock2BehaviourDelegate)beh.getDelegate();
		del.setUseSuperclassBehaviour(true);
		IModulePathFilter earFilter = del.getPathFilter(new IModule[]{earMod});
		IModuleResource[] originalMembers = earDel.members();
		IModuleResource[] filteredMembers = earFilter == null ? originalMembers : earFilter.getFilteredMembers();
		int oCount = PublishFilterDirectoryScannerTest.countAllResources(originalMembers);
		int fCount = PublishFilterDirectoryScannerTest.countAllResources(filteredMembers);
		assertEquals(oCount, fCount);
		
		IVirtualComponent vc = ComponentCore.createComponent(earMod.getProject());

		vc.setMetaProperty("component.inclusion.patterns", "**/*");
		vc.setMetaProperty("component.exclusion.patterns", "**/*.txt");
		
		earFilter = del.getPathFilter(new IModule[]{earMod});
		originalMembers = earDel.members();
		filteredMembers = earFilter.getFilteredMembers();
		oCount = PublishFilterDirectoryScannerTest.countAllResources(originalMembers);
		fCount = PublishFilterDirectoryScannerTest.countAllResources(filteredMembers);
		assertEquals(oCount, fCount+2);

		vc.setMetaProperty("component.inclusion.patterns", "**/*");
		vc.setMetaProperty("component.exclusion.patterns", "**/DON*");
		
		earFilter = del.getPathFilter(new IModule[]{earMod});
		originalMembers = earDel.members();
		filteredMembers = earFilter.getFilteredMembers();
		oCount = PublishFilterDirectoryScannerTest.countAllResources(originalMembers);
		fCount = PublishFilterDirectoryScannerTest.countAllResources(filteredMembers);
		assertEquals(oCount, fCount+1);

		
		IModuleResource r = PublishFilterDirectoryScanner.findResource(originalMembers, null, 
				new Path("DONTincludeme.txt"));
		assertNotNull(r);
		assertFalse(earFilter.shouldInclude(r));
		r = PublishFilterDirectoryScanner.findResource(originalMembers, null, 
				new Path("includeme.txt"));
		assertNotNull(r);
		assertTrue(earFilter.shouldInclude(r));
		
		IModule webMod = ServerUtil.getModule(projects[1]);
		ModuleDelegate webDel = (ModuleDelegate)webMod.loadAdapter(ModuleDelegate.class, null);
		IModulePathFilter webFilter = del.getPathFilter(new IModule[]{webMod});
		IModuleResource[] webOriginalMembers = webDel.members();
		IModuleResource[] webFilteredMembers = webFilter == null ? webOriginalMembers : webFilter.getFilteredMembers();
		int webOCount = PublishFilterDirectoryScannerTest.countAllResources(webOriginalMembers);
		int webFCount = PublishFilterDirectoryScannerTest.countAllResources(webFilteredMembers);

		// Test the actual publish		
		MockPublishMethod.reset();
		server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
		ArrayList<IPath> changed = MockPublishMethod.changed;
		assertEquals(changed.size(),
				fCount +1 /* root folder */ +1 /* application.xml, touched by our logic*/
				+ webFCount + 1 /*  .war root */ 
				);  
		
		
		IVirtualComponent webVC= ComponentCore.createComponent(earMod.getProject());
		webVC.setMetaProperty("component.inclusion.patterns", "**/*");
		webVC.setMetaProperty("component.exclusion.patterns", "**/DON*");
		webFilteredMembers = webFilter == null ? webOriginalMembers : webFilter.getFilteredMembers();
		webOCount = PublishFilterDirectoryScannerTest.countAllResources(webOriginalMembers);
		webFCount = PublishFilterDirectoryScannerTest.countAllResources(webFilteredMembers);

		// Test the actual publish		
		MockPublishMethod.reset();
		server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
		changed = MockPublishMethod.changed;
		assertEquals(changed.size(),
				fCount +1 /* root folder */ +1 /* application.xml, touched by our logic*/
				+ webFCount + 1 /*  .war root */ 
				);  

	}

}