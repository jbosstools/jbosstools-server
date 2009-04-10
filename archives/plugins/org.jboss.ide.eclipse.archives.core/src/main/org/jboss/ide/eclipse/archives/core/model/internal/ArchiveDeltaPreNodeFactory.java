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
package org.jboss.ide.eclipse.archives.core.model.internal;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeDeltaImpl.NodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFolder;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNodeWithProperties;


/**
 * This class generates a replica of what an archive 
 * node looked like before the changes that instigated
 * the delta.
 * 
 * Because these replica nodes will not be connected ot the model, 
 * they need to know their project and their parent properly
 * @author rstryker
 *
 */
public class ArchiveDeltaPreNodeFactory {
	
	// children get added later
	public IArchiveNode createNode(IArchiveNodeDelta parentDelta, IArchiveNode postChange, 
			HashMap attributeChanges, HashMap propertyChanges) {
		
		switch(postChange.getNodeType()) {
		case IArchiveNode.TYPE_ARCHIVE_FILESET:
			if( postChange instanceof IArchiveStandardFileSet) {
				XbFileSet fs = createFileset((ArchiveFileSetImpl)postChange, attributeChanges, propertyChanges); 
				return new DeltaFileset(fs, parentDelta, postChange);
			} 
			break;
		case IArchiveNode.TYPE_ARCHIVE_FOLDER:
			if( postChange instanceof ArchiveFolderImpl) {
				XbFolder folder = createFolder((ArchiveFolderImpl)postChange, attributeChanges, propertyChanges);
				return new DeltaFolder(folder, parentDelta, postChange);
			}
			break;
		case IArchiveNode.TYPE_ARCHIVE:
			if( postChange instanceof ArchiveImpl) {
				XbPackage pack = createPackage((ArchiveImpl)postChange, attributeChanges, propertyChanges);
				return new DeltaArchive(pack, parentDelta, postChange);
			} 
			break;
		}
		return null;
	}
	
	
	protected static XbFileSet createFileset(ArchiveFileSetImpl postChange,HashMap attributeChanges, HashMap propertyChanges ) {
		XbFileSet fs = new XbFileSet((XbFileSet)postChange.nodeDelegate);
		if( attributeChanges.containsKey(IArchiveStandardFileSet.INCLUDES_ATTRIBUTE))
			fs.setIncludes(getBeforeString(attributeChanges, IArchiveStandardFileSet.INCLUDES_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveStandardFileSet.EXCLUDES_ATTRIBUTE))
			fs.setExcludes(getBeforeString(attributeChanges, IArchiveStandardFileSet.EXCLUDES_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveStandardFileSet.SOURCE_PATH_ATTRIBUTE))
			fs.setDir(getBeforeString(attributeChanges, IArchiveStandardFileSet.SOURCE_PATH_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveStandardFileSet.IN_WORKSPACE_ATTRIBUTE))
			fs.setInWorkspace(getBeforeBoolean(attributeChanges, IArchiveStandardFileSet.IN_WORKSPACE_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveStandardFileSet.FLATTENED_ATTRIBUTE))
			fs.setFlattened(getBeforeBoolean(attributeChanges, IArchiveStandardFileSet.FLATTENED_ATTRIBUTE));

		undoPropertyChanges(fs, propertyChanges);
		return fs;
	}
	
	protected static XbFolder createFolder(ArchiveFolderImpl postChange,HashMap attributeChanges, HashMap propertyChanges ) {
		XbFolder folder = new XbFolder((XbFolder)postChange.nodeDelegate);
		if( attributeChanges.containsKey(IArchiveFolder.NAME_ATTRIBUTE))
			folder.setName(getBeforeString(attributeChanges, IArchiveFolder.NAME_ATTRIBUTE));
		undoPropertyChanges(folder, propertyChanges);
		return folder;
	}
	
	protected static XbPackage createPackage(ArchiveImpl postChange,HashMap attributeChanges, HashMap propertyChanges ) {
		XbPackage pack = new XbPackage((XbPackage)postChange.nodeDelegate);
		if( attributeChanges.containsKey(IArchive.NAME_ATTRIBUTE))
			pack.setName(getBeforeString(attributeChanges, IArchive.NAME_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchive.PACKAGE_TYPE_ATTRIBUTE))
			pack.setPackageType(getBeforeString(attributeChanges, IArchive.PACKAGE_TYPE_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchive.DESTINATION_ATTRIBUTE))
			pack.setToDir(getBeforeString(attributeChanges, IArchive.DESTINATION_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchive.IN_WORKSPACE_ATTRIBUTE))
			pack.setInWorkspace(getBeforeBoolean(attributeChanges, IArchive.IN_WORKSPACE_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchive.EXPLODED_ATTRIBUTE))
			pack.setExploded(getBeforeBoolean(attributeChanges, IArchive.EXPLODED_ATTRIBUTE));
		undoPropertyChanges(pack, propertyChanges);
		return pack;
	}
	
	protected static boolean getBeforeBoolean(HashMap map, String key) {
		NodeDelta delta = (NodeDelta)map.get(key);
		if( delta != null ) {
			return ((Boolean)delta.getBefore()).booleanValue();
		}
		return true;
	}
	protected static String getBeforeString(HashMap map, String key) {
		NodeDelta delta = (NodeDelta)map.get(key);
		if( delta != null ) {
			return (String)delta.getBefore();
		}
		return null;
	}
	
	// set the properties here to what they were before the delta
	protected static void undoPropertyChanges(XbPackageNodeWithProperties node, HashMap changes) {
		String key;
		NodeDelta val;
		for( Iterator i = changes.keySet().iterator(); i.hasNext(); ) {
			key = (String) i.next();
			val = (NodeDelta)changes.get(key);
			if( val.getBefore() == null ) {
				node.getProperties().getProperties().remove(key);
			} else {
				node.getProperties().getProperties().setProperty(key, (String)val.getBefore());
			}
		}

	}
	
	
	/*
	 * Delta implementations of the various nodes.
	 * Project is stored because delta nodes (or pre-nodes) are not connected to the model. 
	 * Therefore the project must be kept handy. 
	 */
	
	/**
	 * Extending class representing a delta fileset
	 */
	public static class DeltaFileset extends ArchiveFileSetImpl {
		// everything goes through the delegate or the parent. Simple
		private IArchiveNodeDelta parentDelta; 
		private IArchiveNode impl;
		public DeltaFileset(XbFileSet fileset, IArchiveNodeDelta parentDelta, IArchiveNode impl){
			super(fileset);
			this.parentDelta = parentDelta;
			this.impl = impl;
		}
		public IArchiveNode getParent() {
			return parentDelta == null ? null : parentDelta.getPreNode();
		}
		public IPath getProjectPath() {
			return impl.getProjectPath();
		}
		public IArchiveModelRootNode getModelRootNode() {
			return impl.getModelRootNode();
		}
	}
	
	/**
	 * Extending class representing a delta folder
	 */
	public static class DeltaFolder extends ArchiveFolderImpl {
		private IArchiveNodeDelta parentDelta; 
		private IArchiveNode impl;
		public DeltaFolder(XbFolder folder, IArchiveNodeDelta parentDelta, IArchiveNode impl){
			super(folder);
			this.parentDelta = parentDelta;
			this.impl = impl;
		}
		public IArchiveNode getParent() {
			return parentDelta == null ? null : parentDelta.getPreNode();
		}
		public IPath getProjectPath() {
			return impl.getProjectPath();
		}
		public IArchiveModelRootNode getModelRootNode() {
			return impl.getModelRootNode();
		}
	}
	
	/**
	 * Extending class representing a delta archive
	 */
	public static class DeltaArchive extends ArchiveImpl {
		private IArchiveNodeDelta parentDelta; 
		private IArchiveNode impl;
		public DeltaArchive(XbPackage pack, IArchiveNodeDelta parentDelta, IArchiveNode impl){
			super(pack);
			this.parentDelta = parentDelta;
			this.impl = impl;
		}
		public IArchiveNode getParent() {
			return parentDelta == null ? null : parentDelta.getPreNode();
		}
		public IPath getProjectPath() {
			return impl.getProjectPath();
		}
		public IArchiveModelRootNode getModelRootNode() {
			return impl.getModelRootNode();
		}
	}
}
