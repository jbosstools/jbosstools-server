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
package org.jboss.ide.eclipse.archives.core.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.EventManager;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.core.util.internal.ModelTruezipBridge;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.archives.core.util.internal.ModelTruezipBridge.FileWrapperStatusPair;

/**
 * This delegate will either build from the model completely
 * (if the builder has been given a full build request) or
 * incrementally update the changed files in
 * **ANY AND ALL** filesets that they match, regardless of project.
 *
 * @author Rob Stryker (rob.stryker@redhat.com)
 *
 */
public class ArchiveBuildDelegate {

	public ArchiveBuildDelegate() {
	}


	/**
	 * A full project build has been requested.
	 * @param project The project containing the archive model
	 */
	public void fullProjectBuild(IPath project) {
		EventManager.cleanProjectBuild(project);
		EventManager.startedBuild(project);

		IArchiveModelRootNode root = ArchivesModel.instance().getRoot(project);
		if( root == null ) {
			IStatus s = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "An error occurred locating the root node for " + project.toOSString(), null);
			EventManager.error(null, new IStatus[]{s});
		} else {
			IArchiveNode[] nodes = root.getChildren(IArchiveNode.TYPE_ARCHIVE);
			ArrayList<IStatus> errors = new ArrayList<IStatus>();
			for( int i = 0; i < nodes.length; i++ ) {
				errors.addAll(Arrays.asList(fullArchiveBuild(((IArchive)nodes[i]), false)));
			}

			EventManager.finishedBuild(project);
			EventManager.error(null, errors.toArray(new IStatus[errors.size()]));
		}
	}

	/**
	 * Builds an archive entirely, overwriting whatever was in the output destination.
	 * @param pkg The archive to build
	 */
	public IStatus[] fullArchiveBuild(IArchive pkg) {
		return fullArchiveBuild(pkg, true);
	}
	protected IStatus[] fullArchiveBuild(IArchive pkg, boolean log) {
		if( !pkg.canBuild() ) {
			IStatus s = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID,
					"Cannot Build archive \"" + pkg.getName() +
					"\" due to a problem in the archive's configuration.", null);
			if( log )
				EventManager.error(pkg, new IStatus[]{s});
			return new IStatus[]{};
		}

		EventManager.cleanArchiveBuild(pkg);
		EventManager.startedBuildingArchive(pkg);

		ModelTruezipBridge.deleteArchive(pkg);
		IPath dest = PathUtils.getGlobalLocation(pkg);
		if( dest != null && !dest.toFile().exists() ) {
			if( !dest.toFile().mkdirs() ) {
				IStatus s = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID,
						"Cannot Build archive \"" + pkg.getName() +
						"\". Output location " + dest +
						" is not writeable", null);
				if( log )
					EventManager.error(pkg, new IStatus[]{s});
				return new IStatus[]{};
			}
		}

		ArrayList<IStatus> errors = new ArrayList<IStatus>();

		// Run the pre actions
		IArchiveAction[] actions = pkg.getActions();
		for( int i = 0; i < actions.length; i++ ) {
			if( actions[i].getTime().equals(IArchiveAction.PRE_BUILD)) {
				actions[i].execute();
			}
		}

		if( !ModelTruezipBridge.createFile(pkg) ) {
			IStatus e = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "Error creating output file for node " + pkg.toString());
			errors.add(e);
		}

		// force create all folders
		IArchiveFolder[] folders = ModelUtil.findAllDescendentFolders(pkg);
		for( int i = 0; i < folders.length; i++ ) {
			if( !ModelTruezipBridge.createFile(folders[i])) {
				IStatus e = new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "Error creating output file for node " + folders[i].toString());
				errors.add(e);
			}
		}

		// build the filesets
		IArchiveFileSet[] filesets = ModelUtil.findAllDescendentFilesets(pkg);
		for( int i = 0; i < filesets.length; i++ ) {
			IStatus[] errors2 = fullFilesetBuild(filesets[i], pkg);
			errors.addAll(Arrays.asList(errors2));
		}

		// Run the post actions
		for( int i = 0; i < actions.length; i++ ) {
			if( actions[i].getTime().equals(IArchiveAction.POST_BUILD)) {
				actions[i].execute();
			}
		}

		EventManager.finishedBuildingArchive(pkg);
		IStatus[] errors2 = errors.toArray(new IStatus[errors.size()]);
		if( log )
			EventManager.error(pkg, errors2 );
		return errors2;
	}

	/**
	 * Build the given fileset
	 * @param fileset The fileset to match
	 * @param topLevel The top level archive that the fileset belongs to
	 */
	protected IStatus[] fullFilesetBuild(IArchiveFileSet fileset, IArchive topLevel) {
		EventManager.startedCollectingFileSet(fileset);

		// reset the scanner. It *is* a full build afterall
		fileset.resetScanner();
		FileWrapper[] paths = fileset.findMatchingPaths();

		FileWrapperStatusPair result = ModelTruezipBridge.fullFilesetBuild(fileset, true);

		EventManager.filesUpdated(topLevel, fileset, paths);
		EventManager.finishedCollectingFileSet(fileset);
		return result.s;
	}

	/**
	 * Incremental build.
	 * Parameters are instance sof changed IPath objects
	 * Will search only the given node for matching descendent filesets
	 * @param archive   An archive to limit the scope to, or null if the entire default model
	 * @param addedChanged  A list of added or changed resource paths
	 * @param removed       A list of removed resource paths
	 */
	public void incrementalBuild(IArchive archive, Set<IPath> addedChanged,
			Set<IPath> removed, boolean workspaceRelative) {
		ArrayList<IStatus> errors = new ArrayList<IStatus>();

		// find any and all filesets that match each file
		Iterator<IPath> i;
		IPath path, globalPath;
		IArchiveFileSet[] matchingFilesets;
		ArrayList<IArchive> topPackagesChanged = new ArrayList<IArchive>();
		ArrayList<IArchiveFileSet> seen = new ArrayList<IArchiveFileSet>();

		// Handle the removed files first. Hopefully the fileset hasn't been reset yet
		// or it could make this block of code fail.
		i = removed.iterator();
		while(i.hasNext()) {
			path = ((IPath)i.next());
			globalPath = !workspaceRelative ? path :
				ArchivesCore.getInstance().getVFS().workspacePathToAbsolutePath(path);

			matchingFilesets = ModelUtil.getMatchingFilesets(archive, path, workspaceRelative);
			localFireAffectedTopLevelPackages(topPackagesChanged, matchingFilesets);
			for( int j = 0; j < matchingFilesets.length; j++ ) {
				IStatus[] errors2 = ModelTruezipBridge.deleteFiles(matchingFilesets[j], matchingFilesets[j].getMatches(globalPath), true);
				errors.addAll(Arrays.asList(errors2));
				if( !seen.contains(matchingFilesets[j])) {
					seen.add(matchingFilesets[j]);
				}
			}
			EventManager.fileRemoved(path, matchingFilesets);
		}

		// reset all of the filesets that have already matched
		Iterator<IArchiveFileSet> fit = seen.iterator();
		while(fit.hasNext())
			fit.next().resetScanner();

		i = addedChanged.iterator();
		while(i.hasNext()) {
			path = i.next();
			globalPath = !workspaceRelative ? path :
				ArchivesCore.getInstance().getVFS().workspacePathToAbsolutePath(path);
			matchingFilesets = ModelUtil.getMatchingFilesets(archive, path, workspaceRelative);
			localFireAffectedTopLevelPackages(topPackagesChanged, matchingFilesets);
			for( int j = 0; j < matchingFilesets.length; j++ ) {
				if( !seen.contains(matchingFilesets[j])) {
					seen.add(matchingFilesets[j]);
					matchingFilesets[j].resetScanner();
				}
				IStatus[] errors2 = ModelTruezipBridge.copyFiles(matchingFilesets[j], matchingFilesets[j].getMatches(globalPath), true);
				errors.addAll(Arrays.asList(errors2));
			}
			EventManager.fileUpdated(path, matchingFilesets);
		}


		TrueZipUtil.sync();

		Iterator<IArchive> i2 = topPackagesChanged.iterator();
		while(i2.hasNext()) {
			EventManager.finishedBuildingArchive(i2.next());
		}

		if( errors.size() > 0 )
			EventManager.error(null, errors.toArray(new IStatus[errors.size()]));
	}

	private void localFireAffectedTopLevelPackages(ArrayList<IArchive> affected, IArchiveFileSet[] filesets) {
		for( int i = 0; i < filesets.length; i++ ) {
			if( !affected.contains(filesets[i].getRootArchive())) {
				affected.add(filesets[i].getRootArchive());
				EventManager.startedBuildingArchive(filesets[i].getRootArchive());
			}
		}
	}
}
