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
package org.jboss.tools.wtp.runtimes.tomcat.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class AbstractTomcatDetectionTest {

	// use absolute path, not relative, because Jenkins paths don't always work as relative paths
	protected static final String REQUIREMENTS_DIR = System.getProperty("basedir",".") + "/target/requirements/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	protected static final String TOMCAT_6 = "apache-tomcat-" + System.getProperty("jbosstools.test.tomcat.version.6"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String TOMCAT_6_PATH = REQUIREMENTS_DIR + TOMCAT_6;

	protected static final String TOMCAT_7 = "apache-tomcat-" + System.getProperty("jbosstools.test.tomcat.version.7"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String TOMCAT_7_PATH = REQUIREMENTS_DIR + TOMCAT_7;

	protected static final String TOMCAT_8 = "apache-tomcat-" + System.getProperty("jbosstools.test.tomcat.version.8"); //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String TOMCAT_8_PATH = REQUIREMENTS_DIR + TOMCAT_8;

  protected static final String UNEXPECTED_RUNTIME_COUNT_ERROR = Messages.incorrect_number_of_runtimes + " [ " + REQUIREMENTS_DIR + " ]"; //$NON-NLS-2$ //$NON-NLS-3$

	@BeforeClass
	public static void beforeClass() {
		File tomcat6 = new File(TOMCAT_6_PATH); 
		assertTrue(TOMCAT_6_PATH + Messages.is_missing + "'mvn clean pre-integration-test'", tomcat6.exists()); //$NON-NLS-2$
		File tomcat7 = new File(TOMCAT_7_PATH);
		assertTrue(TOMCAT_7_PATH + Messages.is_missing + "'mvn clean pre-integration-test'", tomcat7.exists()); //$NON-NLS-2$
		File tomcat8 = new File(TOMCAT_8_PATH);
		assertTrue(TOMCAT_8_PATH + Messages.is_missing + "'mvn clean pre-integration-test'", tomcat8.exists()); //$NON-NLS-2$
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
}
