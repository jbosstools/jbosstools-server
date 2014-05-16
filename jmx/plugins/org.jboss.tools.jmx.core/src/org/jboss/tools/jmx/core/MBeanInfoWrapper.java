/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.core;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.NotificationBroadcaster;
import javax.management.ObjectName;

import org.eclipse.core.runtime.Assert;
import org.jboss.tools.jmx.core.tree.ObjectNameNode;
import org.jboss.tools.jmx.core.util.EqualsUtil;

@SuppressWarnings("rawtypes")
public class MBeanInfoWrapper implements Comparable {
    private final ObjectName on;
    private final MBeanInfo info;
    private final MBeanServerConnection mbsc;
    private final ObjectNameNode parent;

    public MBeanInfoWrapper(ObjectName on, MBeanInfo info,
            MBeanServerConnection mbsc, ObjectNameNode parent) {
        Assert.isNotNull(on);
        Assert.isNotNull(info);
        Assert.isNotNull(mbsc);
        this.on = on;
        this.info = info;
        this.mbsc = mbsc;
        this.parent = parent;
    }

    public ObjectNameNode getParent() {
    	return parent;
    }
    
    public ObjectName getObjectName() {
        return on;
    }

    public MBeanInfo getMBeanInfo() {
        return info;
    }

    public MBeanServerConnection getMBeanServerConnection() {
        return mbsc;
    }

    /**
     * Test if the wrapped MBean is a <code>NotificationBroadcaster</code>
     * using {@link MBeanServerConnection#isInstanceOf(ObjectName, String)}.
     *
     * @return <code>true</code> if the wrapped MBean is a
     *         <code>NotificationBroadcaster</code>, <code>false</code>
     *         else
     */
    public boolean isNotificationBroadcaster() {
        try {
            return mbsc.isInstanceOf(on, NotificationBroadcaster.class
                    .getName());
        } catch (Exception e) {
            return false;
        }
    }

    public MBeanAttributeInfoWrapper[] getMBeanAttributeInfoWrappers() {
        MBeanAttributeInfo[] attributes = info.getAttributes();
        MBeanAttributeInfoWrapper[] attrWrappers = new MBeanAttributeInfoWrapper[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            MBeanAttributeInfo attrInfo = attributes[i];
            attrWrappers[i] = new MBeanAttributeInfoWrapper(attrInfo, this);
        }
        return attrWrappers;
    }

    public MBeanOperationInfoWrapper[] getMBeanOperationInfoWrappers() {
        MBeanOperationInfo[] operations = info.getOperations();
        MBeanOperationInfoWrapper[] opWrappers = new MBeanOperationInfoWrapper[operations.length];
        for (int i = 0; i < operations.length; i++) {
            MBeanOperationInfo opInfo = operations[i];
            opWrappers[i] = new MBeanOperationInfoWrapper(opInfo, this);
        }
        return opWrappers;
    }

    public MBeanNotificationInfoWrapper[] getMBeanNotificationInfoWrappers() {
        MBeanNotificationInfo[] notifications = info.getNotifications();
        MBeanNotificationInfoWrapper[] notificationWrappers = new MBeanNotificationInfoWrapper[notifications.length];
        for (int i = 0; i < notifications.length; i++) {
            MBeanNotificationInfo opInfo = notifications[i];
            notificationWrappers[i] = new MBeanNotificationInfoWrapper(opInfo,
                    on, mbsc);
        }
        return notificationWrappers;
    }

    public MBeanFeatureInfoWrapper[] getMBeanFeatureInfos() {
        MBeanAttributeInfo[] attributes = info.getAttributes();
        MBeanOperationInfo[] operations = info.getOperations();
        MBeanFeatureInfoWrapper[] o = new MBeanFeatureInfoWrapper[attributes.length
                + operations.length];
        for (int i = 0; i < attributes.length; i++) {
            MBeanAttributeInfo attrInfo = attributes[i];
            o[i] = new MBeanAttributeInfoWrapper(attrInfo, this);
        }
        for (int i = 0; i < operations.length; i++) {
            MBeanOperationInfo opInfo = operations[i];
            o[attributes.length + i] = new MBeanOperationInfoWrapper(opInfo,
                    this);
        }
        return o;
    }

    public int compareTo(Object object) {
        if (object instanceof MBeanInfoWrapper) {
            MBeanInfoWrapper other = (MBeanInfoWrapper) object;
            return on.toString().compareTo(other.on.toString());
        }
        return 0;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((info == null) ? 0 : info.hashCode());
        result = PRIME * result + ((mbsc == null) ? 0 : mbsc.hashCode());
        result = PRIME * result + ((on == null) ? 0 : on.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MBeanInfoWrapper other = (MBeanInfoWrapper) obj;
        if (info == null) {
            return other.info == null;
        } else if (other.info == null ) {
        	return false;
        } 
        if (mbsc == null) {
            return other.mbsc == null;
        } else if (!mbsc.equals(other.mbsc))
            return false;
        if (on == null) {
           return other.on == null;
        } else if (!on.equals(other.on))
            return false;
        try {
        	return info.equals(other.info);
        } catch( NullPointerException npe ) {
        	return EqualsUtil.infoEquals(info, other.info);
        }
    }
    
 
}
