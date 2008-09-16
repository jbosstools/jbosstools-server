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
