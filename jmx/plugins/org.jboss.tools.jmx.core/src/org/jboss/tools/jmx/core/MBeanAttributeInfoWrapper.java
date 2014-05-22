/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.core;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;


public class MBeanAttributeInfoWrapper extends MBeanFeatureInfoWrapper implements HasName {

    private MBeanAttributeInfo info;

    public MBeanAttributeInfoWrapper(MBeanAttributeInfo attrInfo,
            MBeanInfoWrapper wrapper) {
        super(wrapper);
        this.info = attrInfo;
    }

    public MBeanAttributeInfo getMBeanAttributeInfo() {
        return info;
    }

    public IConnectionWrapper getConnection() {
    	return getParent().getConnectionWrapper();
    }
    
    public Object getValue() throws Exception {
    	IConnectionWrapper con = getConnection();
    	final Object[] ret = new Object[1];
    	con.run(new IJMXRunnable(){
			@Override
			public void run(MBeanServerConnection connection) throws Exception {
				ret[0] = connection.getAttribute(getObjectName(), info.getName());
			}});
    	return ret[0];
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((info == null) ? 0 : info.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MBeanAttributeInfoWrapper other = (MBeanAttributeInfoWrapper) obj;
        if (info == null) {
            if (other.info != null)
                return false;
        } else if (!info.equals(other.info))
            return false;
        return true;
    }

	public String getName() {
		return getMBeanAttributeInfo().getName();
	}
}
