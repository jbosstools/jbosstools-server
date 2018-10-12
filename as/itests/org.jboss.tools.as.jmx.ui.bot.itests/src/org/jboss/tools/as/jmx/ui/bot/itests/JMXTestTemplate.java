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
	
	/**
	 * This abstract method need to be implemented in child's class and initialize {@link #connection} field.
	 */
	public abstract void setUpJMXConnection();
			
	@Before
	public void setUpEclipseConnection() {
		setUpView();
		
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
	}

}
