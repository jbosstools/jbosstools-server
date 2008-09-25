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
package org.jboss.ide.eclipse.archives.core.model;


/**
 * <p>
 * This interface represents a folder inside a package definition.
 * A folder can contain packages, filesets, and sub-folders.
 * </p>
 *
 * @author <a href="marshall@jboss.org">Marshall Culpepper</a>
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 * @version $Revision: 1929 $
 */
public interface INamedContainerArchiveNode extends IArchiveNode {
	public static final String ATTRIBUTE_PREFIX = "org.jboss.ide.eclipse.archives.core.model.INamedContainerArchiveNode"; //$NON-NLS-1$
	public static final String NAME_ATTRIBUTE = ATTRIBUTE_PREFIX + "name"; //$NON-NLS-1$

	/**
	 * @return The name of this folder
	 */
	public String getName();

	/**
	 * Set the name of this folder
	 * @param name The name of this folder
	 */
	public void setName(String name);

	/**
	 * @return An array of sub-packages of this folder
	 */
	public IArchive[] getArchives();

	/**
	 * @return An array of sub-folders of this folder
	 */
	public IArchiveFolder[] getFolders();

	/**
	 * @return An array of filesets whose destination is this folder
	 */
	public IArchiveFileSet[] getFileSets();

}
