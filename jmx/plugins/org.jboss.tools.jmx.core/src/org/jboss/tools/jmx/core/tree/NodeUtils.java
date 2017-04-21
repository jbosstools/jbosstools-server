/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core.tree;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.core.JMXException;

public class NodeUtils {
	
	private NodeUtils() {
		/* Util class */
	}

    public static ObjectNameNode findObjectNameNode(Node node,
            ObjectName objectName) {
        Assert.isNotNull(node);

        if (node instanceof ObjectNameNode) {
            ObjectNameNode onNode = (ObjectNameNode) node;
            if (onNode.getObjectName().equals(objectName)) {
                return onNode;
            }
        }
        Node[] children = node.getChildren();
        for (int i = 0; i < children.length; i++) {
            Node child = children[i];
            Node found = findObjectNameNode(child, objectName);
            if (found != null) {
                return (ObjectNameNode) found;
            }
        }
        return null;
    }

    public static Root createObjectNameTree(final IConnectionWrapper connectionWrapper, final IProgressMonitor monitor)
            throws JMXException {
    	final Root[] roots = new Root[1];
    	connectionWrapper.run(new IJMXRunnable() {
    		@Override
			public void run(MBeanServerConnection connection) throws Exception {
		        monitor.beginTask(JMXCoreMessages.LoadMBeans, 1000);
				Set<ObjectName> beanInfo = connection.queryNames(new ObjectName("*:*"), null); //$NON-NLS-1$
		        roots[0] = NodeBuilder.createRoot(connectionWrapper);
				monitor.worked(100);
				if( beanInfo != null ) {
					SubMonitor subMon = SubMonitor.convert(monitor, JMXCoreMessages.InspectMBeans, beanInfo.size());
			        Iterator<ObjectName> iter = beanInfo.iterator();
			        while (iter.hasNext() && !monitor.isCanceled()) {
			            ObjectName on = iter.next();
			            NodeBuilder.addToTree(roots[0].getMBeansNode(), on, connection);
			        	subMon.worked(1);
			        }
				}
		        monitor.done();
			}
    	});
        return roots[0];
    }
}
