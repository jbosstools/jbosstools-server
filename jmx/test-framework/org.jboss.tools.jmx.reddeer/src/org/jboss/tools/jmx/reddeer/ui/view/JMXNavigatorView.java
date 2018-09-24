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
package org.jboss.tools.jmx.reddeer.ui.view;

import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.workbench.impl.view.WorkbenchView;
import org.jboss.tools.jmx.reddeer.core.JMXConnectionItem;

/**
 * JMX Navigator View class representation
 * @author odockal
 *
 */
public class JMXNavigatorView extends WorkbenchView {

	public static final String TITLE = "JMX Navigator";
	
	public JMXNavigatorView() {
		super(TITLE);
	}
	
	public JMXConnectionItem getLocalProcessesItem() {
		return createConnectionItem("Local Processes");
	}
	
	public JMXConnectionItem getServerConnectionsItem() {
		return createConnectionItem("Server Connections");
	}
	
	public JMXConnectionItem getUserDefinedConnectionsItem() {
		return createConnectionItem("User-Defined Connections");
	}
	
	private JMXConnectionItem createConnectionItem(String name) {
		activate();
		return new JMXConnectionItem(new DefaultTree().getItem(name));
	}

}
