/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.views.as7.management.content;

import java.util.List;

/**
 * IContainerNode
 * 
 * <p/>
 * Base interface for container (non-leaf) nodes.
 * 
 * @author Rob Cernich
 */
public interface IContainerNode<T extends IContainerNode<?>> extends IContentNode<T> {

    /**
     * @return the children of this container.
     */
    public List<? extends IContentNode<?>> getChildren();

    /**
     * Loads the content of this container. This method is invoked by the
     * content provider if getChildren() returns null.
     */
    public void load();

    /**
     * Clears the children of this container. After this method has been
     * invoked, getChildren() must return null. This method is called when a
     * refresh is requested.
     */
    public void clearChildren();

}
