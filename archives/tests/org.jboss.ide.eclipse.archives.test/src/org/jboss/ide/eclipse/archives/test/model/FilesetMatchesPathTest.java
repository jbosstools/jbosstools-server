package org.jboss.ide.eclipse.archives.test.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveModelNode;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.tools.test.util.ResourcesUtils;


public class FilesetMatchesPathTest extends ModelTest {
	public void testAddFilesetToArchive() throws Exception {
		IProject proj = null;
		try {
			proj = ResourcesUtils.importProject("org.jboss.ide.eclipse.archives.test", "/inputs/projects/basicwebproject");
			proj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			XbPackages packs = new XbPackages();
			IArchiveModel model = new ArchivesModel();
			ArchiveModelNode node = new ArchiveModelNode(proj.getLocation(), packs, model);
			ModelUtil.fillArchiveModel(packs, node);
			
			IArchive archive = createArchive("someName.war", "test");
			IArchiveStandardFileSet fs = getFactory().createFileset();
			fs.setIncludesPattern("**");
			fs.setRawSourcePath("basicwebproject/WebContent");
			fs.setInWorkspace(true);
			archive.addChild(fs);
			node.addChild(archive);
		
			FileWrapper[] wrappers = fs.findMatchingPaths();
			assertNotNull(wrappers);
			assertTrue(wrappers.length == 2);
			IFile file = (IFile)proj.findMember("WebContent/WEB-INF/web.xml");
			assertNotNull(file);
			IPath path = file.getFullPath();
			IPath loc = file.getLocation();
			boolean matchesPath = fs.matchesPath(path, true);
			boolean matchesLoc = fs.matchesPath(loc, false);
			boolean matchesFSGarbage = fs.matchesPath(new Path("/home/rob/some.file.txt"), false);
			boolean matchesWSGarbage = fs.matchesPath(new Path("nonmatchingProject/file.txt"), true);
			assertTrue(matchesPath);
			assertTrue(matchesLoc);
			assertFalse(matchesFSGarbage);
			assertFalse(matchesWSGarbage);
		} finally {
			try {
				if( proj != null )
					proj.delete(true, true, null);
			} catch( CoreException ce ) {fail();}
		}
	}
}
