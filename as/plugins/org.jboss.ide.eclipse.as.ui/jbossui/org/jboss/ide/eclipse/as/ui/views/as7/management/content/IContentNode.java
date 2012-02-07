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

import org.eclipse.wst.server.core.IServer;

/**
 * IContentNode
 * 
 * <p/>
 * Base type for server content.
 * 
 * @author Rob Cernich
 */
public interface IContentNode<T extends IContainerNode<?>> {

    /**
     * @return returns the server containing this node.
     */
    public IServer getServer();

    /**
     * @return the resource containing this node, if any.
     */
    public IResourceNode getParent();

    /**
     * @return the containing node.
     */
    public T getContainer();

    /**
     * @return the name of this node.
     */
    public String getName();

    /**
     * @return this node's addres, e.g. /subsystem=foo
     */
    public String getAddress();

    /**
     * Frees any resources held by this node.
     */
    public void dispose();
}
