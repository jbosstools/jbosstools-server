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
