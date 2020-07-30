/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.wtp.runtimes.tomcat.itests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.wtp.core.launching.IExecutionEnvironmentConstants;
import org.jboss.tools.as.test.core.internal.utils.JREUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class AbstractTomcatDetectionTest {

	// use absolute path, not relative, because Jenkins paths don't always work as relative paths
	protected static final String REQUIREMENTS_DIR = System.getProperty("basedir",".") + "/target/requirements/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	protected static final String TOMCAT_7 = "apache-tomcat-" + System.getProperty("jbosstools.test.tomcat.version.7", "7.0.54"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String TOMCAT_7_PATH = REQUIREMENTS_DIR + TOMCAT_7;

	protected static final String TOMCAT_8 = "apache-tomcat-" + System.getProperty("jbosstools.test.tomcat.version.8", "8.0.8"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String TOMCAT_8_PATH = REQUIREMENTS_DIR + TOMCAT_8;
	
	protected static final String TOMCAT_9 = "apache-tomcat-" + System.getProperty("jbosstools.test.tomcat.version.8", "9.0.36"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String TOMCAT_9_PATH = REQUIREMENTS_DIR + TOMCAT_9;

	protected static final String UNEXPECTED_RUNTIME_COUNT_ERROR = Messages.incorrect_number_of_runtimes + " [ " + REQUIREMENTS_DIR + " ]"; //$NON-NLS-2$ //$NON-NLS-3$

	public static final String JRE_8_HOME;
	
	static {
		String jre8 = System.getProperty("jbosstools.test.jre.8");
		JRE_8_HOME = jre8 != null && !jre8.trim().isEmpty() ? jre8.trim() : null; 
	}

	@BeforeClass
	public static void beforeClass() {
		File tomcat7 = new File(TOMCAT_7_PATH);
		assertTrue(TOMCAT_7_PATH + Messages.is_missing + "'mvn clean pre-integration-test'", tomcat7.exists()); //$NON-NLS-2$
		File tomcat8 = new File(TOMCAT_8_PATH);
		assertTrue(TOMCAT_8_PATH + Messages.is_missing + "'mvn clean pre-integration-test'", tomcat8.exists()); //$NON-NLS-2$
		File tomcat9 = new File(TOMCAT_9_PATH);
		assertTrue(TOMCAT_9_PATH + Messages.is_missing + "'mvn clean pre-integration-test'", tomcat9.exists()); //$NON-NLS-2$
		
		checkJRE8Availability();
	}

	@AfterClass
	public static void afterClass() {
	}
	
	@Before
	public void setUp() throws CoreException {
		clearAll();
	}

	@After
	public void tearDown() throws CoreException {
		clearAll();
	}
	
	public void clearAll() throws CoreException {
		// remove all wtp servers
		IServer[] s = ServerCore.getServers();
		for( int i = 0; i < s.length; i++ ) {
		   s[i].delete();
		}

		// Remove all wtp runtimes
		IRuntime[] r = ServerCore.getRuntimes();
		for( int i = 0; i < r.length; i++ ) {
			r[i].delete();
		}
	}

    protected String toString(Object[] runtimes) {
      if (runtimes == null) {
        return null;
      }
    
      StringBuilder sb = new StringBuilder();
      boolean prependComma = false;
      for (Object o : runtimes) {
        if (prependComma) {
          sb.append(", "); //$NON-NLS-1$
        }
        if (o instanceof IRuntime) {
          sb.append(((IRuntime) o).getName());
        } else if (o instanceof IServer) {
          sb.append(((IServer) o).getName());
        } else {
          sb.append(o);
        }
        prependComma = true;
      }
    
      return sb.toString();
    }

    @SuppressWarnings("restriction")
	private static void checkJRE8Availability() {
      IExecutionEnvironment se8Env = EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_JavaSE18);
      System.out.println("jre8 home is " + JRE_8_HOME);
      if (JRE_8_HOME != null && !JRE_8_HOME.startsWith("${")) {
        assertTrue("JRE8 home " + JRE_8_HOME + " does not exist", new File(JRE_8_HOME).exists());
        IVMInstall foundOrCreated = JREUtils.findOrCreateJRE(new Path(JRE_8_HOME));
        System.out.println(foundOrCreated);
        if( foundOrCreated != null )
        	System.out.println(foundOrCreated.getInstallLocation().getAbsolutePath());
        assertNotNull(foundOrCreated);
        assertTrue(JRE_8_HOME + " is not a Java 8+ runtime", se8Env.isStrictlyCompatible(foundOrCreated));
        return;
      }

        assertTrue("Tomcat Detection tests needs to run with at least one JRE8 runtime available. Please set the jbosstools.test.jre.8 property to point to a valid JRE.", se8Env.getCompatibleVMs().length > 0);
    }
	
}
