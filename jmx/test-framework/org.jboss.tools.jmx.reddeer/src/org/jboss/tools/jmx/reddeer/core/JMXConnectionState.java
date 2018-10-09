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

import org.jboss.tools.jmx.reddeer.core.exception.JMXException;

/**
 * 
 * @author odockal
 *
 */
public enum JMXConnectionState {

	CONNECTED("Connected"),
	DISCONNECTED("Disconnected");
	
	private String state;
	
	JMXConnectionState(String state) {
		this.state = state;
	}
	
	public String getState() {
		return this.state;
	}
	
	public static JMXConnectionState get(String value) {
		if (value == null) throw new JMXException("Passing null value as a state");
		switch(value.toLowerCase()) {
			case "connected":
				return CONNECTED;
			case "disconnected":
				return DISCONNECTED;
			default:
				throw new JMXException("Not a defined state: " + value);
		}
	}

}
