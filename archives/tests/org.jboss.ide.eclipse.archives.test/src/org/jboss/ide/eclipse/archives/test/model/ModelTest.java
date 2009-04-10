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
package org.jboss.ide.eclipse.archives.test.model;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;
import org.jboss.ide.eclipse.archives.core.model.other.internal.ArchiveLibFileSetImpl;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspaceNodeFactory;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public abstract class ModelTest extends TestCase {
	protected static IArchiveNodeFactory getFactory() {
		return ArchivesCore.getInstance().getNodeFactory();
	}
	/*
	 * Utility methods
	 */
	protected IArchiveFolder createFolder(String name) {
		IArchiveFolder folder = getFactory().createFolder();
		folder.setName(name);
		return folder;
	}
	
	protected IArchiveStandardFileSet createFileSet(String includes, String path) {
		IArchiveStandardFileSet fs = getFactory().createFileset();
		fs.setIncludesPattern(includes);
		fs.setRawSourcePath( path );
		return fs;
	}
	
	protected IArchiveFileSet createLibFileSet(String name) {
		ArchiveLibFileSetImpl lfsi = ((WorkspaceNodeFactory)getFactory()).createLibFileset();
		lfsi.setId(name);
		return lfsi;
	}

//	protected IArchiveAction createAction() {
//		IArchiveAction action = ArchiveNodeFactory.createAction();
//		action.setTime(IArchiveAction.POST_BUILD);
//		action.setType("ant");
//		return action;
//	}
	
	protected IArchive createArchive(String name, String dest) {
		IArchive archive = getFactory().createArchive();
		archive.setName(name);
		archive.setDestinationPath(new Path(dest));
		return archive;
	}
}
