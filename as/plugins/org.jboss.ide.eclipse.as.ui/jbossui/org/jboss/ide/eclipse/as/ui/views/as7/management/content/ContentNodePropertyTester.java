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

import org.eclipse.core.expressions.PropertyTester;

/**
 * ContentNodePropertyTester
 * 
 * <p/>
 * A property tester for use with IContentNode.
 * 
 * @author Rob Cernich
 */
public class ContentNodePropertyTester extends PropertyTester {

    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof IContentNode<?>) || expectedValue == null) {
            return false;
        }
        IContentNode<?> node = (IContentNode<?>)receiver;
        if ("nodeName".equals(property)) { //$NON-NLS-1$
            return node.getName().matches(expectedValue.toString());
        } else if ("nodeAddress".equals(property)) { //$NON-NLS-1$
            return node.getAddress().matches(expectedValue.toString());
        }
        return false;
    }

}
