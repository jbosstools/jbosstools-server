/******************************************************************************* 
 * Copyright (c) 2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.jmx.reddeer.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;

/**
 * Wait condition for existence of JMX connection, check contains name ignore case
 * @author odockal
 *
 */
public class JMXConnectionExists extends AbstractWaitCondition {

	private JMXConnectionItem connectionItem;
	private List<JMXConnection> items = new ArrayList<>();
	private String name;
	
	public JMXConnectionExists(JMXConnectionItem item, String name) {
		this.connectionItem = item;
		this.name = name;
	}
	
	@Override
	public boolean test() {
		List<JMXConnection> list = connectionItem.getConnectionsIgnoreCase(name);
		if (list != null && !list.isEmpty()) {
			items.addAll(list);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<JMXConnection> getResult() {
		return items;
	}

}
