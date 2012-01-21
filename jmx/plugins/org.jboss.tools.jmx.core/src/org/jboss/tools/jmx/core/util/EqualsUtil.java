/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core.util;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class EqualsUtil {

	
    public static boolean operationEquals(MBeanOperationInfo one, MBeanOperationInfo two) {
    	if (one == two)
    	    return true;
    	return (one.getName().equals(two.getName()) &&
    		one.getReturnType().equals(two.getReturnType()) &&
    		one.getDescription().equals(two.getDescription()) &&
    		one.getImpact() == two.getImpact() &&
    		infoArrayEquals(one.getSignature(), two.getSignature()) /*&&
                    p.getDescriptor().equals(info.getDescriptor()*/);

    }
    
    public static boolean infoArrayEquals(MBeanParameterInfo[] a, MBeanParameterInfo[] a2) {
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

    public static boolean paramEquals(MBeanParameterInfo o1, MBeanParameterInfo o2) {
		if (o1 == o2)
		    return true;
		return (o1.getName().equals(o2.getName()) &&
			o1.getType().equals(o2.getType()) &&
			safeEquals(o1.getDescription(), o2.getDescription()) /*&&
	                o1.getDescriptor().equals(o2.getDescriptor())*/);
	}
	
    public static boolean safeEquals(Object o1, Object o2) {
		return o1 == o2 || !(o1 == null || o2 == null) || o1.equals(o2);  
	}
    
    public static boolean infoEquals(MBeanInfo one, MBeanInfo two) {
    	if (one == two )
    	    return true;
    	if (!safeStringEquals(one.getClassName(), two.getClassName()) || 
    	    !safeStringEquals(one.getDescription(),two.getDescription()))
    	    return false;
    	return true;
    }

    public static boolean safeStringEquals(String one, String two) {
		if( one == null ) 
			return two == null;
		return one.equals(two);
	}		
}
