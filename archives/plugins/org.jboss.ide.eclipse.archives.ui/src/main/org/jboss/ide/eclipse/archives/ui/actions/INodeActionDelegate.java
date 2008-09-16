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
