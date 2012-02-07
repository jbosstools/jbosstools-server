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

import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.CHILDREN;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.wst.server.core.IServer;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;

/**
 * ResourceNode
 * 
 * <p/>
 * Implementation of a resource node.
 * 
 * @author Rob Cernich
 */
public class ResourceNode extends ContainerNode<ITypeNode> implements IResourceNode {

    private ArrayList<IContentNode<? extends IContainerNode<?>>> children;

    protected ResourceNode(ITypeNode parent, String name) {
        super(parent, name);
    }

    protected ResourceNode(IServer server, String name) {
        super(server, name);
    }

    public String getAddress() {
        if (getParent() == null) {
            // special handling for root node
            return ""; //$NON-NLS-1$
        }
        return getParent().getAddress() + PATH_SEPARATOR + getContainer().getName() + "=" + getName(); //$NON-NLS-1$
    }

    protected List<IContentNode<? extends IContainerNode<?>>> delegateGetChildren() {
        return children;
    }

    protected void delegateClearChildren() {
        if (children != null) {
            for (IContentNode<? extends IContainerNode<?>> child : children) {
                child.dispose();
            }
            children.clear();
            children = null;
        }
    }

    protected void delegateLoad() throws Exception {
        String resultJSON = JBoss7ManagerUtil.executeWithService(new JBoss7ManagerUtil.IServiceAware<String>() {
            public String execute(IJBoss7ManagerService service) throws Exception {
                return service.execute(new AS7ManagementDetails(getServer()), createResourceDescriptionRequest());
            }
        }, getServer());
        ModelNode result = ModelNode.fromJSONString(resultJSON);
        children = new ArrayList<IContentNode<? extends IContainerNode<?>>>();
        if (result.hasDefined(ATTRIBUTES)) {
            populateAttributes(result.get(ATTRIBUTES).asObject());
        }
        if (result.hasDefined(CHILDREN)) {
            populateTypes(result.get(CHILDREN).asObject());
        }
        if (children.size() == 1) {
            IContentNode<? extends IContainerNode<?>> child = children.get(0);
            if (child instanceof IAttributesContainer) {
                // collapse the attributes container if it's the only child.
                IAttributesContainer attributesContainer = (IAttributesContainer) child;
                children.clear();
                attributesContainer.load();
                children.addAll(attributesContainer.getChildren());
            }
        }
    }

    private String createResourceDescriptionRequest() {
        ModelNode request = new ModelNode();
        request.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION);
        request.get(OP_ADDR).set(getManagementAddress(this));
        return request.toJSONString(true);
    }

    private void populateAttributes(ModelNode result) {
        Set<String> keys = result.keys();
        if (keys.size() > 0) {
            children.add(new AttributesContainer(this, new ArrayList<String>(keys)));
        }
    }

    private void populateTypes(ModelNode result) {
        Set<String> keys = result.keys();
        children.ensureCapacity(children.size() + keys.size());
        for (String key : keys) {
            children.add(new TypeNode(this, key));
        }
    }

}
