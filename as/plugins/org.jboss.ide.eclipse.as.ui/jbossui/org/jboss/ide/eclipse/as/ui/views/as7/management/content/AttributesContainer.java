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

import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.ide.eclipse.as.management.core.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;

/**
 * AttributesContainer
 * 
 * <p/>
 * Container node for resource attributes.
 * 
 * @author Rob Cernich
 */
public class AttributesContainer extends ContainerNode<IResourceNode> implements IAttributesContainer {

    private List<String> attributeNames;
    private List<IAttributeNode> attributes;

    /**
     * Create a new AttributesContainer.
     * 
     * @param parent
     * @param name
     */
    protected AttributesContainer(IResourceNode parent, List<String> attributeNames) {
        super(parent, ATTRIBUTES_TYPE);
        this.attributeNames = attributeNames;
    }

    protected List<IAttributeNode> delegateGetChildren() {
        return attributes;
    }

    protected void delegateClearChildren() {
        if (attributes != null) {
            for (IAttributeNode attribute : attributes) {
                attribute.dispose();
            }
            attributes.clear();
            attributes = null;
        }
    }

    public void dispose() {
        attributeNames.clear();
        super.dispose();
    }

    protected void delegateLoad() throws Exception {
        String resultJSON = JBoss7ManagerUtil.executeWithService(new JBoss7ManagerUtil.IServiceAware<String>() {
            public String execute(IJBoss7ManagerService service) throws Exception {
                return service.execute(new AS7ManagementDetails(getServer()), createResourceRequest());
            }
        }, getServer());
        ModelNode result = ModelNode.fromJSONString(resultJSON);
        attributes = new ArrayList<IAttributeNode>(attributeNames.size());
        for (String name : attributeNames) {
            if (result.hasDefined(name)) {
                attributes.add(new AttributeNode(this, name, result.get(name).asString()));
            } else {
                attributes.add(new AttributeNode(this, name, null));
            }
        }
    }

    private String createResourceRequest() {
        ModelNode request = new ModelNode();
        request.get(OP).set(READ_RESOURCE_OPERATION);
        request.get(OP_ADDR).set(getManagementAddress(getParent()));
        return request.toJSONString(true);
    }

}
