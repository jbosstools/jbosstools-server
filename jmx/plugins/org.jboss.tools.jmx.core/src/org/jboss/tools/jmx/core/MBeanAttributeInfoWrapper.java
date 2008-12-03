/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.core;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;

public class MBeanAttributeInfoWrapper extends MBeanFeatureInfoWrapper {

    private MBeanAttributeInfo info;

    public MBeanAttributeInfoWrapper(MBeanAttributeInfo attrInfo,
            MBeanInfoWrapper wrapper) {
        super(wrapper);
        this.info = attrInfo;
    }

    public MBeanAttributeInfo getMBeanAttributeInfo() {
        return info;
    }

    public Object getValue() throws Exception {
    	try {
	        MBeanServerConnection mbsc = getMBeanServerConnection();
	        return mbsc.getAttribute(getObjectName(), info.getName());
    	} catch( Exception e ) {
    		return e;
    	}
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((info == null) ? 0 : info.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if( !super.equals(obj))
        	return false;
        
        final MBeanAttributeInfoWrapper other = (MBeanAttributeInfoWrapper) obj;
        if (info == null) {
            if (other.info != null)
                return false;
        } else if (!info.equals(other.info))
            return false;
        return true;
    }
}
