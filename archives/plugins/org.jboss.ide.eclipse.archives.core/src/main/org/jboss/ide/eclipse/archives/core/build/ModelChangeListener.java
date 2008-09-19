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
package org.jboss.ide.eclipse.archives.core.build;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.EventManager;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelListener;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.archives.core.util.internal.ModelTruezipBridge;
import org.jboss.ide.eclipse.archives.core.util.internal.ModelTruezipBridge.FileWrapperStatusPair;

/**
 * This class responds to model change events.
 * It is given a delta as to what nodes are added, removed, or changed.
 * It then keeps the output file for the top level archive in sync with
 * the changes to the model.
 *
 * If the automatic builder is not enabled for this project, the listener
 * does nothing.
 *
 * @author Rob Stryker (rob.stryker@redhat.com)
 *
 */
public class ModelChangeListener implements IArchiveModelListener {

	/**
	 * This is the entry point for model change events.
	 * It immediately passes the delta to be handled.
	 */
	public void modelChanged(IArchiveNodeDelta delta) {
		// if we're not building, get out
		if( !ArchivesCore.getInstance().getPreferenceManager().isBuilderEnabled(delta.getPostNode().getProjectPath()))
			return;

		try {
			handle(delta);
		} catch( Exception e ) {
			ArchivesCore.getInstance().getLogger().log(IStatus.ERROR, "Error updating model changes", e);
		}
	}

	/**
	 * This can handle any type of node / delta, not just
	 * root elements. If the node is added or removed, it
	 * will handle those segments and return without checking
	 * the children at all. IT is the responsibility of the add
	 * and remove methods to go through the children.
	 *
	 *  Otherwise, it will simply handle attribute children and then
	 *  move on to the children.
	 *
	 * @param delta
	 */
	private void handle(IArchiveNodeDelta delta) {
		if( isTopLevelArchive(delta.getPostNode())) {
			EventManager.startedBuildingArchive((IArchive)delta.getPostNode());
		}

		if( (delta.getKind() & (IArchiveNodeDelta.NODE_REGISTERED | IArchiveNodeDelta.UNKNOWN_CHANGE)) != 0 ) {
			nodeRemoved(delta.getPreNode());
			nodeAdded(delta.getPostNode());
		} if( (delta.getKind() & IArchiveNodeDelta.REMOVED) != 0 ) {
			nodeRemoved(delta.getPreNode());
		} else if( (delta.getKind() & IArchiveNodeDelta.ADDED) != 0 ) {
			nodeAdded(delta.getPostNode());
		} else  if( (delta.getKind() & IArchiveNodeDelta.ATTRIBUTE_CHANGED) != 0) {
			boolean shouldHandleChildren = handleAttributeChange(delta);
			if( shouldHandleChildren ) {
				IArchiveNodeDelta[] children = delta.getAllAffectedChildren();
				for( int i = 0; i < children.length; i++ ) {
					handle(children[i]);
				}
			}
		} else if( descendentChanged(delta.getKind()) ) {
			IArchiveNodeDelta[] children = delta.getAllAffectedChildren();
			for( int i = 0; i < children.length; i++ ) {
				handle(children[i]);
			}
		}

		if( isTopLevelArchive(delta.getPostNode()))
			EventManager.finishedBuildingArchive((IArchive)delta.getPostNode());

	}
	protected boolean descendentChanged(int kind) {
		 return (kind & IArchiveNodeDelta.DESCENDENT_CHANGED) != 0 ||
		 		(kind & IArchiveNodeDelta.CHILD_ADDED) != 0 ||
		 		(kind & IArchiveNodeDelta.CHILD_REMOVED) != 0;
	}
	protected boolean isTopLevelArchive(IArchiveNode node) {
		if( node != null && node instanceof IArchive && ((IArchive)node).isTopLevel())
			return true;
		return false;
	}
	/**
	 * Handle changes in this node
	 * @param delta
	 * @return Whether or not the caller should also handle the children
	 */
	private boolean handleAttributeChange(IArchiveNodeDelta delta) {
		switch( delta.getPostNode().getNodeType()) {
		case IArchiveNode.TYPE_ARCHIVE_FOLDER:
			return handleFolderAttributeChanged(delta);
		case IArchiveNode.TYPE_ARCHIVE_FILESET:
			return handleFilesetAttributeChanged(delta);
		case IArchiveNode.TYPE_ARCHIVE:
			return handlePackageAttributeChanged(delta);
		}
		return false;
	}


	/*
	 * These three methods will need to be optimized eventually. Because right now they suck
	 */
	private boolean handleFolderAttributeChanged(IArchiveNodeDelta delta) {
		nodeRemoved(delta.getPreNode());
		nodeAdded(delta.getPostNode());
		return false;
	}

	private boolean handleFilesetAttributeChanged(IArchiveNodeDelta delta) {
		nodeRemoved(delta.getPreNode());
		nodeAdded(delta.getPostNode());
		return false;
	}

	private boolean handlePackageAttributeChanged(IArchiveNodeDelta delta) {
		nodeRemoved(delta.getPreNode());
		nodeAdded(delta.getPostNode());
		return false;
	}




	private void nodeAdded(IArchiveNode added) {
		if( added == null ) return;

		if( added.getNodeType() == IArchiveNode.TYPE_MODEL_ROOT) {
			IArchiveNode[] archives = ((IArchiveModelRootNode)added).getChildren(IArchiveNode.TYPE_ARCHIVE);
			for( int i = 0; i < archives.length; i++ ) {
				nodeAdded(archives[i]);
			}
		} else if( added.getNodeType() == IArchiveNode.TYPE_ARCHIVE) {
			// create the package
			if( ((IArchive)added).isTopLevel() && !added.canBuild() ) {
				logCannotBuildError((IArchive)added);
				return;
			}
			ModelTruezipBridge.createFile(added);
		} else if( added.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER ) {
			// create the folder
			ModelTruezipBridge.createFile(added);
		}
		IArchiveFileSet[] filesets = ModelUtil.findAllDescendentFilesets(added);
		for( int i = 0; i < filesets.length; i++ ) {
			FileWrapperStatusPair result = ModelTruezipBridge.fullFilesetBuild(filesets[i], true);
			FileWrapper[] files = filesets[i].findMatchingPaths();
			EventManager.error(filesets[i], result.s);
			EventManager.filesUpdated(filesets[i].getRootArchive(), filesets[i], files);
		}
		postChange(added);
	}


	private void nodeRemoved(IArchiveNode removed) {
		if( removed == null ) return;
		if( removed.getNodeType() == IArchiveNode.TYPE_MODEL_ROOT ) {
			// remove all top level items
			IArchiveNode[] kids = removed.getChildren(IArchiveNode.TYPE_ARCHIVE);
			for( int i = 0; i < kids.length; i++ ) {
				nodeRemoved(kids[i]);
			}
			postChange(removed);
			return;
		} else if( removed.getNodeType() == IArchiveNode.TYPE_ARCHIVE) {
			if( ((IArchive)removed).isTopLevel() && !removed.canBuild() ) {
				logCannotBuildError((IArchive)removed);
				return;
			}
			ModelTruezipBridge.deleteArchive((IArchive)removed);
			postChange(removed);
			return;
		} else if( removed.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FOLDER ){
			IArchiveFileSet[] filesets = ModelUtil.findAllDescendentFilesets(((IArchiveFolder)removed));
			for( int i = 0; i < filesets.length; i++ ) {
				// TODO report IO Errors
				FileWrapperStatusPair result = ModelTruezipBridge.fullFilesetRemove(filesets[i], false);
				EventManager.filesRemoved(convertToPath(result.f), ((IArchiveFileSet)filesets[i]));
			}
			postChange(removed);
			return;
		}

		IArchiveFileSet[] filesets = ModelUtil.findAllDescendentFilesets(removed);
		for( int i = 0; i < filesets.length; i++ ) {
			FileWrapperStatusPair result = ModelTruezipBridge.fullFilesetRemove(
					((IArchiveFileSet)removed), false);
			EventManager.filesRemoved(convertToPath(result.f), ((IArchiveFileSet)removed));
		}
		postChange(removed);
	}

	protected IPath[] convertToPath(FileWrapper[] wrappers) {
		IPath[] paths = new IPath[wrappers.length];
		for( int i = 0; i < wrappers.length; i++ ) {
			paths[i] = wrappers[i].getWrapperPath();
		}
		return paths;
	}
	protected void postChange(IArchiveNode node) {
	}

	protected void logCannotBuildError(IArchive archive) {
		ArchivesCore.getInstance().getLogger().log(IStatus.WARNING,
				"Cannot Build archive \"" + archive.getName() +
				"\" due to a problem in the archive's configuration.", null);
		return;
	}
}
