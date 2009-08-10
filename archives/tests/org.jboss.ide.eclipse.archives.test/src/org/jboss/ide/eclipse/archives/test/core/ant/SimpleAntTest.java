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
package org.jboss.ide.eclipse.archives.test.core.ant;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.ide.eclipse.archives.test.core.ant.AntLauncher.IProcessListener;
import org.osgi.framework.Bundle;

public class SimpleAntTest extends TestCase implements IProcessListener {
	protected static final String ECLIPSE_LOC = "${UNIT_TEST_ECLIPSE_LOC}";
	protected static final String ARCHIVES_HOME = "${UNIT_TEST_ARCHIVES_HOME}";
	protected static final String PROJECT_NAME = "${UNIT_TEST_PROJECT_NAME}";
	protected static final String PROJECT_DIR = "${UNIT_TEST_PROJECT_DIR}";
	
	// if running from runtime workbench, set this to true. otherwise its running in a full build
	private final static boolean RUNTIME_WORKBENCH = false;

	// if running via runtime workbench, the archives core plugin should be exported somewhere
	// so that its jars can be picked up by the ant launch
	private final static String PLUGIN_LOCATION="/home/rob/tmp/plugins"; 
	private AntLauncher launcher;
	private IPath outputFolder;
	private boolean done = false;
	private boolean success = false;
	private String errorString = "";
	public void setUp() {
		try {
			launcher = new AntLauncher("AntTest", "build.xml", "run-packaging");
			launcher.listener = this;
			IPath templatePath = new Path("inputs").append("ant").append("antTemplate.xml");
			HashMap<String, String> map = new HashMap<String, String>();
			
			Bundle bundle = ArchivesTest.getDefault().getBundle();
			URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
			IPath bundlePath = new Path(bundleURL.getFile());
			IPath projectLoc = bundlePath.append(new Path("inputs").append("projects").append("SimpleAntTest"));
			outputFolder = projectLoc.append("output");
			
			String eclipseHome = new File(new URL(System.getProperty("eclipse.home.location")).toURI()).getAbsolutePath();
			
			map.put(ECLIPSE_LOC, eclipseHome);
			map.put(PROJECT_NAME, "SimpleAntTest");
			map.put(ARCHIVES_HOME, RUNTIME_WORKBENCH ? PLUGIN_LOCATION : eclipseHome);
			map.put(PROJECT_DIR, projectLoc.toOSString());
			launcher.createProjectData(templatePath, map);
		} catch( CoreException ce) {
			fail(ce.getMessage());
		} catch( IOException ioe) {
			fail(ioe.getMessage());
		} catch( URISyntaxException urise) {
			fail(urise.getMessage());
		}
	}
	
	public void tearDown() {
		try {
			launcher.deleteProject();
			File[] children = outputFolder.toFile().listFiles();
			if( children.length > 0 ) {
				for( int i = 0; i < children.length; i++ ) 
					children[i].delete();
			}
		} catch( CoreException ce ) {
		}
	}
	
	public void testOne() {
		try {
			assertTrue(outputFolder.toFile().exists());
			launcher.launch();
		} catch( CoreException ce) {
			fail(ce.getMessage());
		}
		
		int maxWait = 60*1000; // max wait 60s
		int waited = 0;
		while( !done && waited < maxWait ) {
			try {
				Thread.currentThread().sleep(500);
			} catch( InterruptedException ie) {}
			waited += 500;
		}
		
		if( !done || !success )
			fail("The ant task did not successfully complete. " + errorString);
		
		assertTrue(outputFolder.toFile().list().length == 1);
		File out = outputFolder.toFile().listFiles()[0];
		assertTrue(out.exists());
		
	}
	public void out(String text) {
		if( "BUILD SUCCESSFUL\n".equals(text)) //$NON-NLS-1$
			done = success = true;
	}
	public void err(String text) {
		if( "BUILD FAILED\n".equals(text)) { //$NON-NLS-1$
			success = false;
			done = true;
		}
		errorString += text + "\n";
	}
}
