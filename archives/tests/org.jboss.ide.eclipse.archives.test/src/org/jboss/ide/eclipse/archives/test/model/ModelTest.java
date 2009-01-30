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
import org.jboss.ide.eclipse.archives.core.model.ArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public abstract class ModelTest extends TestCase {
	/*
	 * Utility methods
	 */
	protected IArchiveFolder createFolder(String name) {
		IArchiveFolder folder = ArchiveNodeFactory.createFolder();
		folder.setName(name);
		return folder;
	}
	
	protected IArchiveFileSet createFileSet(String includes, String path) {
		IArchiveFileSet fs = ArchiveNodeFactory.createFileset();
		fs.setIncludesPattern(includes);
		fs.setRawSourcePath( path );
		return fs;
	}
	
//	protected IArchiveAction createAction() {
//		IArchiveAction action = ArchiveNodeFactory.createAction();
//		action.setTime(IArchiveAction.POST_BUILD);
//		action.setType("ant");
//		return action;
//	}
	
	protected IArchive createArchive(String name, String dest) {
		IArchive archive = ArchiveNodeFactory.createArchive();
		archive.setName(name);
		archive.setDestinationPath(new Path(dest));
		return archive;
	}
}
