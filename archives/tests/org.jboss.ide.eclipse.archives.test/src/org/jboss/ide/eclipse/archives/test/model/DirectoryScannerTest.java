package org.jboss.ide.eclipse.archives.test.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.ResourcesUtils;
import org.osgi.framework.Bundle;

public class DirectoryScannerTest extends TestCase {
	IProject proj = null;
	private Bundle bundle;
	private IPath bundlePath;
	protected void setUp() throws Exception {
		if( bundlePath == null ) {
			try {
				bundle = ArchivesTest.getDefault().getBundle();
				URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
				bundlePath = new Path(bundleURL.getFile());
			} catch( IOException ioe) {
				fail("Failed to set up " + getClass().getName());
			}
		}

		proj = ResourcesUtils.importProject("org.jboss.ide.eclipse.archives.test", "/inputs/projects/GenericProject");
		proj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		JobUtils.waitForIdle();
	}
	protected void tearDown() throws Exception {
		ResourcesUtils.deleteProject(proj.getName());
	}
		
	public void testSingleFileScanner() {
		DirectoryScannerExtension scanner1 = 
			DirectoryScannerFactory.createDirectoryScanner(
					proj.getLocation().append("Resources").append("images").toOSString(), null, 
					"multiple_files.gif", "", null, 
					false, 1.2, false);
		scanner1.scan();
		FileWrapper[] results = scanner1.getMatchedArray();
		assertTrue(results != null && results.length == 1);

		DirectoryScannerExtension scanner2 = 
			DirectoryScannerFactory.createDirectoryScanner(
					proj.getLocation().toOSString(), null, 
					"Resources/images/multiple_files.gif", "", null, 
					false, 1.2, false);
		scanner2.scan();
		FileWrapper[] results2 = scanner2.getMatchedArray();
		assertTrue(results2 != null && results2.length == 1);
	}
}
