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

import java.util.Deque;
import java.util.LinkedList;

import org.eclipse.wst.server.core.IServer;
import org.jboss.dmr.ModelNode;

/**
 * ContentNode
 * 
 * <p/>
 * Base implementation of IContentNode.
 * 
 * @author Rob Cernich
 */
public class ContentNode<T extends IContainerNode<?>> implements IContentNode<T> {

    /** The path separator for addresses. */
    public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

    private final IServer server;
    private IResourceNode parent;
    private T container;
    private final String name;

    protected ContentNode(IServer server, String name) {
        this.server = server;
        this.parent = null;
        this.container = null;
        this.name = name;
    }

    protected ContentNode(T container, String name) {
        this.server = container.getServer();
        this.parent = container instanceof IResourceNode ? (IResourceNode) container : container.getParent();
        this.container = container;
        this.name = name;
    }

    public IResourceNode getParent() {
        return parent;
    }

    public T getContainer() {
        return container;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return getParent().getAddress() + PATH_SEPARATOR + getName();
    }

    public IServer getServer() {
        return server;
    }

    public void dispose() {
        container = null;
        parent = null;
    }

    /**
     * Constructs an address list that can be used in an operation request.
     * 
     * @param resource the resource being addressed
     * @return a model node containing the list of addresses to the resource.
     */
    protected static ModelNode getManagementAddress(IResourceNode resource) {
        ModelNode address = new ModelNode();
        Deque<IResourceNode> resources = new LinkedList<IResourceNode>();
        resources.push(resource);
        for (IResourceNode parent = resource.getParent(); parent != null; parent = parent.getParent()) {
            resources.push(parent);
        }
        do {
            resource = resources.pop();
            ITypeNode type = resource.getContainer();
            if (type != null) {
                address.add(type.getName(), resource.getName());
            }
        } while (!resources.isEmpty());
        return address;
    }

}
