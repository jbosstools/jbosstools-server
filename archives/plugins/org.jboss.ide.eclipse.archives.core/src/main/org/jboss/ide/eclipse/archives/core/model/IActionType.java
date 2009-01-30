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
public interface IActionType {
	/**
	 * Get the id for this action type
	 * @return
	 */
	public String getId();
	
	/**
	 * Get a label for this action type
	 * @return
	 */
	public String getLabel();
	
	/**
	 * Execute an action of your type
	 */
	public void execute(IArchiveAction action);
	
	/**
	 * Get a string representation for this action
	 */
	public String getStringRepresentation(IArchiveAction action);
}
