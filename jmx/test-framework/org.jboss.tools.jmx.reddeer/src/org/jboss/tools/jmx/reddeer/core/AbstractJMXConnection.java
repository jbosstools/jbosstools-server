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
package org.jboss.tools.jmx.reddeer.core;

import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.jboss.tools.jmx.reddeer.ui.view.JMXNavigatorView;

/**
 * 
 * @author odockal
 *
 */
public abstract class AbstractJMXConnection {

	private JMXNavigatorView view;
	
	protected TreeItem item;
	
	public TreeItem getItem() {
		return item;
	}

	public AbstractJMXConnection(TreeItem item) {
		this.item = item;
		this.view = new JMXNavigatorView();
	}
	
	public void activate() {
		this.view.activate();
	}
	
	public void select() {
		activate();
		this.item.select();
	}
	
	public void refresh() {
		select();
		new ContextMenuItem("Refresh");
	}
	
	/**
	 * TODO: implement
	 * @return
	 */
	public Object newConnection() {
		select();
		new ContextMenuItem("New Connection...").select();
		return null;
	}

}
