/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.test.util;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.jboss.tools.jmx.jolokia.test.JolokiaTestPlugin;
import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.jolokia.http.AgentServlet;
import org.jolokia.jmx.JolokiaMBeanServerUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JolokiaTestEnvironmentSetup {

	public static final  String JOLOKIA_IT_DOMAIN = "jolokia.it";
	private static final String JOLOKIA = "jolokia";
	private static Server jettyServer;
	private static String j4pUrl;
	protected static J4pClient j4pClient;
	private static Set<ObjectName> registeredMBeans = new HashSet<>();
	
	@BeforeClass
	public static void start() throws Exception{
		String testUrl = System.getProperty("j4p.url");
		if (testUrl == null) {

			int port = EnvTestUtil.getFreePort();
			jettyServer = new Server(port);
			SecurityHandler securityHandler = createSecurityHandler();
			ServletContextHandler jettyContext = new ServletContextHandler(jettyServer, "/", null, securityHandler, null, null);
			ServletHolder holder = new ServletHolder(new AgentServlet());
			holder.setInitParameter("dispatcherClasses", "org.jolokia.jsr160.Jsr160RequestDispatcher");
			jettyContext.addServlet(holder, "/j4p/*");

			jettyServer.start();
			j4pUrl = "http://localhost:" + port + "/j4p";

			MBeanServer mBeanServer = JolokiaMBeanServerUtil.getJolokiaMBeanServer();
			registeredMBeans.add(mBeanServer.registerMBean(new OperationChecking(JOLOKIA_IT_DOMAIN), new ObjectName(JOLOKIA_IT_DOMAIN+":type=operation")).getObjectName());
			registeredMBeans.add(mBeanServer.registerMBean(new AttributeChecking(JOLOKIA_IT_DOMAIN), new ObjectName(JOLOKIA_IT_DOMAIN+":type=attributetest")).getObjectName());
		} else {
			j4pUrl = testUrl;
		}
		j4pClient = createJ4pClient(j4pUrl);
	}
	
	@AfterClass
	public static void stop() throws Exception{
		if(!registeredMBeans.isEmpty()){
			for (ObjectName registeredMBean : registeredMBeans) {
				JolokiaMBeanServerUtil.getJolokiaMBeanServer().unregisterMBean(registeredMBean);
			}
			registeredMBeans.clear();
		}
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}
	
    private static SecurityHandler createSecurityHandler() throws Exception {
    	IPath p = JolokiaTestPlugin.getDefault().getStateLocation().append("jolokiasecurity.txt");
    	if( !p.toFile().exists()) {
    		Path path = Paths.get(p.toOSString());
    		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
    		    writer.write(JOLOKIA + ": " + JOLOKIA + "," + JOLOKIA); // user: pass,role
    		} 
    	}
    	
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
		constraint.setRoles(new String[]{JOLOKIA});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        HashLoginService loginService = new HashLoginService("Jolokia", p.toOSString());
        securityHandler.setLoginService(loginService);
        securityHandler.setConstraintMappings(new ConstraintMapping[]{cm});
        return securityHandler;
    }
    
    protected static J4pClient createJ4pClient(String url) {
        return J4pClient.url(url)
                .user(JOLOKIA)
                .password(JOLOKIA)
                .authenticator(new BasicAuthenticator().preemptive())
                .pooledConnections()
                .build();
    }
	
}
