/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.LocalZippedModulePublishRunner;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.MockModule;
import org.jboss.tools.as.test.core.internal.utils.MockModuleUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.schlichtherle.io.ArchiveDetector;

/**
 * This is a test of the basic functionality of the zipped publish runner class
 * This test verifies that full publish and incremental publish work properly
 * 
 * {@link LocalZippedModulePublishRunner}
 */
@RunWith(value = Parameterized.class)
public class ZippedPublishRunnerTest extends TestCase {
	private static IProject dummyProject;
	
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(new Object[]{IJBossToolingConstants.DEPLOY_ONLY_SERVER});
	}
	 
	public ZippedPublishRunnerTest(String serverType) {
		this.serverType = serverType;
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		dummyProject = ResourcesPlugin.getWorkspace().getRoot().getProject("Blah");
		dummyProject.create(new NullProgressMonitor());
		dummyProject.open(new NullProgressMonitor());
	}
	
	@Before
	public void setUp() throws Exception {
		try {
			server = ServerCreationTestUtils.createServerWithRuntime(serverType, getClass().getName() + serverType);
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
	}

	@After
	public void tearDown() throws Exception {
		JobUtils.waitForIdle(100);
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
		ASMatrixTests.clearStateLocation();
		JobUtils.waitForIdle();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		ASMatrixTests.cleanup();
	}

	
	@Test
	public void testSimpleWebModuleFull() throws Exception {
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("out.war");
		IModule web = createTestWebModule(1, false);
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, web, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
	}

	@Test
	public void testSimpleWebModuleFullDirExists() throws Exception {
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("out.war");
		dest.toFile().mkdirs(); // make a folder where an output file should be. 
		IModule web = createTestWebModule(1, false);
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, web, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
		
		//even on incremental, a full should be done
		FileUtil.safeDelete(dest.toFile());
		dest.toFile().mkdirs();
		runner.incrementalPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
	}
	
	
	@Test
	public void testSimpleWebModuleIncremental() throws Exception {
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("out.war");
		IModule web = createTestWebModule(1, false);
				
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, web, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
		
		// verify contents say v1
		IPath unzipFolder = ASMatrixTests.getDefault().getStateLocation().append("unzip");
		unzipFolder.toFile().mkdirs();
		IOUtil.unzipFile(dest, unzipFolder);
		String whtmlContents = IOUtil.getContents(unzipFolder.append("w.html").toFile());
		assertTrue(whtmlContents.equals("version1"));
		
		FileUtil.completeDelete(unzipFolder.toFile());
		
		web = createTestWebModule(2, false);
		final IModule web2 = web;
		runner = new LocalZippedModulePublishRunner(server, web, dest, null){
			// overriding this method because otherwise it requires integration tests
			// use a delta of only 1 file
			protected IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
				try {
					IModuleResource[] allResources = MockModuleUtil.getAllResources(web2);
					IModuleResourceDelta[] delta = MockModuleUtil.createMockResourceDeltas(Arrays.asList(allResources), 
							new IPath[]{new Path("w.html")}, new int[]{IModuleResourceDelta.CHANGED});
					return delta;
				} catch(Exception ce) {
					return new IModuleResourceDelta[]{};
				}
			}
		};
		runner.incrementalPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
		
		// verify contents say v1
		unzipFolder = ASMatrixTests.getDefault().getStateLocation().append("unzip");
		unzipFolder.toFile().mkdirs();
		IOUtil.unzipFile(dest, unzipFolder);
		whtmlContents = IOUtil.getContents(unzipFolder.append("w.html").toFile());
		assertTrue(whtmlContents.equals("version2"));
		String xhtmlContents = IOUtil.getContents(unzipFolder.append("x.html").toFile());
		assertEquals(xhtmlContents,"version1");
	}

	
	
	@Test
	public void testSimpleWebModuleIncrementalFileRemoved() throws Exception {
		// Remove a file during an incremental publish
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("out.war");
		IModule web = createTestWebModule(1, false);
				
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, web, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
		
		// verify contents say v1
		IPath unzipFolder = ASMatrixTests.getDefault().getStateLocation().append("unzip");
		unzipFolder.toFile().mkdirs();
		IOUtil.unzipFile(dest, unzipFolder);
		String whtmlContents = IOUtil.getContents(unzipFolder.append("w.html").toFile());
		assertTrue(whtmlContents.equals("version1"));
		
		FileUtil.completeDelete(unzipFolder.toFile());
		
		runner = testRunnerForDelta(web, dest, new IPath[]{new Path("w.html")}, new int[]{IModuleResourceDelta.REMOVED}); 
		
		runner.incrementalPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(true)), true);
		
		// verify w.html doens't exist, and x.html does. 
		unzipFolder = ASMatrixTests.getDefault().getStateLocation().append("unzip");
		unzipFolder.toFile().mkdirs();
		IOUtil.unzipFile(dest, unzipFolder);
		assertFalse(unzipFolder.append("w.html").toFile().exists());
		String xhtmlContents = IOUtil.getContents(unzipFolder.append("x.html").toFile());
		assertEquals(xhtmlContents,"version1");
		assertTrue(unzipFolder.append("d/F/f1.html").toFile().exists());
		FileUtil.completeDelete(unzipFolder.toFile());
		
		// now remove the deep file too
		runner = testRunnerForDelta(web, dest, new IPath[]{new Path("d/F/f1.html")}, new int[]{IModuleResourceDelta.REMOVED});
		
		runner.incrementalPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(true)), true);
		
		// verify w.html doens't exist, and x.html does. 
		unzipFolder = ASMatrixTests.getDefault().getStateLocation().append("unzip");
		unzipFolder.toFile().mkdirs();
		IOUtil.unzipFile(dest, unzipFolder);
		assertFalse(unzipFolder.append("w.html").toFile().exists());
		assertFalse(unzipFolder.append("d/F/f1.html").toFile().exists());
		FileUtil.completeDelete(unzipFolder.toFile());
		
	}

	private LocalZippedModulePublishRunner testRunnerForDelta(IModule mod, IPath dest, final IPath[] paths, final int[] kind) {
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, mod, dest, null){
			// overriding this method because otherwise it requires integration tests
			// use a delta of only 1 file
			protected IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
				try {
					IModuleResource[] allResources = MockModuleUtil.getAllResources(module[module.length-1]);
					IModuleResourceDelta[] delta = MockModuleUtil.createMockResourceDeltas(Arrays.asList(allResources), 
							paths, kind);
					return delta;
				} catch(Exception ce) {
					return new IModuleResourceDelta[]{};
				}
			}
		};
		return runner;
	}
	
	@Test
	public void testSimpleWebModuleFullTwice() throws Exception {
		// Test 2 publishes to verify the contents of the files actually change
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("out.war");
		IModule web = createTestWebModule(1, false);
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, web, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
		
		
		web = createTestWebModule(2, false);
		runner = new LocalZippedModulePublishRunner(server, web, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
		
		IPath unzipFolder = ASMatrixTests.getDefault().getStateLocation().append("unzip");
		unzipFolder.toFile().mkdirs();
		IOUtil.unzipFile(dest, unzipFolder);
		String whtmlContents = IOUtil.getContents(unzipFolder.append("w.html").toFile());
		assertTrue(whtmlContents.equals("version2"));
	}

	
	@Test
	public void testSimpleWebModuleIncrementalDNE() throws Exception {
		// Test the situation where an incremental publish is called but the output archive does not already exist
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("out.war");
		IModule web = createTestWebModule(1, false);
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, web, dest, null);
		runner.incrementalPublishModule(new NullProgressMonitor());
		verifyList(dest, Arrays.asList(getWebLeafs1(false)), true);
	}

	
	@Test
	public void testWebInEarModuleFull() throws Exception {
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("MyEar.ear");
		IModule ear = createTestWebInEarModule(1, false);
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, ear, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		// verify all ear files are there
		verifyList(dest, Arrays.asList(getEarLeafs1(false)), true);
		// verify all web files are there
		IPath webDest = dest.append("nested/inside/WebProj.war");
		verifyList(webDest, Arrays.asList(getWebLeafs1(false)), true);
	}


	@Test
	public void testWebInEarModuleRemoveWebFull() throws Exception {
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("MyEar.ear");
		IModule ear = createTestWebInEarModule(1, false);
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, ear, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		// verify all ear files are there
		verifyList(dest, Arrays.asList(getEarLeafs1(false)), true);
		// verify all web files are there
		IPath webDest = dest.append("nested/inside/WebProj.war");
		verifyList(webDest, Arrays.asList(getWebLeafs1(false)), true);
		
		//remove web as child
		((MockModule)ear).clearChildren();
		
		runner = new LocalZippedModulePublishRunner(server, ear, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		// verify all ear files are there
		verifyList(dest, Arrays.asList(getEarLeafs1(false)), true);
		// verify all web files are NOT there
		verifyList(webDest, Arrays.asList(getWebLeafs1(false)), false);
	}

	
	@Test
	public void testEarModuleWebAddedFull() throws Exception {
		// test 1 full publish with ear,  then one full publish after child module is added
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("MyEar.ear");
		IModule ear = createTestWebInEarModule(1, false);
		((MockModule)ear).clearChildren();
		
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, ear, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		// verify all ear files are there
		verifyList(dest, Arrays.asList(getEarLeafs1(false)), true);
		// verify NO web files are there
		IPath webDest = dest.append("nested/inside/WebProj.war");
		verifyList(webDest, Arrays.asList(getWebLeafs1(false)), false);
		
		//add web as child
		ear = createTestWebInEarModule(1, false);
		
		runner = new LocalZippedModulePublishRunner(server, ear, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		// verify all ear files are there
		verifyList(dest, Arrays.asList(getEarLeafs1(false)), true);
		// verify all web files are there
		verifyList(webDest, Arrays.asList(getWebLeafs1(false)), true);
	}

	
	@Test
	public void testWebInEarModuleRemoveSomeFilesFull() throws Exception {
		IPath dest = ASMatrixTests.getDefault().getStateLocation().append("testDeploy").append("MyEar.ear");
		IModule ear = createTestWebInEarModule(1, false);
		LocalZippedModulePublishRunner runner = new LocalZippedModulePublishRunner(server, ear, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		// verify all ear files are there
		verifyList(dest, Arrays.asList(getEarLeafs1(false)), true);
		// verify all web files are there
		IPath webDest = dest.append("nested/inside/WebProj.war");
		verifyList(webDest, Arrays.asList(getWebLeafs1(false)), true);
		
		
		// re-create the module with fewer items
		ear = createTestWebInEarModule(1, true);
		runner = new LocalZippedModulePublishRunner(server, ear, dest, null);
		runner.fullPublishModule(new NullProgressMonitor());
		// verify all ear files are there
		verifyList(dest, Arrays.asList(getEarLeafs1(true)), true);
		verifyList(webDest, Arrays.asList(getWebLeafs1(true)), true);
		
		// Verify the files that were removed are not in the end archive
		IPath[] earFilesDeleted = getMissing(getEarLeafs1(false), getEarLeafs1(true));
		IPath[] webFilesDeleted = getMissing(getWebLeafs1(false), getWebLeafs1(true));
		verifyList(dest, Arrays.asList(earFilesDeleted), false);
		verifyList(webDest, Arrays.asList(webFilesDeleted), false);
	}

	
	
	// used to verify the files exist
	protected void verifyList(IPath root, List<IPath> listOfRelativePaths, boolean exists) {
		Iterator<IPath> iterator = listOfRelativePaths.iterator();
		ArchiveDetector detector = TrueZipUtil.getJarArchiveDetector();
		while(iterator.hasNext()) {
			de.schlichtherle.io.File f = new de.schlichtherle.io.File(root.toFile(), detector);
			IPath nextRelative = iterator.next();
			de.schlichtherle.io.File toCheck = new de.schlichtherle.io.File(f, nextRelative.toString(), ArchiveDetector.DEFAULT);
			assertEquals("File " + toCheck.getAbsolutePath() + (exists ? " should " : " should not ") + "exist", toCheck.exists(), exists);
		}
	}	

	// Get the paths that are in 'all' but NOT in 'subset'
	private IPath[] getMissing(IPath[] all, IPath[] subset) {
		ArrayList<IPath> c = new ArrayList<IPath>();
		c.addAll(Arrays.asList(all));
		c.removeAll(Arrays.asList(subset));
		return (IPath[]) c.toArray(new IPath[c.size()]);
	}
	
	private IPath[] getEarLeafs1(boolean ignoreSome) {
		IPath[] possiblyRemoved =new IPath[]{
				new Path("g.html"),
				new Path("h/q1.html"),
				new Path("i/b1.html"),
				new Path("j/F/f1.html"),
		};
		IPath[] most = new IPath[] {
				new Path("g.html"),
				new Path("h/a1.html"),
				new Path("i/b2.html"),
				new Path("j/y1.html"),
				new Path("k/F/f4.html")
		};
		ArrayList<IPath> ret = new ArrayList<IPath>();
		ret.addAll(Arrays.asList(most));
		if( !ignoreSome) {
			ret.addAll(Arrays.asList(possiblyRemoved));
		}
		return (IPath[]) ret.toArray(new IPath[ret.size()]);
	}
	
	private IPath[] getWebLeafs1(boolean ignoreSome) {
		IPath[] possiblyRemoved =new IPath[]{
				new Path("w.html"),
				new Path("a/q1.html"),
				new Path("b/b1.html"),
				new Path("d/F/f1.html"),
		};
		IPath[] most = new IPath[] {
				new Path("x.html"),
				new Path("y.html"),
				new Path("z.html"),
				new Path("a/a1.html"),
				new Path("a/a2.html"),
				new Path("a/q2.html"),
				new Path("b/b2.html"),
				new Path("b/b3.html"),
				new Path("b/b4.html"),
				new Path("c/y1.html"),
				new Path("c/y2.html"),
				new Path("c/y3.html"),
				new Path("c/y4.html"),
				new Path("d/F/f2.html"),
				new Path("d/F/f3.html"),
				new Path("d/F/f4.html")
		};
		ArrayList<IPath> ret = new ArrayList<IPath>();
		ret.addAll(Arrays.asList(most));
		if( !ignoreSome) {
			ret.addAll(Arrays.asList(possiblyRemoved));
		}
		return (IPath[]) ret.toArray(new IPath[ret.size()]);
	}
	
	private IModule createTestWebModule(int version, boolean ignoreSome) throws Exception {
		//First create a temporary file somewhere in metadata to use as our underlying
		// Otherwise publish will fail
		IPath underlying = ASMatrixTests.getDefault().getStateLocation().append("underlying.txt");
		underlying.toFile().getParentFile().mkdirs();
		IOUtil.setContents(underlying.toFile(), "version" + version);
		
		// Create a custom mock project structure
		MockModule m = MockModuleUtil.createMockWebModule();
		IModuleResource[] all = MockModuleUtil.createMockResources(getWebLeafs1(ignoreSome), new IPath[0], underlying.toFile());
		m.setMembers(all);
		return m;
	}
	
	
	private IModule createTestWebInEarModule(int version, boolean ignoreSome) throws Exception {
		//First create a temporary file somewhere in metadata to use as our underlying
		// Otherwise publish will fail
		IPath underlying = ASMatrixTests.getDefault().getStateLocation().append("underlying.txt");
		underlying.toFile().getParentFile().mkdirs();
		IOUtil.setContents(underlying.toFile(), "version" + version);
		
		
		// Create a custom mock project structure
		MockModule web = MockModuleUtil.createMockWebModule();
		IModuleResource[] webAll = MockModuleUtil.createMockResources(getWebLeafs1(ignoreSome), new IPath[0], underlying.toFile());
		web.setMembers(webAll);
		
		MockModule ear = MockModuleUtil.createMockEarModule();
		IModuleResource[] earAll = MockModuleUtil.createMockResources(getEarLeafs1(ignoreSome), new IPath[0], underlying.toFile());
		ear.setMembers(earAll);
		String webWithSuffix = web.getName() + ".war";
		String uri = "nested/inside/" + webWithSuffix;
		((MockModule)ear).addChildModule(web, uri);
		((MockModule)ear).setProject(dummyProject);
		return ear;
	}

	
}
