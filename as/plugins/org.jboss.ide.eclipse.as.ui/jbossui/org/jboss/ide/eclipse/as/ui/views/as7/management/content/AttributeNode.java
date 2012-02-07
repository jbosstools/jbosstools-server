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

/**
 * AttributeNode
 * 
 * <p/>
 * Represents and attribute of a resource.
 * 
 * @author Rob Cernich
 */
public class AttributeNode extends ContentNode<IAttributesContainer> implements IAttributeNode {

    private final String value;

    protected AttributeNode(IAttributesContainer parent, String name, String value) {
        super(parent, name);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
