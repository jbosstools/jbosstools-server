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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.PublishModuleFullRunner;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.PublishModuleIncrementalRunner;
import org.jboss.tools.as.test.core.internal.utils.MockModule;
import org.jboss.tools.as.test.core.internal.utils.MockModuleUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This is a test of the basic functionality of the two publish runner classes.
 * This test verifies that full publish and incremental publish work properly
 * 
 * 
 * {@link PublishModuleFullRunner}
 * {@link PublishModuleIncrementalRunner}
 */
@RunWith(value = Parameterized.class)
public class PublishRunnerTest extends TestCase {
	
	@Parameters
	public static Collection<Object[]> params() {
		ArrayList<Object[]> ret = new ArrayList<Object[]>();
		ret.add(new Object[]{"C:\\some\\folder", new Character('\\')});
		ret.add(new Object[]{"/home/user/some/folder", new Character('/')});
		return ret;
	}

	private IPath rootPath;
	private IModule m;
	public PublishRunnerTest(String basedir, Character sep) {
		// client must use a RemotePath to ensure all oaths are appended
		// correctly for the case of a remote system
		rootPath = new RemotePath(basedir, sep);
	}
	

	@Test
	public void testSingleResourceRootFullPublish() throws Exception {
		TestController tc = new TestController();
		IModuleResource mf = new ModuleFile("wonka.txt", new Path("/"), System.currentTimeMillis());
		IModuleResource[] asArr = new IModuleResource[]{mf};
		PublishModuleFullRunner runner = new PublishModuleFullRunner(tc, rootPath);
		try {
			runner.fullPublish(asArr, null);
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertTrue(tc.mkdir.contains(rootPath));
		assertTrue(tc.copied.contains(rootPath.append("wonka.txt")));
	}

	@Test
	public void testSingleResourceDeepFullPublish() throws Exception {
		TestController tc = new TestController();
		IModuleResource mf = new ModuleFile("wonka.txt", new Path("some/inner/path"), System.currentTimeMillis());
		IModuleResource[] asArr = new IModuleResource[]{mf};
		PublishModuleFullRunner runner = new PublishModuleFullRunner(tc, rootPath);
		try {
			runner.fullPublish(asArr, null);
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertFileCopiedAndParentDirsMade(tc, rootPath, new Path("some/inner/path/wonka.txt"));
	}

	
	@Test
	public void testSingleMultipleDeepFullPublish() throws Exception {
		TestController tc = new TestController();
		IModuleResource mf = new ModuleFile("wonka.txt", new Path("some/inner/path"), System.currentTimeMillis());
		IModuleResource mf2 = new ModuleFile("wonka2.pdf", new Path("some/weird/path"), System.currentTimeMillis());
		IModuleResource[] asArr = new IModuleResource[]{mf,mf2};
		PublishModuleFullRunner runner = new PublishModuleFullRunner(tc, rootPath);
		try {
			runner.fullPublish(asArr, null);
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertFileCopiedAndParentDirsMade(tc, rootPath, new Path("some/inner/path/wonka.txt"));
		assertFileCopiedAndParentDirsMade(tc, rootPath, new Path("some/weird/path/wonka2.pdf"));
	}

	@Test
	public void testDeepFolderWithNestedFileFullPublish() throws Exception {
		TestController tc = new TestController();
		ModuleFolder mf = new ModuleFolder(null, "finalDeepFolder", new Path("some/inner/path"));
		IModuleResource mf2 = new ModuleFile("wonka2.pdf", new Path("some/inner/path/finalDeepFolder"), System.currentTimeMillis());
		mf.setMembers(new IModuleResource[]{mf2});
		IModuleResource[] asArr = new IModuleResource[]{mf};
		PublishModuleFullRunner runner = new PublishModuleFullRunner(tc, rootPath);
		try {
			runner.fullPublish(asArr, null);
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertFolderAndParentDirsMade(tc, rootPath, new Path("some/inner/path/finalDeepFolder"));
		assertFileCopiedAndParentDirsMade(tc, rootPath, new Path("some/inner/path/finalDeepFolder/wonka2.pdf"));
	}

	@Test
	public void testStandardModuleFullPublish() throws Exception {
		TestController tc = new TestController();
		IModule m = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)m.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] asArr = md.members();
		PublishModuleFullRunner runner = new PublishModuleFullRunner(tc, rootPath);
		try {
			runner.fullPublish(asArr, null);
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertFileCopiedAndParentDirsMade(tc, rootPath, getLeafs1());
	}

	
	@Test
	public void testStandardModuleFullPublishWithCancel() throws Exception {
		TestController tc = new TestController();
		tc.cancelAfterFirstAction = true;
		
		IModule m = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)m.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] asArr = md.members();
		PublishModuleFullRunner runner = new PublishModuleFullRunner(tc, rootPath);
		try {
			runner.fullPublish(asArr, null);
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertEquals(tc.numCalls(), 1);
	}

	
	@Test
	public void testStandardModuleIncrementalPublishNoChange() throws Exception {
		TestController tc = new TestController();
		IModule m = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)m.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] asArr = md.members();
		IPath[] leafs = getLeafs1();
		int kind[] = new int[leafs.length];
		for( int i = 0; i < leafs.length; i++ ) {
			kind[i] = IModuleResourceDelta.NO_CHANGE;
		}
		
		IModuleResourceDelta[] mockDelta = MockModuleUtil.createMockResourceDeltas(Arrays.asList(asArr), leafs, kind);
		PublishModuleIncrementalRunner runner = new PublishModuleIncrementalRunner(tc, rootPath);
		try {
			runner.publish(mockDelta, new NullProgressMonitor());
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertTrue(tc.copied.size() == 0);
		assertTrue(tc.mkdir.size() == 0);
	}

	
	@Test
	public void testStandardModuleIncrementalPublishAllChanged() throws Exception {
		TestController tc = new TestController();
		IModule m = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)m.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] asArr = md.members();
		IPath[] leafs = getLeafs1();
		int kind[] = new int[leafs.length];
		for( int i = 0; i < leafs.length; i++ ) {
			kind[i] = IModuleResourceDelta.CHANGED;
		}
		
		IModuleResourceDelta[] mockDelta = MockModuleUtil.createMockResourceDeltas(Arrays.asList(asArr), leafs, kind);
		PublishModuleIncrementalRunner runner = new PublishModuleIncrementalRunner(tc, rootPath);
		try {
			runner.publish(mockDelta, new NullProgressMonitor());
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertFileCopiedAndParentDirsMade(tc, rootPath, getLeafs1());
	}

	@Test
	public void testIncrementalPublishWithCancel() throws Exception {
		TestController tc = new TestController();
		tc.cancelAfterFirstAction = true;
		IModule m = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)m.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
		IModuleResource[] asArr = md.members();
		IPath[] leafs = getLeafs1();
		int kind[] = new int[leafs.length];
		for( int i = 0; i < leafs.length; i++ ) {
			kind[i] = IModuleResourceDelta.CHANGED;
		}
		
		IModuleResourceDelta[] mockDelta = MockModuleUtil.createMockResourceDeltas(Arrays.asList(asArr), leafs, kind);
		PublishModuleIncrementalRunner runner = new PublishModuleIncrementalRunner(tc, rootPath);
		try {
			runner.publish(mockDelta, new NullProgressMonitor());
		} catch(CoreException ce) {
			ce.printStackTrace();
			throw ce;
		}
		assertEquals(tc.numCalls(), 1);
	}

	
	
	
	// A utility method that ensures all parent dirs were made and the file was copied
	private void assertFileCopiedAndParentDirsMade(TestController controller, IPath root, IPath[] relative) {
		for( int i = 0; i < relative.length;i++ ) {
			assertFileCopiedAndParentDirsMade(controller, root, relative[i]);
		}
	}
	
	// A utility method that ensures all parent dirs were made and the file was copied
	private void assertFileCopiedAndParentDirsMade(TestController controller, IPath root, IPath relative) {
		assertParentDirsMade(controller, root, relative);
		assertTrue(controller.copied.contains(root.append(relative)));
	}

	// A utility method that ensures all parent dirs were made and the folder was copied
	private void assertFolderAndParentDirsMade(TestController controller, IPath root, IPath relative) {
		assertParentDirsMade(controller, root, relative);
		assertTrue(controller.mkdir.contains(root.append(relative)));
	}

	// A utility method that ensures all parent dirs were made 
	private void assertParentDirsMade(TestController controller, IPath root, IPath relative) {
		IPath working = root;
		assertTrue(controller.mkdir.contains(working));
		for( int i = 0; i < relative.segmentCount() - 1; i++ ) {
			working = working.append(relative.segment(i));
			if( !controller.mkdir.contains(working)) {
				System.out.println("Test failing");
			}
			assertTrue(controller.mkdir.contains(working));
		}
	}

	private static class TestController extends AbstractSubsystemController implements IFilesystemController {

		private ArrayList<IPath> copied = new ArrayList<IPath>();
		private ArrayList<IPath> deleted = new ArrayList<IPath>();
		private ArrayList<IPath> checkedIsFile = new ArrayList<IPath>();
		private ArrayList<IPath> mkdir = new ArrayList<IPath>();
		private ArrayList<IPath> touch = new ArrayList<IPath>();
		
		private boolean canceled = false;
		private boolean cancelAfterFirstAction = false;
		private int callCount = 0;
				
		private int sumAllArrays() {
			return copied.size()+deleted.size() + checkedIsFile.size() + mkdir.size()+ touch.size();
		}

		private int numCalls() {
			return callCount;
		}

		private void checkCancel(IProgressMonitor monitor) throws CoreException {
			callCount++;
			if( canceled ) {
				throw new CoreException(new Status(IStatus.ERROR, null,
						"Monitor is already canceled. This should not be called"));
			}
			if( cancelAfterFirstAction ) {
				monitor.setCanceled(true);
				canceled= true;
			}
		}
		
		@Override
		public IStatus copyFile(File f, IPath path, IProgressMonitor monitor)
				throws CoreException {
			checkCancel(monitor);
			copied.add(path);
			return null;
		}

		@Override
		public IStatus deleteResource(IPath path, IProgressMonitor monitor)
				throws CoreException {
			checkCancel(monitor);
			deleted.add(path);
			return null;
		}

		@Override
		public boolean isFile(IPath path, IProgressMonitor monitor)
				throws CoreException {
			checkCancel(monitor);
			checkedIsFile.add(path);
			return false;
		}

		@Override
		public IStatus makeDirectoryIfRequired(IPath dir,
				IProgressMonitor monitor) throws CoreException {
			checkCancel(monitor);
			
			// This won't be called for every single path, so its up to the implementation to 
			// ensure that all parent folders are created. To mimic that, this mock impl
			// will add all such parent paths are made also.
			IPath working = dir;
			while(working.segmentCount() > 0 ) {
				if( !mkdir.contains(working)) 
					mkdir.add(working);
				working = working.removeLastSegments(1);
			}
			return null;
		}

		@Override
		public IStatus touchResource(IPath path, IProgressMonitor monitor) throws CoreException {
			checkCancel(monitor);
			touch.add(path);
			return null;
		}

		@Override
		public boolean exists(IPath path, IProgressMonitor monitor)
				throws CoreException {
			return false;
		}
	}	
	
	private IPath[] getLeafs1() {
		IPath[] leafs = new IPath[] {
				new Path("w"),
				new Path("x"),
				new Path("y"),
				new Path("z"),
				new Path("a/a1"),
				new Path("a/a2"),
				new Path("a/q1"),
				new Path("a/q2"),
				new Path("b/b1"),
				new Path("b/b2"),
				new Path("b/b3"),
				new Path("b/b4"),
				new Path("c/y1"),
				new Path("c/y2"),
				new Path("c/y3"),
				new Path("c/y4"),
				new Path("d/F/f1"),
				new Path("d/F/f2"),
				new Path("d/F/f3"),
				new Path("d/F/f4")
		};
		return leafs;
	}
	private IModule createTestMockModule() {
		// Create a custom mock project structure
		MockModule m = MockModuleUtil.createMockWebModule();
		IModuleResource[] all = MockModuleUtil.createMockResources(getLeafs1(), new IPath[0]);
		m.setMembers(all);
		return m;
	}
	
}
