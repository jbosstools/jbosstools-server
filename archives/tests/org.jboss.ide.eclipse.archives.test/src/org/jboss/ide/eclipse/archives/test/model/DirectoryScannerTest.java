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
	
	public void testScanner() {
		DirectoryScannerExtension scanner1 = 
			DirectoryScannerFactory.createDirectoryScanner(
					proj.getLocation().toOSString(), null, 
					"**", "**/bin/**", null, 
					false, 1.2, false);
		DirectoryScannerExtension scanner2 = 
			DirectoryScannerFactory.createDirectoryScanner(
					proj.getLocation().toOSString(), null, 
					"**", "**/bin/**", null, 
					false, 1.2, false);

		// We have two exact scanners.
		// Make the first one scan, get the results
		scanner1.scan();
		FileWrapper[] results = scanner1.getMatchedArray();
		
		
		ArrayList<FileWrapper> iterated = new ArrayList<FileWrapper>();
		Iterator<File> i = scanner2.iterator();
		Object next;
		while( i.hasNext()) {
			next = i.next();
			assertTrue(next != null);
			assertTrue(next instanceof FileWrapper);
			iterated.add((FileWrapper)next);
		}
		
		assertEquals(results.length, iterated.size());
	}
}
