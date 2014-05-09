/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.ui.properties.cpu.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.jboss.tools.jmx.jvmmonitor.core.cpu.ITreeNode;
import org.jboss.tools.jmx.jvmmonitor.internal.ui.properties.cpu.AbstractFilteredTree.ViewerType;

/**
 * The action to find tree item.
 */
public class FindAction extends Action {

    /** The tree viewer. */
    private TreeViewer viewer;

    /** The viewer type. */
    private ViewerType type;

    /**
     * The constructor.
     */
    public FindAction() {
        setText(Messages.findLabel);
        setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
    }

    /*
     * @see Action#run()
     */
    @Override
    public void run() {
        new FindDialog().open();
    }

    /**
     * The target for find action.
     */
    public interface IFindTarget {

        /**
         * Gets the target tree viewer for find action.
         * 
         * @return The target tree viewer for find action
         */
        TreeViewer getTargetTreeViewer();

        /**
         * Gets the target tree model for find action.
         * 
         * @return The target tree model for find action
         */
        ITreeNode[] getTargetTreeNodes();
    }

    /**
     * Sets the viewer.
     * 
     * @param treeViewer
     *            The tree viewer
     * @param viewerType
     *            The viewer type
     */
    public void setViewer(TreeViewer treeViewer, ViewerType viewerType) {
        viewer = treeViewer;
        type = viewerType;
    }
}
