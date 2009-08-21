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

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.test.util.ResourcesUtils;

public class JBIDE2296Test extends TestCase {
	private IProject project;

	protected void setUp() throws Exception {
		project = ResourcesUtils.importProject(ArchivesTest.PLUGIN_ID,
				"inputs" + Path.SEPARATOR + "projects" + Path.SEPARATOR + "JBIDE2296");
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	protected void tearDown() throws Exception {
		ResourcesUtils.deleteProject("JBIDE2296");
	}

	public void testJBIDE2296() {
		IPath p = project.getLocation().append("output");
		File pFile = p.toFile();
		assertTrue(pFile.toString() + " does not exist", pFile.exists());
		assertFalse(pFile.isFile());
		assertTrue(pFile.isDirectory());
	}
}
