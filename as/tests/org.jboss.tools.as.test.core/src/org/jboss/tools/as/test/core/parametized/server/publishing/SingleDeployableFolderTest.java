package org.jboss.tools.as.test.core.parametized.server.publishing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.tools.as.test.core.internal.MockPublishMethod4;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.jboss.tools.test.util.JobUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class SingleDeployableFolderTest extends AbstractPublishingTest {
	public static final String PROJECT_ROOT_NAME = "SingleDeployableFolderTest";
	public static int count = 0;

	@Parameters
	public static Collection<Object[]> params() {
		ArrayList<Object[]> ret = defaultData();
		return ret;
	}

	private String projectName;
	public SingleDeployableFolderTest(String serverType, String zip,
			String deployLoc, String perMod) {
		super(serverType, zip, deployLoc, perMod);
	}
	
	@Override
	protected void createProjects() throws Exception {
		projectName = PROJECT_ROOT_NAME + count;
		count++;

		IDataModel dm = CreateProjectOperationsUtility.getEARDataModel(projectName, "earContent", null, null, JavaEEFacetConstants.EAR_5, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertTrue(p.exists());
		IFolder folder1 = p.getFolder("folder1");
		folder1.create(false, true, new NullProgressMonitor());
		ResourceUtils.createFile(p, "folder1/file1.txt", "file1 edit1");
		ResourceUtils.createFile(p, "folder1/file2.txt", "file2 edit1");
		IModule[] mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 0);
		SingleDeployableFactory.makeDeployable(folder1);
		mods = SingleDeployableFactory.getFactory().getModules();
		assertEquals(mods.length, 1);
		addModuleToServer(mods[0]);
	}
	
	@Test
	public void testSingleDeployableFolder() throws IOException, CoreException {
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String file1Path = "folder1/file1.txt";
		String file2Path = "folder1/file2.txt";
		IPath f1Path = new Path(file1Path);
		IPath f2Path = new Path(file2Path);
		
		// Initial
		fullPublishAndVerify1("file1 edit1", "file2 edit1");
		
		
		/* Add incremental */
		ResourceUtils.setContents(p, f1Path, "file1 edit2");
		JobUtils.waitForIdle();
		incrementalOneFileChangedPublishAndVerify1(f1Path, "file1 edit2");
		
		
		ResourceUtils.setContents(p, f2Path, "file2 edit2");
		JobUtils.waitForIdle();
		incrementalOneFileChangedPublishAndVerify1(f2Path, "file2 edit2");

		// Change 2 files
		String f1e3Cont = "file1 edit3";
		String f2e3Cont = "file2 edit3";
		ResourceUtils.setContents(p, f1Path, f1e3Cont);
		ResourceUtils.setContents(p, f2Path, f2e3Cont);
		incrementalFilesChangedPublishAndVerify1(new IPath[] { f1Path, f2Path}, new String[] { f1e3Cont, f2e3Cont});

		
		// Incremental Addition
		String f3Loc = "folder1/file3.txt";
		IPath f3Path = new Path(f3Loc);
		ResourceUtils.createFile(p, f3Loc, "file3 added");
		incrementalFilesChangedPublishAndVerify1(new IPath[] { f3Path}, new String[] { "file3 added"});
		
		// Incremental Removal
		ResourceUtils.createFile(p, f3Loc, "file3 added");
		incrementalFilesChangedPublishAndVerify1(new IPath[] { f3Path}, new String[] { "file3 added"});
		
		IFile f3 = p.getFile(f3Path);
		f3.delete(false, null);
		incrementalFilesRemovedPublishAndVerify1(f3Path);
	}
	
	private void incrementalOneFileChangedPublishAndVerify1(IPath filePath, String fileContents) throws CoreException, IOException  {
		incrementalFilesChangedPublishAndVerify1(new IPath[] { filePath }, new String[] { fileContents });
	}

	private void fullPublishAndVerify1(String file1Contents, String file2Contents) throws CoreException, IOException  {
		publishAndCheckError(server,IServer.PUBLISH_FULL);
		// initial full publish should be 3 changes, 1 remove, , and 2 temp files (or 1,0,1 for zip)
		int[] vals = isZipped() ? new int[] { 1,0,1} : new int[] {3,1,2};
		vals[0] += getFullPublishChangedResourceCountModifier();
		vals[1] += getFullPublishRemovedResourceCountModifier();
		vals[2] += getFullPublishChangedResourceCountModifier();
		verifyPublishMethodResults(vals[0], vals[1], vals[2]);
	}

	private void incrementalFilesChangedPublishAndVerify1(IPath[] filePath, String[] fileContents) throws CoreException, IOException  {
		publishAndCheckError(server, IServer.PUBLISH_INCREMENTAL);
		int[] vals = isZipped() ? new int[] { 1,0,1} : new int[] {filePath.length,0,filePath.length};
		vals[0] += isZipped() ? getFullPublishChangedResourceCountModifier() : 1;
		vals[1] += getFullPublishRemovedResourceCountModifier();
		vals[2] += isZipped() ? getFullPublishChangedResourceCountModifier() : 0;
		verifyPublishMethodResults(vals[0], vals[1], vals[2]);
		
		IModuleFile[] mf = MockPublishMethod4.getChangedFiles();
		if (isZipped()) { 
			verifyZipContents(mf, filePath, fileContents);
		} else { 
			verifyWorkspaceContents(mf, filePath, fileContents);
		}
	}
	private void incrementalFilesRemovedPublishAndVerify1(IPath filePath) throws CoreException, IOException  {
		publishAndCheckError(server, IServer.PUBLISH_INCREMENTAL);
		IPath[] changed = MockPublishMethod4.getChanged();
		IPath[] removed = MockPublishMethod4.getRemoved();
		IPath[] temp = MockPublishMethod4.getTempPaths();
		//System.out.println("   " + changed.length + ", " + removed.length + ", " + temp.length);
		if( !isZipped() ) {
			boolean foundRemoved = false;
			for( int i = 0; i < removed.length; i++ ) {
				if( removed[i].lastSegment().equals(filePath.lastSegment()))
					foundRemoved = true;
			}
			assertTrue("Incremental removal has failed", foundRemoved);
		} else {
			File foundZip = findZip(MockPublishMethod4.getChangedFiles());
			de.schlichtherle.io.File zipRoot = new de.schlichtherle.io.File(foundZip, new TrueZipUtil.JarArchiveDetector());
			de.schlichtherle.io.File removed2 = new de.schlichtherle.io.File(zipRoot, filePath.removeFirstSegments(1).toString());
			assertFalse(removed2.exists());
		}
	}

}
