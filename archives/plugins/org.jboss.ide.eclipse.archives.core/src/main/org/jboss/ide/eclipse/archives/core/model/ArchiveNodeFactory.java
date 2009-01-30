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
package org.jboss.ide.eclipse.archives.core.model;

import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveActionImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFileSetImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFolderImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveImpl;

/**
 * Just a factory for extenders to access our secret internals
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class ArchiveNodeFactory {
	public static IArchive createArchive() {
		return new ArchiveImpl();
	}
	
	public static IArchiveFileSet createFileset() {
		return new ArchiveFileSetImpl();
	}
	
	public static IArchiveFolder createFolder() {
		return new ArchiveFolderImpl();
	}
	
	public static IArchiveAction createAction() {
		return new ArchiveActionImpl();
	}
}
