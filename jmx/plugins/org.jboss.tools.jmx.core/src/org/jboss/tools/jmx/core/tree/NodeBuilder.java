/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core.tree;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.tools.jmx.core.IConnectionWrapper;


public class NodeBuilder {

    public static void addToList(Node root, ObjectName on) {
        Node node = buildDomainNode(root, on.getDomain());
        node = buildObjectNameNode(node, "on", on.getKeyPropertyListString(), on); //$NON-NLS-1$
    }

    public static void addToTree(Node root, ObjectName on) {
    	addToTree(root, on, null);
    }
    
    public static void addToTree(Node root, ObjectName on, MBeanServerConnection mbsc) {
        Node node = buildDomainNode(root, on.getDomain());
        String keyPropertyListString = on.getKeyPropertyListString();
        String[] properties = keyPropertyListString.split(","); //$NON-NLS-1$
        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            String key = property.substring(0, property.indexOf('='));
            String value = property.substring(property.indexOf('=') + 1,
                    property.length());
            if (i == properties.length - 1) {
                node = buildObjectNameNode(node, key, value, on, mbsc);
            } else {
                node = buildPropertyNode(node, key, value);
            }
        }
    }

    public static Root createRoot(IConnectionWrapper connection) {
        return new Root(connection);
    }

    static Node buildDomainNode(Node parent, String domain) {
        Node n = new DomainNode(parent, domain);
        if (parent != null) {
            return parent.addChildren(n);
        }
        return n;
    }

    static Node buildPropertyNode(Node parent, String key, String value) {
        Node n = new PropertyNode(parent, key, value);
        if (parent != null) {
            return parent.addChildren(n);
        }
        return n;
    }

    static Node buildObjectNameNode(Node parent, String key, String value,
            ObjectName on) {
    	return buildObjectNameNode(parent, key, value, on, null);
    }
    
    static Node buildObjectNameNode(Node parent, String key, String value,
            ObjectName on, MBeanServerConnection mbsc) {
        Node n = new ObjectNameNode(parent, key, value, on, mbsc);
        if (parent != null) {
            return parent.addChildren(n);
        }
        return n;
    }

}
