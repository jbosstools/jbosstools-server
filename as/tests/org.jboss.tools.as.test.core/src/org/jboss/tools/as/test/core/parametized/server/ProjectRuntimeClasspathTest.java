/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tools.as.test.core.parametized.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.validation.ValidationFramework;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ProjectRuntimeUtil;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProjectRuntimeClasspathTest extends TestCase {
	@Parameters
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParamterers());
	 }
	 
	private static final String PROJECT_ROOT_NAME = "basicwebproject";
	private static int count = 1;
	
	private IProject project;
	private IServer server;
	private String serverType;
	private String projectName;
	
	public ProjectRuntimeClasspathTest(String serverType) {
		this.serverType = serverType;
		this.projectName = PROJECT_ROOT_NAME + count;
		count++;
	}
	
	@Before
	public void setUp() throws Exception {
		ValidationFramework.getDefault().suspendAllValidation(true);
		IDataModel dm = CreateProjectOperationsUtility.getWebDataModel(projectName, 
				null, null, null, null, JavaEEFacetConstants.WEB_24, true);
		project = createSingleProject(dm, projectName);
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		server = ServerCreationTestUtils.createServerWithRuntime(serverType, getClass().getName() + serverType);
	}
	protected IProject createSingleProject(IDataModel dm, String name) throws Exception {
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourceUtils.findProject(name);
		if( p == null || !p.exists())
			fail();
		return p;
	}

	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
		ValidationFramework.getDefault().suspendAllValidation(false);
	}

	@Test
	public void testProjectRuntime() {
		assertNotNull(server);
		assertNotNull(server.getRuntime());
		
		try {
			IJavaProject jp = JavaCore.create(project);
			verifyInitialClasspathEntries(jp);
			
			ProjectRuntimeUtil.setTargetRuntime(server.getRuntime(), project);
			verifyPostRuntimeCPE(jp);
			
			ProjectRuntimeUtil.clearRuntime(project);
			verifyInitialClasspathEntries(jp);
			
		} catch( JavaModelException jme ) {
			jme.printStackTrace();
			fail(jme.getMessage());
		} catch( CoreException ce ) {
			ce.printStackTrace();
			fail(ce.getMessage());
		}
	}
	
	@Test
	public void testJBIDE1657EquivilentEntries() {
		try {
			IJavaProject jp = JavaCore.create(project);
	
			// lets try a runtime
			IRuntime createdRuntime = server.getRuntime();
			ProjectRuntimeUtil.setTargetRuntime(createdRuntime, project);
			IClasspathEntry[] raw1 = jp.getRawClasspath();
			IClasspathEntry[] resolved1 = jp.getResolvedClasspath(false);
			
			IClasspathEntry[] raw2 = cloneAndReplace(raw1, createdRuntime.getName());
			jp.setRawClasspath(raw2, new NullProgressMonitor());
			IClasspathEntry[] resolved2 = jp.getResolvedClasspath(false);
			assertEquals("New classpath container path should return the same classpath entries as the old. ", 
					resolved1.length , resolved2.length);
			assertTrue("Should be more than one classpath entry", resolved1.length > 0);
		} catch( CoreException ce ) {
			ce.printStackTrace();
			fail(ce.getMessage());
		}
	}

	/* Replace the jst.server.core.container entry with one from jbt. They should be 100% equivilent */
	private IClasspathEntry[] cloneAndReplace(IClasspathEntry[] raw1, String rtName) {
		IClasspathEntry[] raw2 = new IClasspathEntry[raw1.length];
		for( int i = 0; i < raw1.length; i++ ) {
			if( !raw1[i].getPath().segment(0).equals("org.eclipse.jst.server.core.container")) {
				raw2[i]=raw1[i];
			} else {
				 IPath containerPath = new Path("org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectRuntimeInitializer");
				 containerPath = containerPath.append(rtName);
				 raw2[i] = JavaCore.newContainerEntry(containerPath);
			}
		}
		return raw2;
	}
	
	protected void verifyPostRuntimeCPE(IJavaProject jp) throws CoreException {
		IClasspathEntry[] entries = jp.getRawClasspath();
		String[] required = new String[] { 
				"org.eclipse.jst.server.core.container",
				projectName, 
				"org.eclipse.jst.j2ee.internal.web.container",
				"org.eclipse.jdt.launching.JRE_CONTAINER"};
		assertTrue(entries.length >= required.length);
		jp.getResolvedClasspath(false); // make sure it can resolve all
		verifyClasspathEntries(entries, required);
	}

	protected void verifyInitialClasspathEntries(IJavaProject jp) throws CoreException {
		IClasspathEntry[] entries = jp.getRawClasspath();
		jp.getResolvedClasspath(false); // make sure it can resolve all
		String[] required = new String[] { 
				"org.eclipse.jst.j2ee.internal.web.container", projectName};
		verifyClasspathEntries(entries, required);
	}
	
	protected void verifyClasspathEntries(IClasspathEntry[] entries, String[] required) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < required.length; i++) {
			list.add(required[i]);
		}
		for( int i = 0; i < entries.length; i++ ) {
			if( list.contains(entries[i].getPath().segment(0)))
				list.remove(entries[i].getPath().segment(0));
		}
		
		if( list.size() > 0 ) {
			String tmp = "Required enties not found: ";
			for( int i = 0; i < list.size(); i++ ) {
				tmp += list.get(i) + ", ";
			}
			fail(tmp.substring(0, tmp.length() - 2));
		}
	}
}
