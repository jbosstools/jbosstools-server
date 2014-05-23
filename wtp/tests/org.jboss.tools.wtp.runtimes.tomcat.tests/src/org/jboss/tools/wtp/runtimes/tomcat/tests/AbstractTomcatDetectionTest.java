/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
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

	protected static final String REQUIREMENTS_DIR = "target/requirements/";

	protected static final String TOMCAT_6 = "apache-tomcat-6.0.39";

	protected static final String TOMCAT_6_PATH = REQUIREMENTS_DIR + TOMCAT_6;

	protected static final String TOMCAT_7 = "apache-tomcat-7.0.52";

	protected static final String TOMCAT_7_PATH = REQUIREMENTS_DIR + TOMCAT_7;

	protected static final String TOMCAT_8 = "apache-tomcat-8.0.3";

	protected static final String TOMCAT_8_PATH = REQUIREMENTS_DIR + TOMCAT_8;

	@BeforeClass
	public static void beforeClass() {
		File tomcat6 = new File(TOMCAT_6_PATH); 
		assertTrue(TOMCAT_6_PATH + " is missing, please run 'mvn clean pre-integration-test'", tomcat6.exists());
		File tomcat7 = new File(TOMCAT_7_PATH);
		assertTrue(TOMCAT_7_PATH + " is missing, please run 'mvn clean pre-integration-test'", tomcat7.exists());
		File tomcat8 = new File(TOMCAT_8_PATH);
		assertTrue(TOMCAT_8_PATH + " is missing, please run 'mvn clean pre-integration-test'", tomcat8.exists());
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
}
