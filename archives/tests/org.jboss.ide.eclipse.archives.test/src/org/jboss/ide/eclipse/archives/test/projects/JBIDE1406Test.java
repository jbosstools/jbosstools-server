/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.test.projects;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.test.util.ResourcesUtils;

/**
 * This class tests first and foremost
 * the presence of a ${archives_current_project}
 * extension to allow the currently building
 * project to be agnostic
 *
 * During this JIRA, workspace paths became conscious of
 * their absolute / relative status and are now interpreted
 * differently according to their status.
 * @author rob
 *
 */
public class JBIDE1406Test extends TestCase {
	private IProject project;
	private IPath outputDir;
	private IPath propsFile;
	protected void setUp() throws Exception {
		project = ResourcesUtils.importProject(ArchivesTest.PLUGIN_ID,
				"inputs" + Path.SEPARATOR + "projects" + Path.SEPARATOR + "JBIDE1406");
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		outputDir = project.getLocation().append("output").append("JBIDE1406.jar");
		propsFile = outputDir.append("src").append("in.properties");
	}

	protected void tearDown() throws Exception {
		ResourcesUtils.deleteProject("JBIDE1406");
	}

	public void testJBIDE1406() {
		ArchiveBuildDelegate delegate = new ArchiveBuildDelegate();
		try {
			delegate.fullProjectBuild(project.getLocation(), new NullProgressMonitor());
			assertTrue(outputDir.toFile().isDirectory());
			assertTrue(propsFile.toFile().exists());
			assertTrue(propsFile.toFile().isFile());
		} catch( AssertionFailedError afe) {
			throw afe;
		} catch( RuntimeException re ) {
			fail(re.getMessage());
		}
	}

	/*
	 * Time to test that this commit has not ruined other things.
	 * Specifically, with an older archives descriptor
	 * all workspace paths should still be interpreted as
	 * absolute paths even if they're not visibly absolute.
	 */

	public void testJBIDE1406_descriptor_path_utils() {
		// These 3 should work regardless of version.
		// they are "", ".", and anything absolute "/proj/out"
		assertEquals(new Path("JBIDE1406").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation("", "JBIDE1406", true, 1.0));
		assertEquals(new Path("JBIDE1406").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation(".", "JBIDE1406", true, 1.0));
		assertEquals(new Path("JBIDE1406").append("output").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation("/JBIDE1406/output", "JBIDE1406", true, 1.0));

		// Test 1.2
		assertEquals(new Path("JBIDE1406").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation("", "JBIDE1406", true, 1.2));
		assertEquals(new Path("JBIDE1406").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation(".", "JBIDE1406", true, 1.2));
		assertEquals(new Path("JBIDE1406").append("output").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation("/JBIDE1406/output", "JBIDE1406", true, 1.2));


		// in 1.0, a leading slash does not matter
		assertEquals(new Path("JBIDE1406").append("output").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation("JBIDE1406/output", "JBIDE1406", true, 1.0));

		assertEquals(
				PathUtils.getAbsoluteLocation("JBIDE1406/output", "JBIDE1406", true, 1.0),
				PathUtils.getAbsoluteLocation("/JBIDE1406/output", "JBIDE1406", true, 1.0));

		// In 1.2 the leading slash matters
		assertEquals(new Path("JBIDE1406").append("output").makeAbsolute().toString(),
				PathUtils.getAbsoluteLocation("output", "JBIDE1406", true, 1.2));
		assertNotSame(
				PathUtils.getAbsoluteLocation("JBIDE1406/output", "JBIDE1406", true, 1.2),
				PathUtils.getAbsoluteLocation("/JBIDE1406/output", "JBIDE1406", true, 1.2));
	}
}
