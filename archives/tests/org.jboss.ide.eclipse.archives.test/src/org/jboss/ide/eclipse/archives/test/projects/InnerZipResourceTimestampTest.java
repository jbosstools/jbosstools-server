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

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.test.util.ResourcesUtils;
import org.osgi.framework.Bundle;

import de.schlichtherle.io.File;

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
public class InnerZipResourceTimestampTest extends TestCase {
	private long startTime;
	private Bundle bundle;
	private final String projName = "InnerZipProj";
	private IProject project;
	IPath projLocation;
	private IFolder outputDir, libDir;
	protected void setUp() throws Exception {
		startTime = new Date().getTime();
		bundle = ArchivesTest.getDefault().getBundle();
		project = ResourcesUtils.createEclipseProject(projName, new NullProgressMonitor());
		projLocation = project.getLocation(); // for debugging
		assertTrue(project.exists());
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		outputDir = project.getFolder("output");
		outputDir.create(true, true, new NullProgressMonitor());
		assertTrue(outputDir.exists());
		libDir = project.getFolder("libs");
		libDir.create(true, true, new NullProgressMonitor());
		assertTrue(libDir.exists());
	}

	protected java.io.File findSomeJar() {
		try {
			URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
			IPath bundlePath = new Path(bundleURL.getFile());
			return bundlePath.append("libs").append("some.jar").toFile();
		} catch(IOException ioe){}
		return null;
	}

	
	protected void tearDown() throws Exception {
		ResourcesUtils.deleteProject(projName);
	}

	public void testInnerZipTimestamps() {
		// test the original
		java.io.File someJar = findSomeJar();
		File file = TrueZipUtil.getFile(new Path(someJar.getAbsolutePath()).append("META-INF").append("MANIFEST.MF"));
		long last = file.lastModified();
		assertTrue(last < startTime);
		
		try {
			boolean copyVal = TrueZipUtil.copyFile(someJar.getAbsolutePath(), libDir.getLocation().append("some.jar"));
			TrueZipUtil.umount();
			IPath workspaceJarPath = libDir.getLocation().append("some.jar");
			long workspaceJarLastModified = workspaceJarPath.toFile().lastModified();
			assertTrue(workspaceJarLastModified > startTime);
			File workspaceFile = TrueZipUtil.getFile(workspaceJarPath.append("META-INF").append("MANIFEST.MF"));
			long workspaceResourceMod = workspaceFile.lastModified();
			assertTrue(workspaceResourceMod < startTime);
		} catch(IOException ioe) {
		} finally {}
	}
}
