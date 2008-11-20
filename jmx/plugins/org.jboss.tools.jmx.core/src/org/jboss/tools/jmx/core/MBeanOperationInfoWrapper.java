/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.core;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.eclipse.core.runtime.Assert;

public class MBeanOperationInfoWrapper extends MBeanFeatureInfoWrapper {

    private MBeanOperationInfo info;

    public MBeanOperationInfoWrapper(MBeanOperationInfo info,
            MBeanInfoWrapper wrapper) {
        super(wrapper);
        Assert.isNotNull(info);
        this.info = info;
    }

    public MBeanOperationInfo getMBeanOperationInfo() {
        return info;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((info == null) ? 0 : info.hashCode());
        return result;
    }

    
    /*
     * Everything below here is duplication from javax.management
     * to overcome a jboss bug where some mbeans do not conform to spec.
     */
    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof MBeanOperationInfoWrapper))
            return false;
        final MBeanOperationInfoWrapper other = (MBeanOperationInfoWrapper) obj;
        if (info == null) {
            if (other.info != null)
                return false;
        } else if (!equals2(other.info))
            return false;
        return true;
    }
    
    private boolean equals2(MBeanOperationInfo o) {
    	if (o == info)
    	    return true;
    	if (!(o instanceof MBeanOperationInfo))
    	    return false;
    	MBeanOperationInfo p = (MBeanOperationInfo) o;
    	return (p.getName().equals(info.getName()) &&
    		p.getReturnType().equals(info.getReturnType()) &&
    		p.getDescription().equals(info.getDescription()) &&
    		p.getImpact() == info.getImpact() &&
    		arrayEquals(p.getSignature(), info.getSignature()) &&
                    p.getDescriptor().equals(info.getDescriptor()));

    }
    
    private boolean arrayEquals(MBeanParameterInfo[] a, MBeanParameterInfo[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++) {
        	MBeanParameterInfo o1 = a[i];
        	MBeanParameterInfo o2 = a2[i];
            if (!(o1==null ? o2==null : paramEquals(o1,o2)))
                return false;
        }

        return true;
    }

	private boolean paramEquals(MBeanParameterInfo o1, MBeanParameterInfo o2) {
		if (o1 == o2)
		    return true;
		return (o1.getName().equals(o2.getName()) &&
			o1.getType().equals(o2.getType()) &&
			safeEquals(o1.getDescription(), o2.getDescription()) &&
	                o1.getDescriptor().equals(o2.getDescriptor()));
	}
	
	private boolean safeEquals(Object o1, Object o2) {
		return o1 == o2 || !(o1 == null || o2 == null) || o1.equals(o2);  
	}
}
