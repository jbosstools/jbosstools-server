/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.archives.test.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelListener;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveModelNode;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.tools.test.util.ResourcesUtils;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class ModelCreationTest extends ModelTest {
	protected IPath project = new Path("test").append("project");
	TempArchiveModelListener modelListener = createListener();
	protected void setUp() throws Exception {
		modelListener.clearDelta();
	}

	public void testSimpleCreation() {
		createEmptyModelNode();
	}
	
	
	/*
	 * Testing the validity of adding certain types
	 * of nodes to others. 
	 */
	public void testRegisterInModel() {
		ArchiveModelNode modelNode = createEmptyModelNode();
		modelNode.getModel().registerProject(modelNode, new NullProgressMonitor());
		assertEquals(modelNode,modelNode.getModel().getRoot(project));
		assertNotSame(null, modelListener.getDelta());
		assertEquals(IArchiveNodeDelta.NODE_REGISTERED, modelListener.getDelta().getKind());
		modelNode.getModel().registerProject(modelNode, new NullProgressMonitor());
	}
	
	
	// Add everything to the root model
	
	public void testAddFolderToModel() {
		try {
			createEmptyModelNode().addChild(createFolder("testFolder"));
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}
	
	public void testAddFilesetToModel() {
		try {
			createEmptyModelNode().addChild(createFileSet("*", "blah"));
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}
	
//	public void testAddActionToModel() {
//		try {
//			createEmptyModelNode().addChild(createAction());;
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}
	
	public void testAddArchiveToModel() {
		try {
			createEmptyModelNode().addChild(createArchive("someName.war", "test"));
		} catch( ArchivesModelException ame ) {
			fail();
		}
	}

	/*
	 * Let's make sure all 4 types can be added to an archive.
	 */
	public void testAddArchiveToArchive() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchive archive2 = createArchive("someName.war2", "test2");
			archive.addChild(archive2);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
	}
	
	public void testAddFolderToArchive() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchiveFolder folder = createFolder("test3");
			archive.addChild(folder);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
	}

	public void testAddFilesetToArchive() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchiveFileSet fs = createFileSet("*", "blah");
			archive.addChild(fs);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
	}

//	public void testAddActionToArchive() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchiveAction action = createAction();
//			archive.addChild(action);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			fail();
//		}
//	}
	
	

	/*
	 * Let's make sure all 4 types can be added to an INNER archive.
	 */
	public void testAddArchiveToInnerArchive() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchive archive2 = createArchive("someName.war2", "test2");
			IArchive archive3 = createArchive("someName.war3", "test3");
			archive.addChild(archive2);
			archive2.addChild(archive3);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
	}
	
	public void testAddFolderToInnerArchive() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchive archive2 = createArchive("someName.war2", "test2");
			IArchiveFolder folder = createFolder("test3");
			archive.addChild(archive2);
			archive2.addChild(folder);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
	}

	public void testAddFilesetToInnerArchive() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchive archive2 = createArchive("someName.war2", "test2");
			IArchiveFileSet fs = createFileSet("*", "blah");
			archive.addChild(archive2);
			archive2.addChild(fs);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
	}
//
//	public void testAddActionToInnerArchive() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchive archive2 = createArchive("someName.war2", "test2");
//			IArchiveAction action = createAction();
//
//			archive.addChild(archive2);
//			archive2.addChild(action);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}

	
	// Add all to INNER-folder
	
	public void testAddArchiveToInnerFolder() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchive archive2 = createArchive("someName.war2", "test2");
			IArchiveFolder folder = createFolder("test3");

			archive.addChild(folder);
			folder.addChild(archive2);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
		return;
	}
	
	public void testAddFolderToInnerFolder() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchiveFolder folder = createFolder("folder");
			IArchiveFolder folder2 = createFolder("folder2");

			archive.addChild(folder);
			folder.addChild(folder2);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
		return;
	}

	public void testAddFilesetToInnerFolder() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchive archive2 = createArchive("someName2.war", "test2");
			IArchiveFolder folder = createFolder("test3");

			archive.addChild(folder);
			folder.addChild(archive2);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail();
		}
		return;
	}
	

//	public void testAddActionToInnerFolder() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchiveFolder folder = createFolder("folder");
//			IArchiveAction action = createAction();
//
//			archive.addChild(folder);
//			folder.addChild(action);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}
//	
//	// add all to action
//	public void testAddArchiveToAction() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchiveAction action = createAction();
//			IArchiveNode child = createArchive("someName2.war", "test2");
//			archive.addChild(action);
//			action.addChild(child);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}
//	
//	public void testAddFolderToAction() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchiveAction action = createAction();
//			IArchiveNode child = createFolder("test");
//			archive.addChild(action);
//			action.addChild(child);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}
//	
//	public void testAddFilesetToAction() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchiveAction action = createAction();
//			IArchiveNode child = createFileSet("*", "path");
//			archive.addChild(action);
//			action.addChild(child);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}
//	
//
//	public void testAddActionToAction() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchiveAction action = createAction();
//			IArchiveNode child = createAction();
//			archive.addChild(action);
//			action.addChild(child);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}
	
	// add all to fileset
	public void testAddArchiveToFileset() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchiveFileSet fs = createFileSet("*", "path");
			IArchiveNode child = createArchive("someName.war", "test");
			archive.addChild(fs);
			fs.addChild(child);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}
	
	public void testAddFolderToFileset() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchiveFileSet fs = createFileSet("*", "path");
			IArchiveNode child = createFolder("test");
			archive.addChild(fs);
			fs.addChild(child);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}
	
	public void testAddFilesetToFileset() {
		try {
			IArchive archive = createArchive("someName.war", "test");
			IArchiveFileSet fs = createFileSet("*", "path");
			IArchiveNode child = createFileSet("*", "path");
			archive.addChild(fs);
			fs.addChild(child);
			createEmptyModelNode().addChild(archive);
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}
	
//	public void testAddActionToFileset() {
//		try {
//			IArchive archive = createArchive("someName.war", "test");
//			IArchiveFileSet fs = createFileSet("*", "path");
//			IArchiveNode child = createAction();
//			archive.addChild(fs);
//			fs.addChild(child);
//			createEmptyModelNode().addChild(archive);
//		} catch( ArchivesModelException ame ) {
//			return;
//		}
//		fail();
//	}	
	
	
	
	// Test purposely matching archive / folder names
	public void testAddFolderClashingFolder() {
		IArchive root = createArchive("root.war", "blah");
		IArchiveFolder folder1 = createFolder("folder");
		IArchiveFolder folder2 = createFolder("folder");
		ArchiveModelNode model = createEmptyModelNode();
		try {
			model.addChild(root);
			root.addChild(folder1);
		} catch( ArchivesModelException ame ) {
			fail(ame.getMessage());
		}
		
		try {
			root.addChild(folder2);
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}

	public void testAddFolderClashingArchive() {
		IArchive root = createArchive("root.war", "blah");
		IArchive archive = createArchive("test.war", "dest");
		IArchiveFolder folder = createFolder("test.war");
		ArchiveModelNode model = createEmptyModelNode();
		try {
			model.addChild(root);
			root.addChild(archive);
		} catch( ArchivesModelException ame ) {
			fail(ame.getMessage());
		}
		
		try {
			root.addChild(folder);
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}

	public void testAddArchiveClashingFolder() {
		IArchive root = createArchive("root.war", "blah");
		IArchive archive = createArchive("test.war", "dest");
		IArchiveFolder folder = createFolder("test.war");
		ArchiveModelNode model = createEmptyModelNode();
		try {
			model.addChild(root);
			root.addChild(folder);
		} catch( ArchivesModelException ame ) {
			fail(ame.getMessage());
		}
		
		try {
			root.addChild(archive);
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}

	public void testAddArchiveClashingArchive() {
		IArchive root = createArchive("root.war", "blah");
		IArchiveFolder folder = createFolder("folder");
		IArchiveFolder folder2 = createFolder("folder");
		ArchiveModelNode model = createEmptyModelNode();
		try {
			model.addChild(root);
			root.addChild(folder);
		} catch( ArchivesModelException ame ) {
			fail(ame.getMessage());
		}
		
		try {
			root.addChild(folder2);
		} catch( ArchivesModelException ame ) {
			return;
		}
		fail();
	}
	
	// should clash, same destinations
	public void testArchiveClashingArchiveInModel() throws Exception {
		// copy a project
		IProject proj = null;
		proj = ResourcesUtils.importProject("org.jboss.ide.eclipse.archives.test", "/inputs/projects/basicwebproject");
		proj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());


		ArchiveModelNode model = createEmptyModelNode();
		IArchive root = createArchive("root.war", "basicwebproject");
		IArchive root2 = createArchive("root.war", "basicwebproject");
		
		try {
			model.addChild(root);
			model.addChild(root2);
			fail();
		} catch( ArchivesModelException ame ) {
			return;
		} finally {
			try {
				proj.delete(true, true, null);
			} catch( CoreException ce ) {fail();}
		}
	}
	
	// Should not clash, different destinations
	public void testArchiveNotClashingArchiveInModel() {
		ArchiveModelNode model = createEmptyModelNode();
		IArchive root = createArchive("root.war", "blah");
		IArchive root2 = createArchive("root.war", "blah2");
		try {
			model.addChild(root);
			model.addChild(root2);
		} catch( ArchivesModelException ame ) {
			fail();
		}
		return;
	}



	protected TempArchiveModelListener createListener() {
		return new TempArchiveModelListener();
	}
	
	protected class TempArchiveModelListener implements IArchiveModelListener {
		private IArchiveNodeDelta delta;
		public void modelChanged(IArchiveNodeDelta delta) {
			this.delta = delta;
		} 
		public IArchiveNodeDelta getDelta() { return delta; }
		public void clearDelta() { delta = null; }
	}
	
	protected ArchiveModelNode createModelNode() {
		try {
			XbPackages packs = new XbPackages();
			XbPackage pack = new XbPackage();
			packs.addChild(pack);
			ArchiveModelNode model = getModel(packs);
			ModelUtil.fillArchiveModel(packs, model);
			assertEquals(project, model.getProjectPath());
			assertEquals(IArchiveNode.TYPE_MODEL_ROOT, model.getNodeType());
			assertEquals(null, model.getParent());
			assertEquals(packs, model.getNodeDelegate());
			assertTrue(model.hasChildren());
			assertEquals(1, model.getAllChildren().length);
			assertEquals(null, ArchivesModel.instance().getRoot(project));
			assertEquals(null, modelListener.getDelta());
			return model;
		} catch( ArchivesModelException ame ) {
			fail(ame.getMessage());
		}
		return null;
	}
	
	protected ArchiveModelNode createEmptyModelNode() {
		try {
			XbPackages packs = new XbPackages();
			ArchiveModelNode model = getModel(packs);
			ModelUtil.fillArchiveModel(packs, model);
			assertEquals(project, model.getProjectPath());
			assertEquals(IArchiveNode.TYPE_MODEL_ROOT, model.getNodeType());
			assertEquals(null, model.getParent());
			assertEquals(packs, model.getNodeDelegate());
			assertFalse(model.hasChildren());
			assertEquals(0, model.getAllChildren().length);
			assertEquals(null, ArchivesModel.instance().getRoot(project));
			assertEquals(null, modelListener.getDelta());
			return model;
		} catch( ArchivesModelException ame ) {
			fail(ame.getMessage());
		}
		return null;
	}
	
	
	protected ArchiveModelNode getModel(XbPackages packs) {
		IArchiveModel model = new ArchivesModel();
		model.addModelListener(modelListener);
		ArchiveModelNode node = new ArchiveModelNode(project, packs, model);
		return node;
	}
	
}
