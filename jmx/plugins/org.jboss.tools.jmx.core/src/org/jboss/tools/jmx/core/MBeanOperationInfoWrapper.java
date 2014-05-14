/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core;

import javax.management.MBeanOperationInfo;

import org.eclipse.core.runtime.Assert;
import org.jboss.tools.jmx.core.util.EqualsUtil;


public class MBeanOperationInfoWrapper extends MBeanFeatureInfoWrapper implements HasName {

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
        int infoHC = (info == null) ? 0 : info.hashCode();
        int onHC = getMBeanInfoWrapper() == null ? 0 : getMBeanInfoWrapper().getObjectName() == null ? 0 : 
        getMBeanInfoWrapper().getObjectName().hashCode();
        result = prime * result + (infoHC + onHC);
        
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
        } else if( !EqualsUtil.operationEquals(info, other.info))
            return false;
        String on1 = getMBeanInfoWrapper().getObjectName().toString();
        String on2 = other.getMBeanInfoWrapper().getObjectName().toString();
        boolean objectNamesMatch = on1 == on2 ? true : on1 == null ? false : on1.equals(on2);
        return objectNamesMatch;
    }

	public String getName() {
		return MBeanUtils.prettySignature(getMBeanOperationInfo());
	}


}
