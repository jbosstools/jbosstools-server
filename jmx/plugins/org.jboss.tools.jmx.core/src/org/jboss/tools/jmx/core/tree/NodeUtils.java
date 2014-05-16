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

package org.jboss.tools.jmx.core.tree;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.jmx.commons.tree.Node;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.core.JMXException;


public class NodeUtils {

	public static PropertyNode findObjectNameNode(Node node,
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
				return (PropertyNode) found;
			}
		}
		return null;
	}

	    public static Root createObjectNameTree(final IConnectionWrapper connectionWrapper, final IProgressMonitor monitor)
	            throws JMXException {
	    	final Root[] _root = new Root[1];
	    	connectionWrapper.run(new IJMXRunnable() {
	    		@SuppressWarnings("rawtypes")	    		
				public void run(MBeanServerConnection connection) throws JMXException {
			        monitor.beginTask(JMXCoreMessages.LoadMBeans, 1000);
					Set beanInfo = null;
					try {
						beanInfo = connection.queryNames(new ObjectName("*:*"), null); //$NON-NLS-1$
					} catch (MalformedObjectNameException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					monitor.worked(100);
					SubProgressMonitor subMon = new SubProgressMonitor(monitor, 900);
					subMon.beginTask(JMXCoreMessages.InspectMBeans, beanInfo.size() * 100);
			        _root[0] = NodeBuilder.createRoot(connectionWrapper);
			        Iterator iter = beanInfo.iterator();
			        while (iter.hasNext()) {
			            ObjectName on = (ObjectName) iter.next();
			            NodeBuilder.addToTree(_root[0], on, connection);
			        	subMon.worked(100);
			        }
			        subMon.done();
			        monitor.done();
				}
	    	});
		Root root = _root[0];
		if (root != null) {
			enrichRootNode(root);
		}
		return root;
	}

	protected static void enrichRootNode(Root root) {
		List<NodeProvider> providers = JMXActivator.getNodeProviders();
		for (NodeProvider provider : providers) {
			provider.provide(root);
		}
	}
}
