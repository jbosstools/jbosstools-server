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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.reddeer.swt.api.TreeItem;
import org.jboss.tools.jmx.reddeer.core.exception.JMXException;

public class JMXConnectionItem extends AbstractJMXConnection {
	
	public JMXConnectionItem(TreeItem item) {
		super(item);
	}
	
	public List<JMXConnection> getConnections() {
		List<JMXConnection> list = new ArrayList<>();
		for (TreeItem item : this.item.getItems()) {
			list.add(new JMXConnection(item));
		}
		return list;
	}
	
	public List<JMXConnection> getConnections(String name) {
		return getConnections().stream()
				.filter( x -> x.getLabel().getName().contains(name))
				.collect(Collectors.toList());
	}
	
	public JMXConnection getConnection(String name) {
		return getConnections().stream()
				.filter( x -> x.getLabel().getName().equals(name))
				.findFirst()
				.orElseThrow(() -> {
					String message = "No JMX connection found matching: " + name;
					message = message.concat(" \r\nAvailable connections are: " + getConnections().stream().map(x -> x.getLabel().getName()).collect(Collectors.joining(", ")));
					throw new JMXException(message);});
	}
	
}
