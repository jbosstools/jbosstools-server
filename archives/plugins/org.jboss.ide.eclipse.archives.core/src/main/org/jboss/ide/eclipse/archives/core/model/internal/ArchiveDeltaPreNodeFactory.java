/*
 * JBoss, a division of Red Hat
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
package org.jboss.ide.eclipse.archives.core.model.internal;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
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
	public static ArchiveNodeImpl createNode(ArchiveNodeDeltaImpl parentDelta, ArchiveNodeImpl postChange, 
			HashMap attributeChanges, HashMap propertyChanges) {
		
		switch(postChange.getNodeType()) {
		case IArchiveNode.TYPE_ARCHIVE_FILESET:
			XbFileSet fs = createFileset((ArchiveFileSetImpl)postChange, attributeChanges, propertyChanges); 
			return new DeltaFileset(fs, parentDelta, postChange);
		case IArchiveNode.TYPE_ARCHIVE_FOLDER:
			XbFolder folder = createFolder((ArchiveFolderImpl)postChange, attributeChanges, propertyChanges);
			return new DeltaFolder(folder, parentDelta, postChange);
		case IArchiveNode.TYPE_ARCHIVE:
			XbPackage pack = createPackage((ArchiveImpl)postChange, attributeChanges, propertyChanges);
			return new DeltaArchive(pack, parentDelta, postChange);
		}
		
		return null;
	}
	
	
	protected static XbFileSet createFileset(ArchiveFileSetImpl postChange,HashMap attributeChanges, HashMap propertyChanges ) {
		XbFileSet fs = new XbFileSet((XbFileSet)postChange.nodeDelegate);
		if( attributeChanges.containsKey(IArchiveFileSet.INCLUDES_ATTRIBUTE))
			fs.setIncludes(getBeforeString(attributeChanges, IArchiveFileSet.INCLUDES_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveFileSet.EXCLUDES_ATTRIBUTE))
			fs.setExcludes(getBeforeString(attributeChanges, IArchiveFileSet.EXCLUDES_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveFileSet.SOURCE_PATH_ATTRIBUTE))
			fs.setDir(getBeforeString(attributeChanges, IArchiveFileSet.SOURCE_PATH_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveFileSet.IN_WORKSPACE_ATTRIBUTE))
			fs.setInWorkspace(getBeforeBoolean(attributeChanges, IArchiveFileSet.IN_WORKSPACE_ATTRIBUTE));
		if( attributeChanges.containsKey(IArchiveFileSet.FLATTENED_ATTRIBUTE))
			fs.setFlattened(getBeforeBoolean(attributeChanges, IArchiveFileSet.FLATTENED_ATTRIBUTE));

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
		private ArchiveNodeDeltaImpl parentDelta; 
		private ArchiveNodeImpl impl;
		public DeltaFileset(XbFileSet fileset, ArchiveNodeDeltaImpl parentDelta, ArchiveNodeImpl impl){
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
		private ArchiveNodeDeltaImpl parentDelta; 
		private ArchiveNodeImpl impl;
		public DeltaFolder(XbFolder folder, ArchiveNodeDeltaImpl parentDelta, ArchiveNodeImpl impl){
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
		private ArchiveNodeDeltaImpl parentDelta; 
		private ArchiveNodeImpl impl;
		public DeltaArchive(XbPackage pack, ArchiveNodeDeltaImpl parentDelta, ArchiveNodeImpl impl){
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
