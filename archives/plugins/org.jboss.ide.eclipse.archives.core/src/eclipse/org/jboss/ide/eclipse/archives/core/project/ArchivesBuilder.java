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
package org.jboss.ide.eclipse.archives.core.project;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeVisitor;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;

/**
 * This builder is responsible for building the archives
 * It delegates to a delegate.
 * @author Rob Stryker (rob.stryker@redhat.com)
 *
 */
public class ArchivesBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.jboss.ide.eclipse.archives.core.archivesBuilder"; //$NON-NLS-1$

	/**
	 * Build.
	 * @see IncrementalProjectBuilder#build(int, Map, IProgressMonitor)
	 * @see IProject#build(int, String, Map, IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {

		// if we're not to build, get out of here
		if( !ArchivesCore.getInstance().getPreferenceManager().isBuilderEnabled(getProject().getLocation()))
			return new IProject[]{};

		IProject[] interestingProjects = getInterestingProjectsInternal();

		final TreeSet<IPath> addedChanged = createPathTreeSet();
		final TreeSet<IPath> removed = createPathTreeSet();

		ArchiveBuildDelegate delegate = new ArchiveBuildDelegate();
		if (kind == IncrementalProjectBuilder.INCREMENTAL_BUILD || kind == IncrementalProjectBuilder.AUTO_BUILD) {
			fillDeltas(interestingProjects, addedChanged, removed);
			delegate.incrementalBuild(null,addedChanged, removed,true, monitor);
		} else if (kind == IncrementalProjectBuilder.FULL_BUILD){
			// build each package fully
			IProject p = getProject();
			delegate.fullProjectBuild(p.getLocation(), monitor);
		}

		return interestingProjects;
	}

	/**
	 * Delete all archives that were created or represented by this
	 * project's archive model.
	 */
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IPath p = getProject().getLocation();
		IArchiveModelRootNode root = ArchivesModel.instance().getRoot(p);
		if(root!=null) {
			IArchiveNode[] nodes = root.getChildren(IArchiveNode.TYPE_ARCHIVE);
			for( int i = 0; i < nodes.length; i++ ) {
				IPath path = ((IArchive)nodes[i]).getArchiveFilePath();
				TrueZipUtil.deleteAll(path);
			}
		}
	}

	/**
	 * Browse through the deltas and fill the treesets with
	 * affected resources.
	 * @param projects The interesting projects
	 * @param addedChanged A collection of resources that have been added or changed
	 * @param removed A collection of resources that have been removed.
	 */
	protected void fillDeltas(IProject[] projects, final TreeSet<IPath> addedChanged, final TreeSet<IPath> removed) {
		for( int i = 0; i < projects.length; i++ ) {
			final IProject proj = projects[i];
			IResourceDelta delta = getDelta(proj);
			if( delta == null ) continue;
			try {
				delta.accept(new IResourceDeltaVisitor () {
					public boolean visit(IResourceDelta delta) throws CoreException {
						if (delta.getResource().getType() == IResource.FILE)  {
							if( (delta.getKind() & IResourceDelta.ADDED) > 0
									|| (delta.getKind() & IResourceDelta.CHANGED) > 0) {

								// ignore the packages project. that will it's own build call,
								// or will handle the change in some other way
								if( !delta.getResource().equals(proj.findMember(IArchiveModel.DEFAULT_PACKAGES_FILE)))
									addedChanged.add(delta.getResource().getFullPath());
							} else if( (delta.getKind() & IResourceDelta.REMOVED ) > 0 ) {
								removed.add(delta.getResource().getFullPath());
							}
						}
						return true;
					}
				});
			} catch( CoreException ce) {
			}
		}
	}

	/**
	 * Get any projects that the current project may depend on
	 * (regarding it's archive model, if any filesets match files in
	 * another project).
	 * @return The list of projects that matter
	 */
	protected IProject[] getInterestingProjectsInternal() {
		final TreeSet<IProject> set = createProjectTreeSet();
		set.add(getProject());

		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		IArchiveModelRootNode root = ArchivesModel.instance().getRoot(getProject().getLocation());
		if(root!=null) {
			root.accept(new IArchiveNodeVisitor () {
				public boolean visit (IArchiveNode node) {
					if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE_FILESET) {
						IArchiveFileSet fileset = (IArchiveFileSet)node;
						IPath p = PathUtils.getGlobalLocation(fileset);
						if( p != null ) {
							IContainer[] containers = workspaceRoot.findContainersForLocation(p);
							for( int i = 0; i < containers.length; i++ ) {
								set.add(containers[i].getProject());
							}
						}
					}
					return true;
				}
			});
			return (IProject[]) set.toArray(new IProject[set.size()]);
		} else {
			return new IProject[0];
		}
	}

	/**
	 * Default treeset with default comparator
	 * @return
	 */
	protected TreeSet<IProject> createProjectTreeSet() {
		return new TreeSet<IProject>(new Comparator<IProject> () {
			public int compare(IProject o1, IProject o2) {
				if (o1.equals(o2)) return 0;
				else return -1;
			}
		});
	}

	protected TreeSet<IPath> createPathTreeSet() {
		return new TreeSet<IPath>(new Comparator<IPath> () {
			public int compare(IPath o1, IPath o2) {
				if (o1.equals(o2)) return 0;
				else return -1;
			}
		});
	}
}