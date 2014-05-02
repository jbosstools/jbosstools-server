/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.ui.actions;

import static org.jboss.tools.jmx.jvmmonitor.ui.ISharedImages.COLLAPSE_ALL_IMG_PATH;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.jmx.jvmmonitor.ui.Activator;

/**
 * The action to collapse all tree nodes.
 */
public class CollapseAllAction extends Action {

    /** The tree viewer. */
    private TreeViewer viewer;

    /**
     * The constructor.
     */
    public CollapseAllAction() {
        setToolTipText(Messages.collapseAllLabel);
        setImageDescriptor(Activator.getImageDescriptor(COLLAPSE_ALL_IMG_PATH));
        setId(getClass().getName());
    }

    /*
     * @see Action#run()
     */
    @Override
    public void run() {
        if (viewer != null) {
            viewer.collapseAll();
        }
    }

    /**
     * Gets the active viewer.
     * 
     * @return The active viewer
     */
    private static TreeViewer getTargetTreeViewer() {
        IWorkbenchPart part = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActivePart();

        if (part instanceof ICollapseTarget) {
            return ((ICollapseTarget) part).getTargetTreeViewer();
        }

        return null;
    }

    /**
     * The target for collapse all action.
     */
    public interface ICollapseTarget {

        /**
         * Gets the target tree viewer for collapse all action.
         * 
         * @return The target tree viewer for collapse all action
         */
        TreeViewer getTargetTreeViewer();
    }

    /**
     * Sets the viewer.
     * 
     * @param treeViewer
     *            The tree viewer
     */
    public void setViewer(TreeViewer treeViewer) {
        viewer = treeViewer;
    }    

}
