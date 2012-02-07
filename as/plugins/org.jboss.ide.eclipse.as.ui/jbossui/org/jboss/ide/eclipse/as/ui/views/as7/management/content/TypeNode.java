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

import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;

/**
 * TypeNode
 * 
 * <p/>
 * Implementation of ITypeNode. Loads child names for a specific resource
 * child-type.
 * 
 * @author Rob Cernich
 */
public class TypeNode extends ContainerNode<IResourceNode> implements ITypeNode {

    private List<IResourceNode> resources;

    protected TypeNode(IResourceNode container, String name) {
        super(container, name);
    }

    protected List<IResourceNode> delegateGetChildren() {
        return resources;
    }

    protected void delegateClearChildren() {
        if (resources != null) {
            for (IResourceNode resource : resources) {
                resource.dispose();
            }
            resources.clear();
            resources = null;
        }
    }

    protected void delegateLoad() throws Exception {
        String resultJSON = JBoss7ManagerUtil.executeWithService(new JBoss7ManagerUtil.IServiceAware<String>() {
            public String execute(IJBoss7ManagerService service) throws Exception {
                return service.execute(new AS7ManagementDetails(getServer()), createResourceNamesRequest());
            }
        }, getServer());
        // process the results
        ModelNode result = ModelNode.fromJSONString(resultJSON);
        List<ModelNode> childResources = result.asList();
        resources = new ArrayList<IResourceNode>(childResources.size());
        for (ModelNode child : childResources) {
            resources.add(new ResourceNode(this, child.asString()));
        }
    }

    private String createResourceNamesRequest() {
        ModelNode request = new ModelNode();
        request.get(OP).set(READ_CHILDREN_NAMES_OPERATION);
        request.get(CHILD_TYPE).set(getName());
        request.get(OP_ADDR).set(getManagementAddress(getParent()));
        return request.toJSONString(true);
    }

}
