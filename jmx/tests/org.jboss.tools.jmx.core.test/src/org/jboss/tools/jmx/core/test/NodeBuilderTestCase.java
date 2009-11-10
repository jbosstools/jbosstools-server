/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.core.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.tree.DomainNode;
import org.jboss.tools.jmx.core.tree.Node;
import org.jboss.tools.jmx.core.tree.NodeBuilder;
import org.jboss.tools.jmx.core.tree.ObjectNameNode;
import org.jboss.tools.jmx.core.tree.PropertyNode;

import junit.framework.TestCase;

public class NodeBuilderTestCase extends TestCase {

    private IConnectionWrapper mockConn;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockConn = new MockConnectionWrapper();
    }

    @Override
    protected void tearDown() throws Exception {
        mockConn = null;
        super.tearDown();
    }

    public void testOneObjectName() throws Exception {
        ObjectName on = new ObjectName("test:type=Test,name=Test1"); //$NON-NLS-1$

        Node root = NodeBuilder.createRoot(mockConn);
        NodeBuilder.addToTree(root, on);

        Node[] children = root.getChildren();
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof DomainNode);
        DomainNode domainNode = (DomainNode) children[0];
        assertEquals("test", domainNode.getDomain()); //$NON-NLS-1$

        children = domainNode.getChildren();
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof PropertyNode);
        PropertyNode typeNode = (PropertyNode) children[0];
        assertEquals("type", typeNode.getKey()); //$NON-NLS-1$
        assertEquals("Test", typeNode.getValue()); //$NON-NLS-1$

        children = typeNode.getChildren();
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof ObjectNameNode);
        ObjectNameNode onNode = (ObjectNameNode) children[0];
        assertEquals(on, onNode.getObjectName());
    }

    public void testTwoObjectNames() throws Exception {
        ObjectName on = new ObjectName("test:type=Test,name=Test1"); //$NON-NLS-1$
        ObjectName on2 = new ObjectName("test:type=Test,name=Test2"); //$NON-NLS-1$

        Node root = NodeBuilder.createRoot(mockConn);
        NodeBuilder.addToTree(root, on);
        NodeBuilder.addToTree(root, on2);

        Node[] children = root.getChildren();
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof DomainNode);
        DomainNode domainNode = (DomainNode) children[0];
        assertEquals("test", domainNode.getDomain()); //$NON-NLS-1$

        children = domainNode.getChildren();
        assertEquals(1, children.length);
        assertTrue(children[0] instanceof PropertyNode);
        PropertyNode typeNode = (PropertyNode) children[0];
        assertEquals("type", typeNode.getKey()); //$NON-NLS-1$
        assertEquals("Test", typeNode.getValue()); //$NON-NLS-1$

        children = typeNode.getChildren();
        assertEquals(2, children.length);
        assertTrue(children[0] instanceof ObjectNameNode);
        assertTrue(children[1] instanceof ObjectNameNode);
        ObjectNameNode onNode = (ObjectNameNode) children[0];
        ObjectNameNode onNode2 = (ObjectNameNode) children[1];
        assertEquals(on, onNode.getObjectName());
        assertEquals(on2, onNode2.getObjectName());
    }

    public void testTwoDifferentDomains() throws Exception {
        ObjectName on = new ObjectName("test:type=Test,name=Test1"); //$NON-NLS-1$
        ObjectName other = new ObjectName("other:type=Test,name=Test2"); //$NON-NLS-1$

        Node root = NodeBuilder.createRoot(mockConn);
        NodeBuilder.addToTree(root, on);
        NodeBuilder.addToTree(root, other);

        Node[] children = root.getChildren();
        assertEquals(2, children.length);
        assertTrue(children[0] instanceof DomainNode);
        assertTrue(children[1] instanceof DomainNode);
        DomainNode domainNode1 = (DomainNode) children[0];
        DomainNode domainNode2 = (DomainNode) children[1];
        // domains are sorted by lexical order
        assertEquals("other", domainNode1.getDomain()); //$NON-NLS-1$
        assertEquals("test", domainNode2.getDomain()); //$NON-NLS-1$
    }

    public void testHierarchy() throws Exception {
        ObjectName on = new ObjectName("test:type=Test,name=Test1"); //$NON-NLS-1$
        ObjectName on2 = new ObjectName("test:type=Test,name=Test2"); //$NON-NLS-1$
        ObjectName on3 = new ObjectName("test:type=AnotherTest,name=Test1"); //$NON-NLS-1$
        ObjectName on4 = new ObjectName("test:type=AnotherTest,name=Test2"); //$NON-NLS-1$
        ObjectName on5 = new ObjectName("other:type=Test,name=Test1"); //$NON-NLS-1$

        Node root = NodeBuilder.createRoot(mockConn);
        NodeBuilder.addToTree(root, on);
        NodeBuilder.addToTree(root, on2);
        NodeBuilder.addToTree(root, on3);
        NodeBuilder.addToTree(root, on4);
        NodeBuilder.addToTree(root, on5);
    }
}
