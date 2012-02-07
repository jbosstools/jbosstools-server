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
 * IAttributesContainer
 * 
 * <p/>
 * Simple intermediary for a resource's attributes.
 * 
 * @author Rob Cernich
 */
public interface IAttributesContainer extends IContainerNode<IResourceNode> {

    /**
     * Represents the type for the attributes container. Can be used in an
     * address, e.g. /subsystem=foo/attributes.
     */
    public static final String ATTRIBUTES_TYPE = "attributes"; //$NON-NLS-1$

}
