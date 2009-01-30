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
package org.jboss.ide.eclipse.archives.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeVisitor;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveActionImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFileSetImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFolderImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveModelNode;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbAction;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFolder;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNode;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;

/**
 * Utility class for matching model elements and stuff
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class ModelUtil {

	/**
	 * Get any declared filesets that match this path and are
	 * a child of the given node
	 * @param node
	 * @param path
	 * @return
	 */
	public static IArchiveFileSet[] getMatchingFilesets(IArchiveNode node, final IPath path) {
		return getMatchingFilesets(node, path, false);
	}

	public static IArchiveFileSet[] getMatchingFilesets(IArchiveNode node, final IPath path, final boolean inWorkspace) {
		final ArrayList<IArchiveFileSet> rets = new ArrayList<IArchiveFileSet>();
		IArchiveNodeVisitor visitor = new IArchiveNodeVisitor() {
			public boolean visit(IArchiveNode node) {
				if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET &&
						((IArchiveFileSet)node).matchesPath(path, inWorkspace)) {
					rets.add((IArchiveFileSet)node);
				}
				return true;
			}
		};

		if( node == null )
			ArchivesModel.instance().accept(visitor);
		else
			node.accept(visitor);

		return rets.toArray(new IArchiveFileSet[rets.size()]);
	}

	/**
	 * Find all filesets that are a child to this node
	 * @param node
	 * @return
	 */
	public static IArchiveFileSet[] findAllDescendentFilesets(IArchiveNode node) {
		ArrayList<IArchiveNode> matches = findAllDescendents(node, IArchiveNode.TYPE_ARCHIVE_FILESET, true);
		return matches.toArray(new IArchiveFileSet[matches.size()]);
	}

	/**
	 * Find all folders that are a child to this node
	 * @param node
	 * @return
	 */
	public static IArchiveFolder[] findAllDescendentFolders(IArchiveNode node) {
		ArrayList<IArchiveNode> matches = findAllDescendents(node, IArchiveNode.TYPE_ARCHIVE_FOLDER, false);
		return matches.toArray(new IArchiveFolder[matches.size()]);
	}

	/**
	 * Find all nodes of one type that are a child to this one
	 * @param node
	 * @return
	 */
	public static ArrayList<IArchiveNode> findAllDescendents(IArchiveNode node, final int type, final boolean includeSelf) {
		final ArrayList<IArchiveNode> matches = new ArrayList<IArchiveNode>();
		final IArchiveNode original = node;
		node.accept(new IArchiveNodeVisitor() {
			public boolean visit(IArchiveNode node) {
				if( ((node.getNodeType() == type) && !matches.contains(node)) && (includeSelf || node != original))
					matches.add(node);
				return true;
			}
		});
		return matches;
	}



	/**
	 * Do any filesets other than the parameter match this path?
	 */
	public static boolean otherFilesetMatchesPathAndOutputLocation(IArchiveFileSet fileset, FileWrapper file) {
		return otherFilesetMatchesPathAndOutputLocation(fileset, new Path(file.getAbsolutePath()),
				file.getFilesetRelative(), file.getRootArchiveRelative().toString(), null);
	}
	public static boolean otherFilesetMatchesPathAndOutputLocation(IArchiveFileSet fileset, IPath absolute,
			String fsRelative, String rootArchiveRelative, IArchiveNode root) {
		IArchiveFileSet[] filesets = ModelUtil.getMatchingFilesets(root, absolute);
		if( filesets.length == 0 || (filesets.length == 1 && Arrays.asList(filesets).contains(fileset))) {
			return false;
		} else {
			// other filesets DO match... but are they at the same location in the archive?
			boolean relativePathsMatch = false;
			boolean destinationsMatch = false;
			FileWrapper[] matches;
			for( int i = 0; i < filesets.length; i++ ) {
				if( fileset.equals(filesets[i])) continue;
				matches = filesets[i].getMatches(absolute);
				for( int j = 0; j < matches.length; j++ )
					relativePathsMatch |= matches[j].getRootArchiveRelative().toString().equals(rootArchiveRelative);
				destinationsMatch = fileset.getRootArchive().getArchiveFilePath().equals(filesets[i].getRootArchive().getArchiveFilePath());

				if( relativePathsMatch && destinationsMatch ) {
					// the two put the file in the same spot, within the same archive! It's a match!
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the raw file for this node, specifically,
	 * the file actually saved as an OS file.
	 * @param node
	 * @return
	 */
	public static IPath getBaseDestinationFile(IArchiveNode node) {
		IArchiveNode[] nodes = getReverseNodeTree(node);
		IPath lastConcrete = null;
		for( int i = 0; i < nodes.length; i++ ) {
			if( nodes[i] instanceof IArchive) {
				if( lastConcrete == null )
					lastConcrete = ((IArchive)nodes[i]).getArchiveFilePath();
				else
					lastConcrete = lastConcrete.append(((IArchive)nodes[i]).getName());

				if( !((IArchive)nodes[i]).isExploded())
					return lastConcrete;
			}  else if( nodes[i] instanceof IArchiveFolder ) {
				lastConcrete = lastConcrete.append(((IArchiveFolder)nodes[i]).getName());
			}
		}
		return lastConcrete;
	}

	public static IPath getBaseDestinationFile(IArchiveFileSet node, IPath fsRelative) {
		IPath last = getBaseDestinationFile(node);
		if( fsRelative != null ) {
			IArchiveNode[] nodes = getReverseNodeTree(node);
			boolean anyZipped = false;
			for( int i = 0; !anyZipped && i < nodes.length; i++ )
				if( nodes[i] instanceof IArchive && !((IArchive)nodes[i]).isExploded())
					anyZipped = true;

			if(!anyZipped) // none are zipped, we can append this path
				last = last.append(fsRelative);
		}
		return last;
	}

	private static IArchiveNode[] getReverseNodeTree(IArchiveNode node) {
		ArrayList<IArchiveNode> list = new ArrayList<IArchiveNode>();
		while( node != null && !(node instanceof ArchiveModelNode)) {
			list.add(node);
			node = node.getParent();
		}
		Collections.reverse(list);
		IArchiveNode[] nodes = list.toArray(new IArchiveNode[list.size()]);
		return nodes;
	}

	public static void fillArchiveModel( XbPackages node, IArchiveModelRootNode modelNode) throws ArchivesModelException {
		for (Iterator iter = node.getAllChildren().iterator(); iter.hasNext(); ) {
			XbPackageNode child = (XbPackageNode) iter.next();
			ArchiveNodeImpl childImpl = (ArchiveNodeImpl)createPackageNodeImpl(child, modelNode);
			if (modelNode != null && childImpl != null) {
				try {
					if( modelNode instanceof ArchiveNodeImpl )
						((ArchiveNodeImpl)modelNode).addChild(childImpl, false);
					else
						modelNode.addChild(childImpl);
				} catch( ArchivesModelException ame ) {
					ArchivesCore.getInstance().getLogger().log(IStatus.ERROR, ame.getMessage(), ame);
				}
			}
		}
	}

	protected static IArchiveNode createPackageNodeImpl (XbPackageNode node, IArchiveNode parent) throws ArchivesModelException {
		ArchiveNodeImpl nodeImpl = null;
		if (node instanceof XbPackage) {
			nodeImpl = new ArchiveImpl((XbPackage)node);
		} else if (node instanceof XbFolder) {
			nodeImpl = new ArchiveFolderImpl((XbFolder)node);
		} else if (node instanceof XbFileSet) {
			nodeImpl = new ArchiveFileSetImpl((XbFileSet)node);
		} else if( node instanceof XbAction ) {
			nodeImpl = new ArchiveActionImpl((XbAction)node);
		}

		for (Iterator iter = node.getAllChildren().iterator(); iter.hasNext(); ) {
			XbPackageNode child = (XbPackageNode) iter.next();
			ArchiveNodeImpl childImpl = (ArchiveNodeImpl)createPackageNodeImpl(child, nodeImpl);
			if (nodeImpl != null && childImpl != null) {
				nodeImpl.addChild(childImpl, false);
			}
		}
		return nodeImpl;
	}

	public static IArchive[] getProjectArchives(IPath project) {
		return getProjectArchives(project, ArchivesModel.instance());
	}

	public static IArchive[] getProjectArchives(IPath project, IArchiveModel model) {
		if( model != null ) {
			IArchiveModelRootNode root = model.getRoot(project);
			if( root == null ) return new IArchive[0];
			IArchiveNode[] archives = model.getRoot(project).getAllChildren();
			List<IArchiveNode> list = Arrays.asList(archives);
			return list.toArray(new IArchive[list.size()]);
		}
		return null;
	}
}
