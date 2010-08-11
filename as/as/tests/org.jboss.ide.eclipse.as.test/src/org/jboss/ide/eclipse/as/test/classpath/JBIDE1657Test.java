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
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ProjectRuntimeUtil;
import org.jboss.tools.jmx.core.test.util.TestProjectProvider;

/**
 * This test will test whether the old classpath container, 
 *   org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectRuntimeInitializer
 * which used to be automatically assigned to WTP projects, still works
 * and will not fail to resolve.
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 */
public class JBIDE1657Test extends TestCase {
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

	public void testJBIDE1657() {
		try {
			IJavaProject jp = JavaCore.create(project);
	
			// lets try a runtime
			IRuntime createdRuntime = ProjectRuntimeUtil.createRuntime("runtime1", 
					IJBossToolingConstants.AS_42, ASTest.JBOSS_AS_HOME);
			ProjectRuntimeUtil.setTargetRuntime(createdRuntime, project);
			IClasspathEntry[] raw1 = jp.getRawClasspath();
			IClasspathEntry[] resolved1 = jp.getResolvedClasspath(false);
			IClasspathEntry[] raw2 = new IClasspathEntry[raw1.length];
			for( int i = 0; i < raw1.length; i++ ) {
				if( !raw1[i].getPath().segment(0).equals("org.eclipse.jst.server.core.container")) {
					raw2[i]=raw1[i];
				} else {
					 IPath containerPath = new Path("org.jboss.ide.eclipse.as.classpath.core.runtime.ProjectRuntimeInitializer");
					 containerPath = containerPath.append("runtime1");
					 raw2[i] = JavaCore.newContainerEntry(containerPath);
				}
			}
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
}
