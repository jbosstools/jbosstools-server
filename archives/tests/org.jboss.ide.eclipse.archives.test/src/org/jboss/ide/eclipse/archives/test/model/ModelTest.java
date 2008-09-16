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
	
	protected IArchiveAction createAction() {
		IArchiveAction action = ArchiveNodeFactory.createAction();
		action.setTime(IArchiveAction.POST_BUILD);
		action.setType("ant");
		return action;
	}
	
	protected IArchive createArchive(String name, String dest) {
		IArchive archive = ArchiveNodeFactory.createArchive();
		archive.setName(name);
		archive.setDestinationPath(new Path(dest));
		return archive;
	}
}
