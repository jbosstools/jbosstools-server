/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.jboss.tools.jmx.ui.test.interactive"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    //private JMXConnectorServer cs;

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
//        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//        mbs.registerMBean(new ArrayType(), ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=ArrayType")); //$NON-NLS-1$
//        mbs.registerMBean(new WritableAttributes(), ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=WritableAttributes")); //$NON-NLS-1$
//        mbs.registerMBean(new ComplexType(), ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=ComplexType")); //$NON-NLS-1$
//        mbs.registerMBean(new OperationResults(), ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=OperationResults")); //$NON-NLS-1$
//        mbs.registerMBean(new Registration(), ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=Registration")); //$NON-NLS-1$
//        mbs.registerMBean(new CustomizedAttributes(), ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=CustomizedAttributes")); //$NON-NLS-1$
//        mbs.registerMBean(new NotifEmitter(), ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=NotifEmitter")); //$NON-NLS-1$
//        try {
//            System.setProperty("java.rmi.server.randomIDs", "true"); //$NON-NLS-1$ //$NON-NLS-2$
//            LocateRegistry.createRegistry(3000);
//            JMXServiceURL url = new JMXServiceURL(
//                    "service:jmx:rmi:///jndi/rmi://:3000/jmxrmi"); //$NON-NLS-1$
//            cs = JMXConnectorServerFactory
//                    .newJMXConnectorServer(url, null, mbs);
//            cs.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
//        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//        mbs.unregisterMBean(ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=ArrayType")); //$NON-NLS-1$
//        mbs.unregisterMBean(ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=WritableAttributes")); //$NON-NLS-1$
//        mbs.unregisterMBean(ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=ComplexType")); //$NON-NLS-1$
//        mbs.unregisterMBean(ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=OperationResults")); //$NON-NLS-1$
//        mbs.unregisterMBean(ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=Registration")); //$NON-NLS-1$
//        mbs.unregisterMBean(ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=CustomizedAttributes")); //$NON-NLS-1$
//        mbs.unregisterMBean(ObjectName
//                .getInstance("org.jboss.tools.jmx.test:type=NotifEmitter")); //$NON-NLS-1$
//        cs.stop();
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public void earlyStartup() {
        Activator.getDefault();
    }
}
