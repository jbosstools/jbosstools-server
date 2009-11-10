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
package org.jboss.ide.eclipse.as.test.classpath;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ProjectRuntimeUtil;
import org.jboss.tools.jmx.core.test.util.TestProjectProvider;

public class ProjectRuntimeTest extends TestCase {
	private TestProjectProvider provider;
	private IProject project;

	protected void setUp() throws Exception {
		provider = new TestProjectProvider("org.jboss.ide.eclipse.as.test", null, "basicwebproject", true); 
		project = provider.getProject();
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	protected void tearDown() throws Exception {
		provider.dispose();
	}

	public void testProjectRuntime() {
		try {
			IJavaProject jp = JavaCore.create(project);
			verifyInitialClasspathEntries(jp);
			
			// lets try a runtime
			IRuntime createdRuntime = ProjectRuntimeUtil.createRuntime("runtime1", ASTest.JBOSS_RUNTIME_42, ASTest.JBOSS_AS_HOME);
			ProjectRuntimeUtil.setTargetRuntime(createdRuntime, project);
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
	
	protected void verifyPostRuntimeCPE(IJavaProject jp) throws CoreException {
		IClasspathEntry[] entries = jp.getRawClasspath();
		assertEquals(4, entries.length);
		jp.getResolvedClasspath(false); // make sure it can resolve all
		String[] required = new String[] { 
				"org.eclipse.jst.server.core.container",
				"basicwebproject", 
				"org.eclipse.jst.j2ee.internal.web.container",
				"org.eclipse.jdt.launching.JRE_CONTAINER"};
		verifyClasspathEntries(entries, required);
	}

	protected void verifyInitialClasspathEntries(IJavaProject jp) throws CoreException {
		IClasspathEntry[] entries = jp.getRawClasspath();
		jp.getResolvedClasspath(false); // make sure it can resolve all
		String[] required = new String[] { 
				"org.eclipse.jst.j2ee.internal.web.container", "basicwebproject"};
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
