/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.core.tests;

import junit.framework.TestCase;

public class DefaultMBeanServerConnectionFactoryTest extends TestCase {
//
//    private String correctURL;
//
//    private JMXConnectorServer cs;
//
//    protected void setUp() throws Exception {
//        super.setUp();
//        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://"); //$NON-NLS-1$
//        cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
//        cs.start();
//        correctURL = cs.getAddress().toString();
//        System.out.println(correctURL);
//    }
//
//    protected void tearDown() throws Exception {
//        cs.stop();
//        correctURL = null;
//        super.tearDown();
//    }
//
//    public void testConnectToNullURL() throws Exception {
//        DefaultMBeanServerConnectionFactory factory = new DefaultMBeanServerConnectionFactory();
//        try {
//            factory.createMBeanServerConnection(null);
//            fail("should not connect to null descriptor"); //$NON-NLS-1$
//        } catch (Exception e) {
//        }
//    }
//
//    public void testConnectToBadURL() throws Exception {
//        DefaultMBeanServerConnectionFactory factory = new DefaultMBeanServerConnectionFactory();
//        MBeanServerConnectionDescriptor descriptor = new MBeanServerConnectionDescriptor(UUID.randomUUID().toString(), "service:whatever", null, null);
//        try {
//            factory.createMBeanServerConnection(descriptor);
//            fail("should not connect to bad URL"); //$NON-NLS-1$
//        } catch (Exception e) {
//        }
//    }
//
//    public void testConnectToURL() throws Exception {
//        DefaultMBeanServerConnectionFactory factory = new DefaultMBeanServerConnectionFactory();
//        MBeanServerConnectionDescriptor descriptor = new MBeanServerConnectionDescriptor(correctURL, correctURL, null, null);
//        MBeanServerConnection mbsc = factory.createMBeanServerConnection(descriptor);
//        assertNotNull(mbsc);
//    }
}
