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
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public interface IArchiveAction extends IArchiveNode {

	public static final String ATTRIBUTE_PREFIX = "org.jboss.ide.eclipse.archives.core.model.IArchiveAction."; //$NON-NLS-1$
	public static final String ACTION_TYPE_ATTRIBUTE = ATTRIBUTE_PREFIX + "type"; //$NON-NLS-1$
	public static final String ACTION_TIME_ATTRIBUTE = ATTRIBUTE_PREFIX + "time"; //$NON-NLS-1$


	/**
	 * A constant to indicate the action is to run before
	 *  the build
	 */
	public static final String PRE_BUILD = "preBuild"; //$NON-NLS-1$

	/**
	 * A constant to indicate the action is to run after the build
	 */
	public static final String POST_BUILD = "postBuild"; //$NON-NLS-1$
	/**
	 * Get whether this should be executed
	 * before or after building the parent archive.
	 * @return
	 */
	public String getTime();

	/**
	 * Set whether this should be executed
	 * before or after building the parent archive.
	 * @return
	 */
	public void setTime(String time);

	/**
	 * Get the id of this action's type.
	 * These are to be provided via an extension point
	 * or ant task to be discovered in ArchivesCore.
	 * @return
	 */
	public String getTypeString();

	/**
	 * Get the type of action this is.
	 * These are to be provided via an extension point
	 * or ant task to be discovered in ArchivesCore.
	 * @return
	 */
	public IActionType getType();



	/**
	 * Set the type of action this is.
	 * These are to be provided via an extension point
	 * or ant task to be discovered in ArchivesCore.
	 * @return
	 */
	public void setType(String type);

	/**
	 * Execute me
	 */
	public void execute();

	/**
	 * ToString must give something usable
	 */
	public String toString();
}
