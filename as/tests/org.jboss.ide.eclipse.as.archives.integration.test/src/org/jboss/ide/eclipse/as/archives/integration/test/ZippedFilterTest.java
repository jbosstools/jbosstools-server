/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.patterns.internal.PublishFilterDirectoryScanner;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.MockPublishMethod4;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.jboss.tools.as.test.core.internal.utils.wtp.ProjectUtility;

public class ZippedFilterTest extends TestCase {
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServers();
		ServerCreationTestUtils.deleteAllRuntimes();
		ProjectUtility.deleteAllProjects();
		ASMatrixTests.clearStateLocation();
	}
	
	private IProject createProject(String name) throws Exception {
		IDataModel dm = CreateProjectOperationsUtility.getWebDataModel("module1", null, null, 
				"myContent", null, JavaEEFacetConstants.WEB_25, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		IFile f = p.getFile(new Path("myContent/includeme.txt"));
		IOUtil.setContents(f, "includeMe");
		IFile f2 = p.getFile(new Path("myContent/DONTincludeme.txt"));
		IOUtil.setContents(f2, "leave me out");
		return p;
	}

	protected void setMockPublishMethod4(IServerWorkingCopy wc) {
		MockPublishMethod4.reset();
		wc.setAttribute(IDeployableServer.SERVER_MODE, "mock4");
	}

	protected void setZipped(IServerWorkingCopy wc, boolean val) {
		ServerConverter.getDeployableServer(wc).setZipWTPDeployments(val);
	}
	
    public static IServer addModule(IServer server, IModule module) throws CoreException  {
        IServerWorkingCopy copy = server.createWorkingCopy();
        copy.modifyModules(new IModule[]{module}, new IModule[0], new NullProgressMonitor());
        return copy.save(false, new NullProgressMonitor());
    }

	public void testServerIntegration() throws CoreException, IOException, Exception {
		IServer server = ServerCreationTestUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_AS_60, "name1");
		IServerWorkingCopy wc = server.createWorkingCopy();
		setMockPublishMethod4(wc);
		setZipped(wc, true);
		server = wc.save(true,  null);
		IProject project = createProject("module1");
		MockPublishMethod4.reset();
		ServerPreferences.getInstance().setAutoPublishing(false);
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		IModule mod = ServerUtil.getModule(project);
		String depRoot = ds.getDeploymentLocation(new IModule[]{mod}, false).toString();
		MockPublishMethod4.setExpectedRoot(depRoot);

		server = addModule(server, mod);
		ModuleDelegate md = (ModuleDelegate)mod.loadAdapter(ModuleDelegate.class, null);
		
		DelegatingServerBehavior beh = (DelegatingServerBehavior)server.loadAdapter(DelegatingServerBehavior.class, null);
		IJBossBehaviourDelegate del = (IJBossBehaviourDelegate)beh.getDelegate();
//		del.setUseSuperclassBehaviour(true);
		IModulePathFilter filter = del.getPathFilter(new IModule[]{mod});
		IModuleResource[] originalMembers = md.members();
		IModuleResource[] filteredMembers = filter == null ? originalMembers : filter.getFilteredMembers();
		int oCount = IOUtil.countAllResources(originalMembers);
		int fCount = IOUtil.countAllResources(filteredMembers);
		assertEquals(oCount, fCount);
		
		IVirtualComponent vc = ComponentCore.createComponent(mod.getProject());

		vc.setMetaProperty("component.inclusion.patterns", "**/*");
		vc.setMetaProperty("component.exclusion.patterns", "**/*.txt");
		
		filter = del.getPathFilter(new IModule[]{mod});
		originalMembers = md.members();
		filteredMembers = filter.getFilteredMembers();
		oCount = IOUtil.countAllResources(originalMembers);
		fCount = IOUtil.countAllResources(filteredMembers);
		assertEquals(oCount, fCount+2);

		vc.setMetaProperty("component.inclusion.patterns", "**/*");
		vc.setMetaProperty("component.exclusion.patterns", "**/DON*");
		
		filter = del.getPathFilter(new IModule[]{mod});
		originalMembers = md.members();
		filteredMembers = filter.getFilteredMembers();
		oCount = IOUtil.countAllResources(originalMembers);
		fCount = IOUtil.countAllResources(filteredMembers);
		assertEquals(oCount, fCount+1);

		
		IModuleResource r = PublishFilterDirectoryScanner.findResource(originalMembers, null, 
				new Path("DONTincludeme.txt"));
		assertNotNull(r);
		assertFalse(filter.shouldInclude(r));
		r = PublishFilterDirectoryScanner.findResource(originalMembers, null, 
				new Path("includeme.txt"));
		assertNotNull(r);
		assertTrue(filter.shouldInclude(r));
		
		MockPublishMethod4.reset();
		MockPublishMethod4.setExpectedRoot(depRoot);
		server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
	
		IModuleFile[] copied = MockPublishMethod4.getChangedFiles();
		assertTrue(copied.length == 1);
		IModuleFile f1 = copied[0];
		File f2 = (File)f1.getAdapter(File.class);
		
		IPath unzip3 = ASMatrixTests.getDefault().getStateLocation().append("unzip3");
		IOUtil.unzipFile(new Path(f2.getAbsolutePath()),unzip3);
		assertEquals(IOUtil.countAllResources(unzip3.toFile()), fCount+1);

	}

}
