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
package org.jboss.ide.eclipse.archives.ui.actions;

import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;

/**
 * All extensions of org.jboss.ide.eclipse.archives.ui.nodePopupMenus should implement this interface
 * (also see AbstractNodeActionDelegate)
 * @author Marshall
 *
 */
public interface INodeActionDelegate {

	/**
	 * @param node
	 * @return Whether or not this action delegate will be enabled (viewable) for a specific package node.
	 */
	public boolean isEnabledFor (IArchiveNode node);

	/**
	 * Run this action delegate on the passed-in node
	 * @param node A package node
	 */
	public void run (IArchiveNode node);
}
