/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class OperationResults extends StandardMBean implements
        OperationResultsMBean {

    public OperationResults() throws NotCompliantMBeanException {
        super(OperationResultsMBean.class);
    }
    
    public int[] intsOperation() {
        return new int[] {-3, -2, -1, 0, 1, 2, 3};
    }

    public String stringOperation() {
        return "operation returned a String"; //$NON-NLS-1$
    }

    public void voidOperation() {
        // do nothing
    }
    
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public Collection collectionOperation() {
        Collection<String> coll = new ArrayList<String>();
        coll.add("first"); //$NON-NLS-1$
        coll.add("second"); //$NON-NLS-1$
        coll.add("third"); //$NON-NLS-1$
        coll.add("fourth"); //$NON-NLS-1$
        return coll;
    }
    
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public Map mapOperation() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("first key", "first value"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("second key", "second value"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("third key", "third value"); //$NON-NLS-1$ //$NON-NLS-2$
        return map;
    }
}
