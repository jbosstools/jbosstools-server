/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.IStatus;
import org.jboss.tools.jmx.jvmmonitor.core.IHost;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;
import org.jboss.tools.jmx.jvmmonitor.core.mbean.IMBeanServer;

/**
 * The MBean server. MBeanServerConnection is hidden for clients, since its
 * method invocation doesn't return until timeout occurs when target JVM is
 * disconnected.
 */
public class MBeanServer extends AbstractMBeanServer implements IMBeanServer {
    /** The JVM. */
    private ActiveJvm jvm;

    /** The JMX server URL. */
    private JMXServiceURL jmxUrl;

    /** The JMX connector */
    private JMXConnector connector;

    /**
     * The constructor.
     * 
     * @param jmxUrl
     *            The JVM URL
     * @param jvm
     *            The JVM
     */
    @SuppressWarnings("rawtypes")
    protected MBeanServer(JMXServiceURL jmxUrl, ActiveJvm jvm) {
    	super(jvm);
        this.jmxUrl = jmxUrl;
        this.jvm = jvm;
    }

    
    /**
     * Connects to the MBean server in the given VM.
     * 
     * @param url
     *            The JMX service URL
     * @return The MBean server connection
     * @throws JvmCoreException
     */
    private MBeanServerConnection connectToMBeanServer(JMXServiceURL url)
            throws JvmCoreException {
        try {
            if (jvm.getUserName() != null && jvm.getPassword() != null) {
                Map<String, String[]> env = new HashMap<String, String[]>();
                env.put(JMXConnector.CREDENTIALS,
                        new String[] { jvm.getUserName(), jvm.getPassword() });
                connector = JMXConnectorFactory.connect(url, env);
            } else {
                connector = JMXConnectorFactory.connect(url);
            }
            return connector.getMBeanServerConnection();
        } catch (IOException e) {
            IHost host = jvm.getHost();
            if (host != null && host.getActiveJvms().contains(jvm)) {
                host.removeJvm(jvm.getPid());
            }
            throw new JvmCoreException(IStatus.INFO,
                    Messages.connectToMBeanServerFailedMsg, e);
        } catch(NullPointerException npe) {
        	npe.printStackTrace();
            throw new JvmCoreException(IStatus.INFO,
                    Messages.connectToMBeanServerFailedMsg, npe);
        }
    }

	@Override
	protected MBeanServerConnection createMBeanServerConnection() throws JvmCoreException {
		return connectToMBeanServer(jmxUrl);
	}
}
