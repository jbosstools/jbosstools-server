/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.acme.mx;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.logging.Logger;

/**
 * ManagementAgent.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class ManagementAgent implements ManagementAgentMBean
{
   private static final Logger log = Logger.getLogger(ManagementAgent.class);
   
   private static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
   
   private int registryPort;
   
   private int proxyPort;
   
   private InetAddress bindAddress;
   
   private JMXConnectorServer connector;
   
   public void start() throws Exception
   {
      /* Ensure cryptographically strong random number generator used
       * to choose the object number - see java.rmi.server.ObjID */
      System.setProperty("java.rmi.server.randomIDs", "true");

      /* Start an RMI registry on given registry port */
      log.debug("Create RMI registry on port " + registryPort);
      LocateRegistry.createRegistry(registryPort);
      
      /*  Environment map. */
      log.debug("Initialize the environment map");
      Map<String,?> env = new HashMap<String,Object>();

      /* Create an RMI connector server. As specified in the JMXServiceURL the 
       * RMIServer stub will be registered in the RMI registry running in the 
       * given host and given registry and proxy ports, with the name "jmxrmi".*/
      
      String host = bindAddress.getHostAddress();
      
      log.debug("Create an RMI connector server");
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + host + ":" + 
            proxyPort  + "/jndi/rmi://" + host + ":" + registryPort + "/jmxrmi");
      connector = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
      connector.start();
   }

   public void stop() throws Exception
   {      
      connector.stop();
   }
   
   public int getRegistryPort()
   {
      return registryPort;
   }
   
   public void setRegistryPort(int port)
   {
      registryPort = port;
   }
   
   public int getProxyPort()
   {
      return proxyPort;
   }

   public void setProxyPort(int port)
   {
      proxyPort = port;
   }

   public String getBindAddress()
   {
      return bindAddress.getHostAddress();
   }

   public void setBindAddress(String host) throws UnknownHostException
   {
      bindAddress = InetAddress.getByName(host);
   }
}
