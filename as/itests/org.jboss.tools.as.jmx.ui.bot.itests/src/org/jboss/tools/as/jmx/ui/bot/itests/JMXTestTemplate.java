/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.jmx.ui.bot.itests;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.jboss.tools.as.jmx.ui.bot.itests.util.JMXUtils;
import org.jboss.tools.jmx.reddeer.core.JMXConnection;
import org.jboss.tools.jmx.reddeer.ui.view.JMXNavigatorView;
import org.junit.After;
import org.junit.Before;

/**
 * JMX Test class template 
 * @author odockal
 *
 */
public abstract class JMXTestTemplate {

	protected JMXConnection connection;
	protected JMXNavigatorView view;
	
	protected static final String JAVA_APP = "RunApp";
	
	private static Process processCompile;
	
	private static Process processRun;

	protected static final Path JAVA_FOLDER = Paths.get(System.getProperty("user.dir"), "projects");
	
	private static Logger log = Logger.getLogger(JMXTestTemplate.class);
	
	public static void runJavaApp() {
		try {	
			processCompile = Runtime.getRuntime().exec("javac " + JAVA_FOLDER + "/" + JAVA_APP + ".java");
			
			processRun = Runtime.getRuntime().exec("java -classpath " + JAVA_FOLDER + " RunApp");
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void killJavaApp() {
		if (processCompile != null) {
			processCompile.destroy();
		}
		if (processRun != null) {
			processRun.destroy();	
		}
	}
	
	/**
	 * This abstract method need to be implemented in child's class and initialize {@link #connection} field.
	 */
	public abstract void setUpJMXConnection();
			
	@Before
	public void setUpEclipseConnection() {
		setUpView();
		runJavaApp();
		new WaitUntil(new JobIsRunning(), TimePeriod.MEDIUM, false);
		setUpJMXConnection();
	}
	
	public void setUpView() {
		view = new JMXNavigatorView();
		view.open();
	}
	
	@After
	public void tearDownConnections() {
		JMXUtils.closeAllEditors();
		if (connection != null && !connection.getItem().isDisposed() && connection.isConnected()) {
			connection.disconnect();
		}
		connection = null;
		if (view.isOpen()) {
			view.close();
		}
		view = null;
		killJavaApp();
	}

}
