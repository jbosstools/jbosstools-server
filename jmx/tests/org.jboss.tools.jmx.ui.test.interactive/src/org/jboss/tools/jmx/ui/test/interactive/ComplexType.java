/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
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
import java.util.Properties;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class ComplexType extends StandardMBean implements ComplexTypeMBean {

    public ComplexType() throws NotCompliantMBeanException {
        super(ComplexTypeMBean.class);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public Map getMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("first key", "first value"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("second key", "second value"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("third key", "third value"); //$NON-NLS-1$ //$NON-NLS-2$
        return map;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public HashMap getHashMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("must", "be"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("displayed", "as"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("a", "map"); //$NON-NLS-1$ //$NON-NLS-2$
        return map;
    }

    public Collection<String> getCollection() {
        Collection<String> coll = new ArrayList<String>();
        coll.add("first"); //$NON-NLS-1$
        coll.add("second"); //$NON-NLS-1$
        coll.add("third"); //$NON-NLS-1$
        coll.add("fourth"); //$NON-NLS-1$
        return coll;
    }

    public Properties getSystemProperties() {
        Properties props = System.getProperties();
        return props;
    }

}
