package org.jboss.ide.eclipse.archives.test.model;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.model.ArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.osgi.framework.Bundle;

public class ModelUtilTest extends TestCase {
	private Bundle bundle;
	private IPath bundlePath;
	private IPath outputs;
	private IPath inputs; 
	private IArchive rootArchive;
	protected void setUp() {
		if( bundlePath == null ) {
			try {
				bundle = ArchivesTest.getDefault().getBundle();
				URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
				bundlePath = new Path(bundleURL.getFile());
				outputs = bundlePath.append("output");
				inputs = bundlePath.append("inputs");
			} catch( IOException ioe) {}
		}
		try {
			rootArchive = createArchive();
		} catch( ArchivesModelException ame ) {
			fail(ame.getMessage());
		}
	}

	public void tearDown() {
		rootArchive = null;
	}
	
	
	
	protected IArchive createArchive() throws ArchivesModelException {
		IPath fileTrees = inputs.append("fileTrees");

		IArchive root = ArchiveNodeFactory.createArchive();
		root.setArchiveType("jar");
		root.setDestinationPath(outputs);
		root.setName("output.jar");
		root.setExploded(false);
		root.setInWorkspace(false);
		
		IArchiveFolder topFolder = ArchiveNodeFactory.createFolder();
		topFolder.setName("topFolder");
		root.addChild(topFolder);
		
		IArchiveFolder inner1 = ArchiveNodeFactory.createFolder();
		inner1.setName("inner1");
		topFolder.addChild(inner1);
		
		IArchiveFolder images = ArchiveNodeFactory.createFolder();
		images.setName("images");
		topFolder.addChild(images);
		
		IArchiveFileSet outerFileset = ArchiveNodeFactory.createFileset();
		outerFileset.setInWorkspace(false);
		outerFileset.setRawSourcePath(fileTrees.append("misc").toString());
		outerFileset.setExcludesPattern("**/*.gif,**/*.png");
		outerFileset.setIncludesPattern("**/*");
		topFolder.addChild(outerFileset);
		
		IArchiveFileSet imageFileset = ArchiveNodeFactory.createFileset();
		imageFileset.setInWorkspace(false);
		imageFileset.setRawSourcePath(fileTrees.append("misc").toString());
		imageFileset.setIncludesPattern("**/*.gif,**/*.png,**/*.xml");
		images.addChild(imageFileset);
		
		((ArchiveNodeImpl)root).clearDelta();
		return root;
	}
	
	public void testFindAllDescendentFilesets() {
		IArchiveFileSet[] fsets = ModelUtil.findAllDescendentFilesets(rootArchive);
		assertTrue(fsets.length == 2);
		assertTrue(fsets[0].getParent().getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER);
		assertTrue(fsets[1].getParent().getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER);
		assertTrue(ModelUtil.findAllDescendentFilesets(ArchiveNodeFactory.createFileset()).length == 1);
	}
	
	public void testFindAllDescendentFolders() {
		IArchiveFolder[] folders = ModelUtil.findAllDescendentFolders(rootArchive);
		assertTrue(folders.length == 3);
	}
	
	public void testFindAllDescendentsGeneric() {
		assertTrue(ModelUtil.findAllDescendents(rootArchive, IArchiveNode.TYPE_ARCHIVE, false).size() == 0);
		assertTrue(ModelUtil.findAllDescendents(rootArchive, IArchiveNode.TYPE_ARCHIVE, true).size() == 1);
		assertTrue(ModelUtil.findAllDescendents(rootArchive, IArchiveNode.TYPE_ARCHIVE_FOLDER, false).size() == 3);
		assertTrue(ModelUtil.findAllDescendents(rootArchive, IArchiveNode.TYPE_ARCHIVE_FILESET, false).size() == 2);
	}
	
	public void testGetMatchingFileSets() {
		IPath misc = inputs.append("fileTrees").append("misc");
		IPath gif = misc.append("multiple_files.gif");
		IPath xml = misc.append("rug.xml");
		IPath html = misc.append("someHtml.html");
		
		IArchiveFileSet[] gifFS = ModelUtil.getMatchingFilesets(rootArchive, gif);
		IArchiveFileSet[] xmlFS = ModelUtil.getMatchingFilesets(rootArchive, xml);
		IArchiveFileSet[] htmlFS = ModelUtil.getMatchingFilesets(rootArchive, html);
		
		assertEquals(1, gifFS.length);
		assertEquals(2, xmlFS.length);
		assertEquals(1, htmlFS.length);
		
	}
	
	public void testOtherFilesetMatchesPath() throws ArchivesModelException {
		IPath xml = inputs.append("fileTrees").append("misc").append("rug.xml");
		IArchiveFileSet[] xmlFS = ModelUtil.getMatchingFilesets(rootArchive, xml);
		assertTrue(xmlFS.length == 2);
		assertFalse(testMatches(xmlFS[0], xml, rootArchive));
		assertFalse(testMatches(xmlFS[1], xml, rootArchive));

		IPath html = inputs.append("fileTrees").append("misc").append("someHtml.html");
		IArchiveFileSet[] htmlFS = ModelUtil.getMatchingFilesets(rootArchive, html);
		assertTrue(htmlFS.length == 1);
		assertFalse(testMatches(htmlFS[0], html, rootArchive));
		
		// add a temporary fileset that will match exactly
		IArchiveFileSet otherFS = ArchiveNodeFactory.createFileset();
		otherFS.setIncludesPattern(xmlFS[0].getIncludesPattern());
		otherFS.setInWorkspace(xmlFS[0].isInWorkspace());
		otherFS.setRawSourcePath(xmlFS[0].getRawSourcePath());
		xmlFS[0].getParent().addChild(otherFS);
		
		assertTrue(testMatches(xmlFS[0], xml, rootArchive));
	}
	
	private boolean testMatches(IArchiveFileSet fs, IPath absoluteFile, IArchiveNode node) {
		FileWrapper[] wrappers = fs.getMatches(absoluteFile);
		return ModelUtil.otherFilesetMatchesPathAndOutputLocation(fs, absoluteFile, wrappers[0].getFilesetRelative(), wrappers[0].getRootArchiveRelative().toString(), node); 
				
	}

}
